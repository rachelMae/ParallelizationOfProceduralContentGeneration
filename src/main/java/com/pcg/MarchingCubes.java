package com.pcg;

import org.lwjgl.opengl.GL;

import static com.pcg.ShaderUtils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.util.ArrayList;
import java.util.Scanner;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MarchingCubes {
    public static int resolution = 128;
    public static int num_threads = 8;
    public static boolean wireframe_enabled = false;

    public static void main(String[] args) {
        /** GET USER INPUT */
        System.out.println("Please choose a generation method:\n 1. Worley Noise \n 2. Perlin Noise");
        Scanner reader = new Scanner(System.in);
        int option = reader.nextInt();

        while (option != 1 && option != 2) {
            System.out.println("Invalid option. Please choose an option:\n 1. Worley Noise \n 2. Perlin Noise");
            option = reader.nextInt();
        }
        reader.nextLine();

        System.out.println("Enter the resolution of the grid or press Enter for default (128): ");
        String res_input = reader.nextLine();
        if (!res_input.equals("")) {
            int input_resolution = Integer.parseInt(res_input);
            if (input_resolution > 0) {
                resolution = input_resolution;
            }
        }

        System.out.println("Enter the number of threads for Marching Cubes or press Enter for default (8): ");
        String thread_input = reader.nextLine();
        if (!thread_input.equals("")) {
            int input_threads = Integer.parseInt(thread_input);
            if (input_threads > 0) {
                num_threads = input_threads;
            }
        }

        // System.out.println("Enable wireframe (y/n) or press Enter for default (n): ");
        // String wireframe_input = reader.nextLine();
        // if (wireframe_input.equals("y") || wireframe_input.equals("Y")) {
        //     wireframe_enabled = true;
        // } else {
        //     wireframe_enabled = false;
        // }

        reader.close();

        if (!glfwInit()) {
            System.exit(1);
        }

        long window = glfwCreateWindow(1280, 720, "LWJGL 3D Example", 0, 0);
        if (window == 0) {
            throw new IllegalStateException("Failed to create window");
        }

        glfwMakeContextCurrent(window);

        // enable v-sync
        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();
        // background color
        glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        glClearDepth(1.0f);

        glEnable(GL_DEPTH_TEST);

        // Create a camera
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f target = new Vector3f(0.0f, 0.0f, 0.0f);
        float aspect = 800.0f / 600.0f;
        float fov = (float) Math.toRadians(45.0f);
        float near = 0.001f;
        float far = 1000.0f;
        Camera camera = new Camera(position, target, up, fov, aspect, near, far, window);
        camera.yaw(-240.0f);
        camera.pitch(-40.0f);

        // Create a shader
        int shaderProgram = loadShader();

        // Initialize lighting
        int lightLocation = glGetUniformLocation(shaderProgram, "lightPos");
        glUniform3fv(lightLocation, new float[] {
            0.0f, 0.0f, 0.0f
        });

        // Initialize view and projection matrices
        int viewMatrixLocation = glGetUniformLocation(shaderProgram, "view");
        Matrix4f viewMatrix = camera.getViewMatrix();
        glUniformMatrix4fv(viewMatrixLocation, false, viewMatrix.get(new float[16]));

        int projectionMatrixLocation = glGetUniformLocation(shaderProgram, "projection");
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrix.get(new float[16]));

        /** NOISE GENERATION METHODS */
        float[][][] noise;
        if (option == 1) {
            // Create 3D Worley Noise
            System.out.println("Generating Worley Noise...");
            long start = System.nanoTime();

            Worley3DThreaded worley = new Worley3DThreaded(resolution, resolution, resolution, 8);
            worley.invert();
            noise = worley.getData();

            long end = System.nanoTime();
            System.out.println("Time to generate Worley Noise (s): " + (end - start) / 1000000000.0 + "s");
        } else {
            // Create Perlin Noise
            long start = System.nanoTime();

            PerlinNoiseGenerator png = new PerlinNoiseGenerator();
            noise = png.generatePerlinNoise3D(resolution, resolution, resolution, 0.1);

            long end = System.nanoTime();
            System.out.println("Time to generate Perlin Noise (s): " + (end - start) / 1000000000.0 + "s");
        }

        // NON-multithreaded
        // VoxelGrid voxel_grid = new VoxelGrid(resolution); // uses scalar field to generate vertices
        // VoxelGrid voxel_grid = new VoxelGrid(noise, resolution); // uses noise to generate vertices

        // MULTITHREADED
        MultithreadedVoxelGrid voxel_grid = new MultithreadedVoxelGrid(noise, resolution, num_threads); // Chunkify (FASTER)
        // ParallelVoxelGrid voxel_grid = new ParallelVoxelGrid(noise, resolution); // Shared Counter (SLOW)

        // Create the mesh
        Mesh mesh = new Mesh();
        ArrayList<Vector3f> positions = new ArrayList<Vector3f>();

        /** START MARCHING CUBES TIMER */
        long start = System.nanoTime();
        System.out.println("Marching cubes...");

        // Run the marching cubes algorithm
        positions = voxel_grid.create_positions();

        long end = System.nanoTime();
        System.out.println("Time to complete Marching Cubes (s): " + (end - start) / 1000000000.0 + "s");
        System.out.println("Number of vertices: " + positions.size());

        // convert positions to float array
        float[] vertices = new float[positions.size() * 3];
        for (int i = 0; i < positions.size(); i++) {
            vertices[i * 3] = positions.get(i).x;
            vertices[i * 3 + 1] = positions.get(i).y;
            vertices[i * 3 + 2] = positions.get(i).z;
        }

        // calculate face normals for each triangle in the mesh
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
        // initialize normals for each vertex
        for (int i = 0; i < positions.size(); i++) {
            normals.add(new Vector3f());
        }
        for (int i = 0; i < positions.size(); i += 3) {
            Vector3f v0 = positions.get(i);
            Vector3f v1 = positions.get(i + 1);
            Vector3f v2 = positions.get(i + 2);

            Vector3f edge1 = new Vector3f();
            v1.sub(v0, edge1);

            Vector3f edge2 = new Vector3f();
            v2.sub(v0, edge2);

            Vector3f normal = new Vector3f();
            edge1.cross(edge2, normal).normalize();

            // Add face normal to each vertex
            normals.get(i).add(normal);
            normals.get(i + 1).add(normal);
            normals.get(i + 2).add(normal);
        }

        float[] normalArray = new float[normals.size() * 3];
        for (int i = 0; i < normals.size(); i++) {
            normalArray[i * 3] = normals.get(i).x;
            normalArray[i * 3 + 1] = normals.get(i).y;
            normalArray[i * 3 + 2] = normals.get(i).z;
        }

        mesh.updateVertices(vertices);
        mesh.updateColors(vertices);
        mesh.updateNormals(normalArray);

        System.out.println("Use WASD and Mouse Drag to move the camera. Press ESC to exit.");

        // MAIN LOOP
        while (!glfwWindowShouldClose(window)) {
		    GL.createCapabilities();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Draw wireframe cubes around each voxel
            if (wireframe_enabled) {
                for (int i = 0; i < resolution - 1; i++) {
                    for (int j = 0; j < resolution - 1; j++) {
                        for (int k = 0; k < resolution - 1; k++) {
                            // Calculate voxel position
                            float voxelSize = 1.0f;
                            float voxelSpacing = 1.0f; // Adjust as needed
                            float xPos = i * voxelSpacing;
                            float yPos = j * voxelSpacing;
                            float zPos = k * voxelSpacing;

                            // Draw wireframe cube
                            drawWireframeCube(xPos, yPos, zPos, voxelSize);
                        }
                    }
                }
            }

            // Set the camera position
            if (camera.pressedKeys[GLFW_KEY_W]) {
                camera.moveForward(1f);
            }
            if (camera.pressedKeys[GLFW_KEY_S]) {
                camera.moveForward(-1f);
            }
            if (camera.pressedKeys[GLFW_KEY_A]) {
                camera.moveLeft(1f);
            }
            if (camera.pressedKeys[GLFW_KEY_D]) {
                camera.moveLeft(-1f);
            }
            if(camera.pressedKeys[GLFW_KEY_ESCAPE]) {
                glfwSetWindowShouldClose(window, true);
            }

            viewMatrix = camera.getViewMatrix();
            glUniformMatrix4fv(viewMatrixLocation, false, viewMatrix.get(new float[16]));

            projectionMatrix = camera.getProjectionMatrix();
            glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrix.get(new float[16]));

            glUniform3fv(lightLocation, new float[] {
                camera.getPosition().x, camera.getPosition().y, camera.getPosition().z
            });

            mesh.render(shaderProgram);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // clean up
        glfwDestroyWindow(window);
        mesh.cleanup();

        glfwTerminate();
    }

    // Method to draw wireframe cube
    private static void drawWireframeCube(float x, float y, float z, float size) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Render in wireframe mode
        glBegin(GL_QUADS);

        // Front face
        glVertex3f(x - size / 2, y - size / 2, z + size / 2);
        glVertex3f(x + size / 2, y - size / 2, z + size / 2);
        glVertex3f(x + size / 2, y + size / 2, z + size / 2);
        glVertex3f(x - size / 2, y + size / 2, z + size / 2);

        // Back face
        glVertex3f(x - size / 2, y - size / 2, z - size / 2);
        glVertex3f(x + size / 2, y - size / 2, z - size / 2);
        glVertex3f(x + size / 2, y + size / 2, z - size / 2);
        glVertex3f(x - size / 2, y + size / 2, z - size / 2);

        // Top face
        glVertex3f(x - size / 2, y + size / 2, z - size / 2);
        glVertex3f(x + size / 2, y + size / 2, z - size / 2);
        glVertex3f(x + size / 2, y + size / 2, z + size / 2);
        glVertex3f(x - size / 2, y + size / 2, z + size / 2);

        // Bottom face
        glVertex3f(x - size / 2, y - size / 2, z - size / 2);
        glVertex3f(x + size / 2, y - size / 2, z - size / 2);
        glVertex3f(x + size / 2, y - size / 2, z + size / 2);
        glVertex3f(x - size / 2, y - size / 2, z + size / 2);

        // Left face
        glVertex3f(x - size / 2, y - size / 2, z - size / 2);
        glVertex3f(x - size / 2, y - size / 2, z + size / 2);
        glVertex3f(x - size / 2, y + size / 2, z + size / 2);
        glVertex3f(x - size / 2, y + size / 2, z - size / 2);

        // Right face
        glVertex3f(x + size / 2, y - size / 2, z - size / 2);
        glVertex3f(x + size / 2, y - size / 2, z + size / 2);
        glVertex3f(x + size / 2, y + size / 2, z + size / 2);
        glVertex3f(x + size / 2, y + size / 2, z - size / 2);

        glEnd();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Restore to fill mode
    }
}