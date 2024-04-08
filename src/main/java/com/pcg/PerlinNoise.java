package com.pcg;

public class PerlinNoise {

    private static final int P = 512;
    private int[] perm;

    public PerlinNoise() {
        // Initialize permutation array
        perm = new int[P];
        for (int i = 0; i < P; i++) {
            perm[i] = i;
        }

        // Shuffle permutation array
        for (int i = P - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            int temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x) {
        int h = hash & 15;
        double grad = 1 + (h & 7); // Gradient value 1-8
        if ((h & 8) != 0)
            grad = -grad; // Randomly invert half of them
        return (grad * x); // Multiply the gradient with the distance
    }

    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double gradX = 1 + (h & 7); // Gradient value for x (1-8)
        double gradY = 1 + ((h + 1) & 7); // Gradient value for y (1-8), ensuring it's different from gradX
        double gradZ = 1 + ((h + 2) & 7); // Gradient value for z (1-8), ensuring it's different from gradX and gradY

        if ((h & 8) != 0)
            gradX = -gradX; // Randomly invert half of them
        if (((h + 1) & 8) != 0)
            gradY = -gradY; // Randomly invert half of them
        if (((h + 2) & 8) != 0)
            gradZ = -gradZ; // Randomly invert half of them

        // Calculate the dot product of gradient and distance vectors
        return (gradX * (x - ((h & 1) == 0 ? 0 : 1)) + gradY * (y - ((h & 2) == 0 ? 0 : 1))
                + gradZ * (z - ((h & 4) == 0 ? 0 : 1)));
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 7; // Convert the hash code into 0-7 range
        double gradX = 1 + (h & 1) * 2 - 1; // Gradient value for x (-1 or 1)
        double gradY = 1 + (h >> 1 & 1) * 2 - 1; // Gradient value for y (-1 or 1)
        return (gradX * x + gradY * y); // Calculate the dot product of gradient and distance vectors
    }

    public double noise(double x) {
        int X = (int) Math.floor(x) & 255;
        x -= Math.floor(x);
        double u = fade(x);
        return lerp(u, grad(perm[X], x), grad(perm[X + 1], x - 1));
    }

    public double noise(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);
        int A = perm[X] + Y;
        int AA = perm[A];
        int AB = perm[A + 1];
        int B = perm[X + 1] + Y;
        int BA = perm[B];
        int BB = perm[B + 1];

        return lerp(v, lerp(u, grad(perm[AA], x, y), grad(perm[BA], x - 1, y)),
                lerp(u, grad(perm[AB], x, y - 1), grad(perm[BB], x - 1, y - 1)));
    }

    public double noise(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        int A = perm[X] + Y;
        int AA = perm[A] + Z;
        int AB = perm[A + 1] + Z;
        int B = perm[X + 1] + Y;
        int BA = perm[B] + Z;
        int BB = perm[(B + 1) & 255] + Z;

        return lerp(w, lerp(v, lerp(u, grad(perm[AA], x, y, z), grad(perm[BA], x - 1, y, z)),
                lerp(u, grad(perm[AB], x, y - 1, z), grad(perm[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(perm[(AA + 1) & 255], x, y, z - 1), grad(perm[(BA + 1) & 255], x - 1, y, z - 1)),
                        lerp(u, grad(perm[(AB + 1) & 255], x, y - 1, z - 1),
                                grad(perm[(BB + 1) & 255], x - 1, y - 1, z - 1))));
    }

    public static void main(String[] args) {
        PerlinNoise perlin = new PerlinNoise();

        // Example usage
        // System.out.println(perlin.noise(1.23));
        // System.out.println(perlin.noise(2.45, 3.67));
         System.out.println(perlin.noise(4.56, 7.89, 10.11));
    }
}
