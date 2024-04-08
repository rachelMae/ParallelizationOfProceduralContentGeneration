package com.pcg;

/**
 * Class to represent a single pixel in an image.
 * Format is RGBA.
 */
public class Pixel {
    private float r, g, b, a;

    public Pixel(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Pixel(int rgb) {
        this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, (rgb >> 24) & 0xFF);
    }

    public Pixel(float r, float g, float b) {
        this(r, g, b, 1);
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public int getRGBA() {
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        int alpha = (int) (a * 255);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public String toString() {
        return "Pixel{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }
}
