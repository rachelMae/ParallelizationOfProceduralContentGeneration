package io.github.hunterherbst;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Worley3DThreaded {

    // CURRENTLY DIMENSIONS HAVE TO BE DIVISIBLE BY THREADS_X, THREADS_Y, THREADS_Z
    private static final int THREADS_X = 2;
    private static final int THREADS_Y = 2;
    private static final int THREADS_Z = 2;

    private int width;
    private int height;
    private int depth;
    private float[][][] data;
    private int numPoints;
    private int[][] points;
    private boolean wrap;

    public Worley3DThreaded(int width, int height, int depth, int numPoints, boolean wrap) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.numPoints = numPoints;
        this.points = new int[numPoints][3];
        this.data = new float[width][height][depth];
        for (int i = 0; i < numPoints; i++) {
            this.points[i][0] = (int) (Math.random() * width);
            this.points[i][1] = (int) (Math.random() * height);
            this.points[i][2] = (int) (Math.random() * depth);
        }

        this.wrap = wrap;


        // create threads
        GenerationThread[] threads = new GenerationThread[THREADS_X * THREADS_Y * THREADS_Z];
        int xStep = width / THREADS_X;
        int yStep = height / THREADS_Y;
        int zStep = depth / THREADS_Z;
        for (int i = 0; i < THREADS_X; i++) {
            for (int j = 0; j < THREADS_Y; j++) {
                for (int k = 0; k < THREADS_Z; k++) {
                    int startX = i * xStep;
                    int startY = j * yStep;
                    int startZ = k * zStep;
                    int endX = (i + 1) * xStep;
                    int endY = (j + 1) * yStep;
                    int endZ = (k + 1) * zStep;
                    threads[i * THREADS_X * THREADS_Y + j * THREADS_Y + k] = new GenerationThread(this, startX, startY, startZ, endX, endY, endZ);
                }
            }
        }

        // use executor service to run threads
        ExecutorService ex = Executors.newFixedThreadPool(THREADS_X * THREADS_Y * THREADS_Z);
        for (int i = 0; i < THREADS_X * THREADS_Y * THREADS_Z; i++) {
            ex.execute(threads[i]);
        }

        ex.shutdown();

        while(!ex.isTerminated()) {
        }

        this.normalizeData();

    }

    public Worley3DThreaded(int width, int height, int depth, int numPoints) {
        this(width, height, depth, numPoints, true);
    }

    private static float distance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
    }

    private static float distanceWrapped(int x1, int y1, int z1, int x2, int y2, int z2, int width, int height, int depth) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);
        if (dx > width / 2) {
            dx = width - dx;
        }
        if (dy > height / 2) {
            dy = height - dy;
        }
        if (dz > depth / 2) {
            dz = depth - dz;
        }
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static float taxiCabDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1) + Math.abs(z2 - z1);
    }

    private static float taxiCabDistanceWrapped(int x1, int y1, int z1, int x2, int y2, int z2, int width, int height, int depth) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);
        if (dx > width / 2) {
            dx = width - dx;
        }
        if (dy > height / 2) {
            dy = height - dy;
        }
        if (dz > depth / 2) {
            dz = depth - dz;
        }
        return dx + dy + dz;
    }

    public void generate(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        // generate the worley noise by calculating the distance from each point to each pixel
        // and setting the pixel to the closest point
        // the range for these numbers should be 0 to 1
        for (int z = startZ; z < endZ; z++) {
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    float minDistance = distance(x, y, z, this.points[0][0], this.points[0][1], this.points[0][2]);
                    for (int i = 1; i < this.numPoints; i++) {
                        float d = distance(x, y, z, this.points[i][0], this.points[i][1], this.points[i][2]);
                        if (d < minDistance) {
                            minDistance = d;
                        }
                    }
                    this.data[x][y][z] = minDistance;
                }
            }
        }
    }

    public void generateWithWrapping(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        // generate the worley noise by calculating the distance from each point to each pixel
        // and setting the pixel to the closest point
        // the range for these numbers should be 0 to 1
        for (int z = startZ; z < endZ; z++) {
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    float minDistance = distanceWrapped(x, y, z, this.points[0][0], this.points[0][1], this.points[0][2], this.width, this.height, this.depth);
                    for (int i = 1; i < this.numPoints; i++) {
                        float d = distanceWrapped(x, y, z, this.points[i][0], this.points[i][1], this.points[i][2], this.width, this.height, this.depth);
                        if (d < minDistance) {
                            minDistance = d;
                        }
                    }
                    this.data[x][y][z] = minDistance;
                }
            }
        }
    }

    public void regenerate() {
        points = new int[numPoints][3];
        for (int i = 0; i < numPoints; i++) {
            this.points[i][0] = (int) (Math.random() * width);
            this.points[i][1] = (int) (Math.random() * height);
            this.points[i][2] = (int) (Math.random() * depth);
        }

        // create threads
        GenerationThread[] threads = new GenerationThread[THREADS_X * THREADS_Y * THREADS_Z];
        int xStep = width / THREADS_X;
        int yStep = height / THREADS_Y;
        int zStep = depth / THREADS_Z;
        for (int i = 0; i < THREADS_X; i++) {
            for (int j = 0; j < THREADS_Y; j++) {
                for (int k = 0; k < THREADS_Z; k++) {
                    int startX = i * xStep;
                    int startY = j * yStep;
                    int startZ = k * zStep;
                    int endX = (i + 1) * xStep;
                    int endY = (j + 1) * yStep;
                    int endZ = (k + 1) * zStep;
                    threads[i * THREADS_X * THREADS_Y + j * THREADS_Y + k] = new GenerationThread(this, startX, startY, startZ, endX, endY, endZ);
                }
            }
        }

        // use executor service to run threads
        ExecutorService ex = Executors.newFixedThreadPool(THREADS_X * THREADS_Y * THREADS_Z);
        for (int i = 0; i < THREADS_X * THREADS_Y * THREADS_Z; i++) {
            ex.execute(threads[i]);
        }

        ex.shutdown();

        while(!ex.isTerminated()) {
        }

        this.normalizeData();


    }

    private void normalizeData() {
        float max = 0;
        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    if (this.data[x][y][z] > max) {
                        max = this.data[x][y][z];
                    }
                }
            }
        }
        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    this.data[x][y][z] /= max;
                }
            }
        }
    }
    public void invert() {
        for (int z = 0; z < this.depth; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    this.data[x][y][z] = 1 - this.data[x][y][z];
                }
            }
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getDepth() {
        return this.depth;
    }

    public float[][][] getData() {
        return this.data;
    }

    public float[][] getSlice(int z) {
        float[][] slice = new float[this.width][this.height];
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                slice[x][y] = this.data[x][y][z];
            }
        }
        return slice;
    }

    public PNG[] getPNGs() {
        // each layer of depth gets its own PNG of Width x Height
        PNG[] pngs = new PNG[this.depth];
        for (int z = 0; z < this.depth; z++) {
            pngs[z] = new PNG(this.width, this.height);
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    pngs[z].setPixel(x, y, new Pixel(this.data[x][y][z], this.data[x][y][z], this.data[x][y][z], 1));
                }
            }
        }
        return pngs;
    }

    static class GenerationThread implements Runnable {

        private Worley3DThreaded worley;
        private int startX;
        private int startY;
        private int startZ;
        private int endX;
        private int endY;
        private int endZ;

        public GenerationThread(Worley3DThreaded worley, int startX, int startY, int startZ, int endX, int endY, int endZ) {
            this.worley = worley;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
        }

        @Override
        public void run() {
            if (worley.wrap)
                worley.generateWithWrapping(startX, startY, startZ, endX, endY, endZ);
            else
                worley.generate(startX, startY, startZ, endX, endY, endZ);
        }
    }
}
