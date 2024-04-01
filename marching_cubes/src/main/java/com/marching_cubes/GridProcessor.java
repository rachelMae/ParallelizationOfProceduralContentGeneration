package com.marching_cubes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector3f;

// Java code for thread creation by implementing
// the Runnable Interface
class GridProcessor extends VoxelGrid implements Runnable {
    private int row_index;
    private ArrayList<Vector3f> positions = new ArrayList<Vector3f>();

    public GridProcessor(ArrayList<Float> voxel_grid, int resolution, int row_index) {
        super(voxel_grid, resolution);
        this.row_index = row_index;
    }

    public void run()
    {
        try {
            // Displaying the thread that is running
            // System.out.println(
            //     "Thread " + Thread.currentThread().getId()
            //     + " is running");

            // Initialize positions
            for (int z = this.row_index; z < this.row_index + 1; z++) {
                for (int y = 0; y < this.resolution - 1; y++) {
                    for (int x = 0; x < this.resolution - 1; x++) {
                        march_cube(x, y, z,
                            this,
                            positions
                        );
                    }
                }
            }
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println("Exception is caught: " + e);
        }
    }

    public ArrayList<Vector3f> get_positions() {
        return this.positions;
    }

    public int get_row_index() {
        return this.row_index;
    }
}

// Main Class
class MultithreadedVoxelGrid {
    private int resolution;
    private ArrayList<Float> voxel_grid;
    private ArrayList<Vector3f> positions = new ArrayList<Vector3f>();
    private int num_threads;
    public MultithreadedVoxelGrid(int resolution, int num_threads) {
        this.resolution = resolution;
        this.num_threads = num_threads;
        // create a sample voxel grid
        this.voxel_grid = new ArrayList<Float>();
        for (int z = 0; z < resolution; z++) {
            for (int y = 0; y < resolution; y++) {
                for (int x = 0; x < resolution; x++) {
                    voxel_grid.add(VoxelGrid.scalar_field((float)x, (float)y, (float)z));
                }
            }
        }
    }

    public ArrayList<Vector3f> create_grid() {
        // create threads
        Thread[] threads = new Thread[num_threads];
        GridProcessor[] grid_processors = new GridProcessor[num_threads];
        HashMap<Thread, GridProcessor> thread_to_grid_processor = new HashMap<Thread, GridProcessor>();

        for (int i = 0; i < num_threads; i++) {
            GridProcessor grid_processor = new GridProcessor(voxel_grid, resolution, i);
            grid_processors[i] = grid_processor;
            Thread object = new Thread(grid_processor);
            threads[i] = object;
            thread_to_grid_processor.put(object, grid_processor);
            object.start();
        }

        // wrap up threads
        for (Thread thread : threads) {
            try {
                thread.join(); // wait for each thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // display each thread's positions
                GridProcessor grid_processor = thread_to_grid_processor.get(thread);
                // merge in thread's positions to main positions
                positions.addAll(grid_processor.get_positions());
                // ArrayList<Vector3f> positions = grid_processor.get_positions();
                // for (int i = 0; i < positions.size(); i++) {
                //     System.out.println("Thread " + grid_processor.get_row_index() + ": " + positions.get(i));
                // }
            }
        }

        return this.positions;
    }

    public static void main(String[] args) {
        int resolution = 1000;
        int num_threads = 32;
        if (args.length == 2) {
            String input = args[0];
            resolution = Integer.parseInt(input);
            String input2 = args[1];
            num_threads = Integer.parseInt(input2);
        }

        MultithreadedVoxelGrid example = new MultithreadedVoxelGrid(resolution, num_threads);

        long start = System.nanoTime();
        System.out.println("Starting voxel grid creation with resolution " + resolution + " and " + num_threads + " threads");

        example.create_grid();

        long end = System.nanoTime();

        System.out.println("Number of vertices: " + example.positions.size());
        System.out.println("Time (s): " + (end - start) / 1000000000.0 + "s");
    }
}