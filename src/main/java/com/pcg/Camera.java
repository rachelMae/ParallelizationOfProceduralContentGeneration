package com.pcg;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private Vector3f up;

    private float fov; // Field of view
    private float aspect; // Aspect ratio
    private float near; // Near plane
    private float far; // Far plane

    // Mouse movement variables
    public boolean mouseDragging = false;
    public boolean[] pressedKeys;
    private double lastX = 0.0;
    private double lastY = 0.0;
    private float yaw = 0.0f;
    private float pitch = 0.0f;

    public Camera(Vector3f position, Vector3f target, Vector3f up, float fov, float aspect, float near, float far, long window) {
        this.position = position;
        this.target = target;
        this.up = up;
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;

        this.pressedKeys = new boolean[GLFW_KEY_LAST];

        // Set up mouse callbacks
        glfwSetCursorPosCallback(window, this::mouseCallback);
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback);

        // Set up keyboard callbacks
        glfwSetKeyCallback(window, this::keyCallback);

        // Set up scroll callback
        glfwSetScrollCallback(window, this::mouseScrollCallback);
    }

    public Matrix4f getViewMatrix() {
        // Calculate camera/view matrix
        Matrix4f view = new Matrix4f()
            .rotate((float) Math.toRadians(pitch), new Vector3f(1.0f, 0.0f, 0.0f))
            .rotate((float) Math.toRadians(yaw), new Vector3f(0.0f, 1.0f, 0.0f))
            .translate(position.x, position.y, position.z);

        return view;
        // return new Matrix4f().lookAt(position, target, up);
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().perspective(fov, aspect, near, far);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setTarget(Vector3f target) {
        this.target.set(target);
    }

    public void setUp(Vector3f up) {
        this.up.set(up);
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        // System.out.println("Key: " + key + " Action: " + action + " Mods: " + mods);
        pressedKeys[key] = action != GLFW_RELEASE;
    }

    public void mouseCallback(long window, double xpos, double ypos) {
        //if (mouseDragging) {
            float sensitivity = 0.1f;
            float xoffset = (float) (xpos - lastX) * sensitivity;
            float yoffset = (float) (lastY - ypos) * sensitivity;  // Reversed since y-coordinates go from bottom to top
            lastX = xpos;
            lastY = ypos;

            yaw += xoffset;
            pitch -= yoffset;

            // Clamp pitch
            if (pitch > 89.0f) pitch = 89.0f;
            if (pitch < -89.0f) pitch = -89.0f;
        //}
    }

    public void mouseButtonCallback(long window, int button, int action, int mods) {
//        if (button == GLFW_MOUSE_BUTTON_LEFT) {
//            if (action == GLFW_PRESS) {
//                mouseDragging = true;
//                double[] xpos = new double[1];
//                double[] ypos = new double[1];
//                glfwGetCursorPos(window, xpos, ypos);
//                lastX = xpos[0];
//                lastY = ypos[0];
//            } else if (action == GLFW_RELEASE) {
//                mouseDragging = false;
//            }
//        }
    }

    public void mouseScrollCallback(long window, double xoffset, double yoffset) {
        // adjust fov based on scroll
        System.out.println("Scroll: " + yoffset);
        fov -= yoffset * 0.1f;
        if (fov < 1.0f) fov = 1.0f;
        if (fov > 90.0f) fov = 90.0f;
    }

    public void yaw(float amount)
	{
	    //increment the yaw by the amount param
	    yaw += amount;
	}

	//increment the camera's current yaw rotation
	public void pitch(float amount)
	{
	    //increment the pitch by the amount param
	    pitch += amount;
	}

	public void moveLeft(float distance)
	{
        position.x += distance * (float)Math.sin(Math.toRadians(yaw+90));
        position.z -= distance * (float)Math.cos(Math.toRadians(yaw+90));
	}

	//strafes the camera left relitive to its current rotation (yaw)
	public void moveForward(float distance)
	{
        position.x -= distance * (float)Math.sin(Math.toRadians(yaw));
        position.z += distance * (float)Math.cos(Math.toRadians(yaw));
	}

    public void moveUp(float distance)
    {
        //moves the camera up or down relitive to its current rotation (yaw)
        position.y -= distance;
    }

}
