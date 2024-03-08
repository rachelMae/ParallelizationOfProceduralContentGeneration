package com.marching_cubes;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.*;

public class ShaderUtils {
    public static String loadShaderSource(String filename) throws IOException {
        StringBuilder shaderSource = new StringBuilder();
        InputStream in = ClassLoader.getSystemResourceAsStream(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            shaderSource.append(line).append("\n");
        }
        reader.close();
        return shaderSource.toString();
    }

    public static int loadShader() {
        // shader program
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        try {
            String vertexShaderSource = ShaderUtils.loadShaderSource("com/marching_cubes/shaders/vertShader.glsl");
            String fragmentShaderSource = ShaderUtils.loadShaderSource("com/marching_cubes/shaders/fragShader.glsl");

            // Load and compile vertex shader
            glShaderSource(vertexShader, vertexShaderSource);
            glCompileShader(vertexShader);
            // String vertexCompileLog = glGetShaderInfoLog(vertexShader);
            // System.out.println("Vertex shader compilation log:\n" + vertexCompileLog);

            // Load and compile fragment shader
            glShaderSource(fragmentShader, fragmentShaderSource);
            glCompileShader(fragmentShader);
            // String fragmentCompileLog = glGetShaderInfoLog(fragmentShader);
            // System.out.println("Fragment shader compilation log:\n" + fragmentCompileLog);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        return shaderProgram;
    }

    public static int generateVAO() {
        int vao = glGenVertexArrays(); // generate a VAO ID
        glBindVertexArray(vao); // bind the VAO

        return vao;
    }

    public static int generateVBOFloat(float[] data) {
        int vbo_id = -1;
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stackMallocFloat(data.length);
            buffer.put(data);
            buffer.flip();
            vbo_id = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo_id);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        return vbo_id;
    }

    public static int generateVBOInt(int[] data) {
        int vbo_id = -1;
        try (MemoryStack stack = stackPush()) {
            IntBuffer buffer = stackMallocInt(data.length);
            buffer.put(data);
            buffer.flip();
            vbo_id = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo_id);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        return vbo_id;
    }


    public static void checkGLError() {
        int err = glGetError();

        while (err != GL_NO_ERROR) {
            String error;
            switch (err) {
                case GL_INVALID_OPERATION:
                    error = "INVALID_OPERATION";
                    break;
                case GL_INVALID_ENUM:
                    error = "INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    error = "INVALID_VALUE";
                    break;
                case GL_OUT_OF_MEMORY:
                    error = "OUT_OF_MEMORY";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    error = "INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_STACK_UNDERFLOW:
                    error = "STACK_UNDERFLOW";
                    break;
                case GL_STACK_OVERFLOW:
                    error = "STACK_OVERFLOW";
                    break;
                case GL_NO_ERROR:
                    error = "NO_ERROR";
                    break;
                default:
                    error = "UNKNOWN_ERROR";
                    break;
            }
            System.out.println("GL_" + error);
            err = glGetError();
        }
    }
}