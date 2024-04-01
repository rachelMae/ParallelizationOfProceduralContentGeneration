package com.marching_cubes;

import java.util.ArrayList;

import org.joml.Vector3f;

/* Marching Cube Algorithm without multithreading */
public class VoxelGrid {
    public ArrayList<Float> voxel_grid; // 1-dimensional representation of a 3-dimensional grid

    public int resolution;

    public static final int SURFACE_LEVEL = 100;

    public VoxelGrid(int resolution) {
        this.voxel_grid = new ArrayList<Float>();

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

    public VoxelGrid(ArrayList<Float> voxel_grid, int resolution) {
        this.voxel_grid = voxel_grid;
        this.resolution = resolution;
    }

    public float read(int x, int y, int z) {
        return this.voxel_grid.get(x + y * resolution + z * resolution * resolution);
    }

    public void write(int x, int y, int z, float value) {
        this.voxel_grid.set(x + y * resolution + z * resolution * resolution, value);
    }

    // this function determines the shape of the mesh
    public static float scalar_field(float x, float y, float z) {
        return (x * x + y * y + z * z) - 0.75f*0.75f;
        // return (float) (Math.sin(x) * Math.cos(y) + Math.sin(y) * Math.cos(z) + Math.sin(z) * Math.cos(x));
    }

    // run the marching cubes algorithm for the entire grid (batch mode)
    public ArrayList<Vector3f> create_grid() {
        ArrayList<Vector3f> positions = new ArrayList<Vector3f>();
        for (int z = 0; z < this.resolution - 1; z++) {
            for (int y = 0; y < this.resolution - 1; y++) {
                for (int x = 0; x < this.resolution - 1; x++) {
                    march_cube(x, y, z,
                        this,
                        positions
                    );
                }
            }
        }
        return positions;
    }

    // takes a vertex position and a voxel grid and array of vertex positions
    // and appends the new positions to the array
    public static void march_cube(int x, int y, int z, VoxelGrid voxel_grid, ArrayList<Vector3f> positions) {
        int[] triangulation = get_triangulation(x, y, z, voxel_grid);

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
    }

    // takes a vertex position and a voxel grid and returns a list of vertex positions to render
    // using the triangulation table
    public static int[] get_triangulation(int x, int y, int z, VoxelGrid voxel_grid) {
        int cube_index = 0;
        // each bit represents whether that corner should be inside or outside the mesh
        for (int i = 0; i < 8; i++) {
            if (voxel_grid.read(x + TriTable.POINTS[i][0], y + TriTable.POINTS[i][1], z + TriTable.POINTS[i][2]) < TriTable.SURFACE_LEVEL) {
                cube_index |= 1 << i;
            }
        }
        return TriTable.TRIANGULATIONS[cube_index];
    }

    // Tests the VoxelGrid class
    // Outputs the time it takes to run just the marching cubes algorithm at various resolutions
    public static void main(String[] args) {
        int resolution = 200;
        if (args.length == 1) {
            resolution = Integer.parseInt(args[0]);
        }
        long start = System.nanoTime();
        VoxelGrid voxel_grid = new VoxelGrid(resolution);
        voxel_grid.create_grid();
        long end = System.nanoTime();
        System.out.println("Time (s): " + (end - start) / 1000000000.0 + "s");
    }
}
