package com.marching_cubes;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31C.glGetActiveUniformName;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.*;

import static com.marching_cubes.ShaderUtils.*;

public class CubeRenderer {
    // Vertices for a unit cube centered at the origin
    private static final float[] positions = new float[] {
        // VO
        -0.5f,  0.5f,  0.5f,
        // V1
        -0.5f, -0.5f,  0.5f,
        // V2
        0.5f, -0.5f,  0.5f,
        // V3
         0.5f,  0.5f,  0.5f,
        // V4
        -0.5f,  0.5f, -0.5f,
        // V5
         0.5f,  0.5f, -0.5f,
        // V6
        -0.5f, -0.5f, -0.5f,
        // V7
         0.5f, -0.5f, -0.5f,
    };

    private static final float[] colors = new float[]{
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
    };


    // Indices to define triangles for rendering a cube
    private static final int[] indices = new int[] {
        // Front face
        0, 1, 3, 3, 1, 2,
        // Top Face
        4, 0, 3, 5, 4, 3,
        // Right face
        3, 2, 7, 5, 3, 7,
        // Left face
        6, 1, 0, 6, 0, 4,
        // Bottom face
        2, 1, 6, 2, 6, 7,
        // Back face
        7, 6, 4, 7, 4, 5,
    };

    // Vertex buffer object (VBO) ID
    private int posVboId;

    private int colorVboId;

    // Vertex array object (VAO) ID
    private int vao;

    // Index VBO
    private int idxVboId;

    // Shader program ID
    private int shaderProgram;

    private Camera camera;

    private int element_count;

    // Constructor
    public CubeRenderer(long window) {
        shaderProgram = loadShader();

        // Generate VAO
        vao = generateVAO();

        posVboId = generateVBOFloat(positions);
        // Enable the position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        colorVboId = generateVBOFloat(colors);
        // Enable the color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        idxVboId = generateVBOInt(indices);

        glBindVertexArray(0);

        /* DEBUGGING START */
        // Get the total number of active uniforms in the shader program
        // int numActiveUniforms = glGetProgrami(shaderProgram, GL_ACTIVE_UNIFORMS);
        // System.out.println("  numActiveUniforms: " + numActiveUniforms);

        // // Buffer to hold uniform information
        // IntBuffer sizeBuffer = BufferUtils.createIntBuffer(1);
        // IntBuffer typeBuffer = BufferUtils.createIntBuffer(1);
        // for (int i = 0; i < numActiveUniforms; i++) {
        //     // Get information about the i-th uniform
        //     glGetActiveUniform(shaderProgram, i, sizeBuffer, typeBuffer);

        //     // Get the size and type of the uniform
        //     int size = sizeBuffer.get(0);
        //     int type = typeBuffer.get(0);

        //     // Get the name of the uniform
        //     String name = glGetActiveUniformName(shaderProgram, i);

        //     // Print information about the uniform
        //     System.out.println("Uniform " + i + ":");
        //     System.out.println("  Name: " + name);
        //     System.out.println("  Size: " + size);
        //     System.out.println("  Type: " + type);
        // }
        /* DEBUGGING END */

        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f position = new Vector3f(3.0f, 3.0f, 3.0f);
        Vector3f target = new Vector3f(0.0f, 0.0f, 0.0f);
        float aspect = 800.0f / 600.0f;
        float fov = (float) Math.toRadians(45.0f);
        float near = 0.1f;
        float far = 100.0f;
        camera = new Camera(position, target, up, fov, aspect, near, far, window);

        // Initialize view and projection matrices
        int viewMatrixLocation = glGetUniformLocation(shaderProgram, "view");
        System.out.println("View matrix location: " + viewMatrixLocation);
        Matrix4f viewMatrix = camera.getViewMatrix();
        glUniformMatrix4fv(viewMatrixLocation, false, viewMatrix.get(new float[16]));

        System.out.println("Set view matrix successfully!");
        checkGLError();

        int projectionMatrixLocation = glGetUniformLocation(shaderProgram, "projection");
        System.out.println("Projection matrix location: " + projectionMatrixLocation);
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrix.get(new float[16]));

        System.out.println("Set projection matrix successfully!");
        checkGLError();
    }

    // Render method
    public void render() {
        glUseProgram(shaderProgram);
        // Bind VAO
        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Set the camera position
        if (camera.mouseDragging) {
            int viewMatrixLocation = glGetUniformLocation(shaderProgram, "view");
            Matrix4f viewMatrix = camera.getViewMatrix();
            glUniformMatrix4fv(viewMatrixLocation, false, viewMatrix.get(new float[16]));
            checkGLError();

            int projectionMatrixLocation = glGetUniformLocation(shaderProgram, "projection");
            Matrix4f projectionMatrix = camera.getProjectionMatrix();
            glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrix.get(new float[16]));
            checkGLError();

            System.out.println("Set view and projection matrices successfully!");
        }
        // Calculate translation for each cube
        float x = 0 * 2.0f; // Adjust as needed
        float y = 0.0f;     // Adjust as needed
        float z = 0.0f;     // Adjust as needed

        // Set the translation matrix
        Matrix4f translationMatrix = new Matrix4f().translate(x, y, z);

        // Set scale matrix
        Matrix4f scaleMatrix = new Matrix4f().scale(1.0f);

        // Set rotation matrix
        Matrix4f rotationMatrix = new Matrix4f().identity();
        // Rotate the cube around the y-axis
        rotationMatrix.rotate((float) Math.toRadians(90.0f), new Vector3f(0.0f, 1.0f, 0.0f));

        // Create model matrix
        Matrix4f modelMatrix = new Matrix4f().identity().mul(translationMatrix).mul(scaleMatrix);
        System.out.println("Model matrix: " + modelMatrix.toString());


        System.out.println("Set translation matrix successfully!");
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        System.out.println("Created matrix buffer successfully!");
        modelMatrix.get(matrixBuffer);
        System.out.println("Got matrix buffer successfully!");

        // Upload the translation matrix to the shader
        int matrixLocation = glGetUniformLocation(shaderProgram, "model");
        System.out.println("Got matrix location successfully! " + matrixLocation);
        glUniformMatrix4fv(matrixLocation, false, matrixBuffer);
        checkGLError();
        System.out.println("Uploaded matrix to shader successfully!");

        // Render cube
        // glDrawArrays(GL_TRIANGLES, 0, element_count);
        checkGLError();
        glDrawElements(GL_TRIANGLES, positions.length / 3, GL_UNSIGNED_INT, 0);
        checkGLError();
        System.out.println("Rendered cube successfully!");


        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        // Unbind VAO
        glBindVertexArray(0);
    }


    // Cleanup method
    public void cleanup() {
        glDisableVertexAttribArray(0);
        // Delete VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(colorVboId);
        glDeleteBuffers(idxVboId);
        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
    }
}

