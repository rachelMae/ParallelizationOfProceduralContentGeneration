package com.marching_cubes;

import static com.marching_cubes.ShaderUtils.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Mesh {
    public float[] vertices;
    public float[] colors;
    public int[] indices;

    public int vao;
    public int posVboId = -1;
    public int colorVboId = -1;
    public int idxVboId = -1;

    public Mesh() {
        // Generate VAO
        vao = generateVAO();
    }

    public void updateVertices(float[] newVertices) {
        // Update the vertices array
        this.vertices = newVertices;

        // Bind the VAO
        glBindVertexArray(vao);

        // Delete the old VBO
        if (posVboId != -1) {
            glDeleteBuffers(posVboId);
        }

        // Create a new VBO with the updated vertices
        posVboId = generateVBOFloat(vertices);

        // Enable the position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // Unbind the VAO
        glBindVertexArray(0);
    }

    public void updateColors(float[] colors) {
        // Update the colors array
        this.colors = colors;

        // Bind the VAO
        glBindVertexArray(vao);

        // Delete the old VBO
        if (colorVboId != -1) {
            glDeleteBuffers(colorVboId);
        }

        // Create a new VBO with the updated colors
        colorVboId = generateVBOFloat(colors);

        // Enable the color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        // Unbind the VAO
        glBindVertexArray(0);
    }

    public void render(int shaderProgram) {
        glUseProgram(shaderProgram);
        // Bind VAO
        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Calculate translation for each vertex
        float x = 0 * 2.0f;
        float y = 0.0f;
        float z = 0.0f;

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

        // Create buffer for model matrix
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        modelMatrix.get(matrixBuffer);

        // Upload the model matrix to the shader
        int matrixLocation = glGetUniformLocation(shaderProgram, "model");
        glUniformMatrix4fv(matrixLocation, false, matrixBuffer);

        // Render cube
        glDrawArrays(GL_TRIANGLES, 0, vertices.length);

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
        if (posVboId != -1)
            glDeleteBuffers(posVboId);
        if (colorVboId != -1)
            glDeleteBuffers(colorVboId);
        if (idxVboId != -1)
            glDeleteBuffers(idxVboId);
        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
    }
}
