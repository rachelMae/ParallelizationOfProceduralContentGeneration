package com.marching_cubes;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.joml.Vector3f;

/* Marching Cube Algorithm with Shared Resources between Threads */
// This method uses a shared voxel grid, shared positions list,
// and shared atomic integers to keep track of the current cube
// The cubes are stored in a queue and each thread takes a cube from the queue,
// performs the marching cubes algorithm on it,
// and adds the vertices to the shared positions list
// ******DOES NOT PERFORM AS WELL AS GRIDPROCESSOR********

/* To Do */
// * Experiment with having position array be on a separate lock / lockless
// so that marching cube algorithm is run outside the critical section

public class ParallelVoxelGrid implements Runnable {
    // 1-dimensional representation of a 3-dimensional grid
    private ArrayList<Float> voxel_grid = new ArrayList<Float>();
    private int resolution;
    private AtomicInteger atomicCubeX = new AtomicInteger(0);
    private AtomicInteger atomicCubeY = new AtomicInteger(0);
    private AtomicInteger atomicCubeZ = new AtomicInteger(0);
    // rendered vertices
    private CopyOnWriteArrayList<Vector3f> positions = new CopyOnWriteArrayList<Vector3f>();

    // lock for the voxel grid, positions list, and atomic integers
    private Lock lock = new ReentrantLock();;

    public ParallelVoxelGrid(int resolution) {
        this.resolution = resolution;

        // this is where the shape of the mesh is determined
        // so the output from perlin noise, worley noise, WFC etc. goes here
        for (int z = 0; z < resolution; z++) {
            for (int y = 0; y < resolution; y++) {
                for (int x = 0; x < resolution; x++) {
                    this.voxel_grid.add(scalar_field((float)x, (float)y, (float)z));
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            // Acquire the lock before performing operations
            lock.lock();
            try {
                int x = atomicCubeX.get();
                int y = atomicCubeY.get();
                int z = atomicCubeZ.get();

                if (x >= resolution - 1) {
                    return;
                }
                int[] triangulation = get_triangulation(x, y, z);
                for (int i = 0; i < triangulation.length; i++) {
                    if (triangulation[i] < 0) {
                        break;
                    }

                    int[] point_indices = TriTable.EDGES[triangulation[i]];
                    int[] point_1 = TriTable.POINTS[point_indices[0]];
                    int[] point_2 = TriTable.POINTS[point_indices[1]];

                    // positions for the two corners
                    Vector3f position_1 = new Vector3f(x + point_1[0], y + point_1[1], z + point_1[2]);
                    Vector3f position_2 = new Vector3f(x + point_2[0], y + point_2[1], z + point_2[2]);

                    // take the average between two corners
                    Vector3f position = (position_1.add(position_2)).mul(0.5f);

                    // add position to the list of vertex positions to render
                    positions.add(position);
                }

                // increment x y and z
                atomicCubeZ.incrementAndGet();
                if (atomicCubeZ.get() >= resolution - 1) {
                    atomicCubeZ.set(0);
                    atomicCubeY.incrementAndGet();
                }
                if (atomicCubeY.get() >= resolution - 1) {
                    atomicCubeY.set(0);
                    atomicCubeX.incrementAndGet();
                }
            } finally {
                // Ensure the lock is released in a finally block
                lock.unlock();
            }
        }
    }

    public float read(int x, int y, int z) {
        return this.voxel_grid.get(x + y * resolution + z * resolution * resolution);
    }

    public void write(int x, int y, int z, float value) {
        this.voxel_grid.set(x + y * resolution + z * resolution * resolution, value);
    }

    // finds the mesh configuration corresponding to the current cube
    public int[] get_triangulation(int x, int y, int z) {
        int cube_index = 0;
        // each bit represents whether that corner should be inside or outside the mesh
        for (int i = 0; i < 8; i++) {
            if (read(x + TriTable.POINTS[i][0], y + TriTable.POINTS[i][1], z + TriTable.POINTS[i][2]) < TriTable.SURFACE_LEVEL) {
                cube_index |= 1 << i;
            }
        }
        return TriTable.TRIANGULATIONS[cube_index];
    }

    // this function determines the shape of the mesh
    public static float scalar_field(float x, float y, float z) {
        return (x * x + y * y + z * z) - 0.75f*0.75f;
        // return (float) (Math.sin(x) * Math.cos(y) + Math.sin(y) * Math.cos(z) + Math.sin(z) * Math.cos(x));
    }

    public ArrayList<Vector3f> create_grid() {
        int num_threads = 8;
        Thread[] threads = new Thread[num_threads];
        for (int i = 0; i < num_threads; i++) {
            threads[i] = new Thread(this, "Thread " + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join(); // wait for each thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<Vector3f>(positions);
    }

    // Tests the VoxelGrid class
    // Outputs the time it takes to run just the marching cubes algorithm at various resolutions
    public static void main(String[] args) {
        int resolution = 200;
        int num_threads = 4;
        if (args.length == 2) {
            String input = args[0];
            resolution = Integer.parseInt(input);
            String input2 = args[1];
            num_threads = Integer.parseInt(input2);
        }

        ParallelVoxelGrid example = new ParallelVoxelGrid(resolution);

        long start = System.nanoTime();

        Thread[] threads = new Thread[num_threads];
        for (int i = 0; i < num_threads; i++) {
            threads[i] = new Thread(example, "Thread " + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join(); // wait for each thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long end = System.nanoTime();

        System.out.println("Number of vertices: " + example.positions.size());
        System.out.println("Time (s): " + (end - start) / 1000000000.0 + "s");

    }
}
