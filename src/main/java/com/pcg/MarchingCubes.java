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
    public static int resolution = 512;

    public static void main(String[] args) {

        System.out.println("Please choose an option:\n 1. Worley Noise \n 2. Perlin Noise");
        Scanner reader = new Scanner(System.in);
        int noiseOption = reader.nextInt();

        while (noiseOption != 1 && noiseOption != 2) {
            System.out.println("Invalid option. Please choose an option:\n 1. Run marching cubes algorithm cube by cube. \n 2. Run marching cubes algorithm in batch mode.");
            noiseOption = reader.nextInt();
        }

        reader.close();
        int option = 2;

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
        glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        glClearDepth(1.0f);

        glEnable(GL_DEPTH_TEST);

        // Create a camera
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f position = new Vector3f(20.0f, 20.0f, 10.0f);
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

        // Create 3D Worley Noise
        MultithreadedVoxelGrid voxel_grid = null;
        if(noiseOption == 1) {
            System.out.println("Generating Worley Noise...");
            Worley3DThreaded worley = new Worley3DThreaded(resolution, resolution, resolution, 25);
            worley.invert();
            float[][][] worley_noise = worley.getData();
            voxel_grid = new MultithreadedVoxelGrid(worley_noise, resolution, 8); // Chunkify
        }
        else {
            // Create Perlin Noise
            PerlinNoiseGenerator png = new PerlinNoiseGenerator();
            float[][][] perlin = png.generatePerlinNoise3D(resolution, resolution, resolution, 0.1);
            voxel_grid = new MultithreadedVoxelGrid(perlin, resolution, 8); // Chunkify
        }

        Mesh mesh = new Mesh();
        ArrayList<Vector3f> positions = new ArrayList<Vector3f>();

        // run marching cubes algorithm
        long start = System.nanoTime();
        System.out.println("Marching cubes...");
        positions = voxel_grid.create_positions(); // for multithreaded
        // positions = voxel_grid.create_grid(); // for non-multithreaded
        long end = System.nanoTime();
        System.out.println("Time (s): " + (end - start) / 1000000000.0 + "s");
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
        // Initialize normals for each vertex
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


        int x = 0;
        int y = 0;
        int z = 0;

        System.out.println("Use WASD and Mouse Drag to move the camera. Press ESC to exit.");
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);


        // MAIN LOOP
        while (!glfwWindowShouldClose(window)) {
		    GL.createCapabilities();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

             //Draw wireframe cubes around each voxel
            /*
             for (int i = 0; i < resolution; i++) {
                 for (int j = 0; j < resolution; j++) {
                     for (int k = 0; k < resolution; k++) {
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
             */

            // Camera input processing
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
            if (camera.pressedKeys[GLFW_KEY_SPACE]) {
                camera.moveUp(1f);
            }
            if (camera.pressedKeys[GLFW_KEY_LEFT_SHIFT]) {
                camera.moveUp(-1f);
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