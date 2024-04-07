package io.github.hunterherbst;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorleyThreaded {

    // CURRENTLY DIMENSIONS HAVE TO BE DIVISIBLE BY THREADS_X, THREADS_Y, THREADS_Z
    private static final int THREADS_X = 4;
    private static final int THREADS_Y = 4;

    private int width;
    private int height;
    private float[][] data;
    private int numPoints;
    private int[][] points;
    private boolean wrap;

    public WorleyThreaded(int width, int height, int numPoints, boolean wrap) {
        this.width = width;
        this.height = height;
        this.numPoints = numPoints;
        this.points = new int[numPoints][2];
        this.data = new float[width][height];
        for (int i = 0; i < numPoints; i++) {
            this.points[i][0] = (int) (Math.random() * width);
            this.points[i][1] = (int) (Math.random() * height);
        }

        this.wrap = wrap;


//        if (wrap)
//            this.generateWithWrapping();
//        else
//            this.generate();

        // create threads
        GenerationThread[] threads = new GenerationThread[THREADS_X * THREADS_Y];
        int xStep = width / THREADS_X;
        int yStep = height / THREADS_Y;
        for (int i = 0; i < THREADS_X; i++) {
            for (int j = 0; j < THREADS_Y; j++) {
                int startX = i * xStep;
                int startY = j * yStep;
                int endX = (i + 1) * xStep;
                int endY = (j + 1) * yStep;
                threads[i * THREADS_X + j] = new GenerationThread(this, startX, startY, endX, endY);
            }
        }

        // use executor service to run threads
        ExecutorService ex = Executors.newFixedThreadPool(THREADS_X * THREADS_Y);
        for (int i = 0; i < THREADS_X * THREADS_Y; i++) {
            ex.execute(threads[i]);
        }

        ex.shutdown();

        while(!ex.isTerminated()) {
            // wait for all threads to finish
        }

        this.normalizeData();

    }

    public WorleyThreaded(int width, int height, int numPoints) {
        this(width, height, numPoints, true);
    }

    // create new worley from just data set
    public WorleyThreaded(float[][] data, int numPoints, int[][] points) {
        this.width = data.length;
        this.height = data[0].length;
        this.numPoints = numPoints;
        this.points = points;
        this.data = data;
    }

    private static float distance(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private static float distanceWrapped(int x1, int y1, int x2, int y2, int width, int height) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx > width / 2) {
            dx = width - dx;
        }
        if (dy > height / 2) {
            dy = height - dy;
        }
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float taxiCabDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    private static float taxiCabDistanceWrapped(int x1, int y1, int x2, int y2, int width, int height) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx > width / 2) {
            dx = width - dx;
        }
        if (dy > height / 2) {
            dy = height - dy;
        }
        return dx + dy;
    }

    public void generate(int startX, int startY, int endX, int endY) {
        // generate worley noise in the given range
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                float minDistance = distance(x, y, this.points[0][0], this.points[0][1]);
                for (int i = 1; i < this.numPoints; i++) {
                    float d = distance(x, y, this.points[i][0], this.points[i][1]);
                    if (d < minDistance) {
                        minDistance = d;
                    }
                }
                this.data[x][y] = minDistance;
            }
        }
    }

    public void generateWithWrapping(int startX, int startY, int endX, int endY) {
        for(int y = startY; y < endY; y++) {
            for(int x = startX; x < endX; x++) {
                float minDistance = distanceWrapped(x, y, this.points[0][0], this.points[0][1], this.width, this.height);
                for(int i = 1; i < this.numPoints; i++) {
                    float d = distanceWrapped(x, y, this.points[i][0], this.points[i][1], this.width, this.height);
                    if(d < minDistance) {
                        minDistance = d;
                    }
                }
                this.data[x][y] = minDistance;
            }
        }
    }

    public void regenerate() {
        this.points = new int[numPoints][2];
        for (int i = 0; i < numPoints; i++) {
            this.points[i][0] = (int) (Math.random() * width);
            this.points[i][1] = (int) (Math.random() * height);
        }

        // create threads
        GenerationThread[] threads = new GenerationThread[THREADS_X * THREADS_Y];
        int xStep = width / THREADS_X;
        int yStep = height / THREADS_Y;
        for (int i = 0; i < THREADS_X; i++) {
            for (int j = 0; j < THREADS_Y; j++) {
                int startX = i * xStep;
                int startY = j * yStep;
                int endX = (i + 1) * xStep;
                int endY = (j + 1) * yStep;
                threads[i * THREADS_X + j] = new GenerationThread(this, startX, startY, endX, endY);
            }
        }

        // use executor service to run threads
        ExecutorService ex = Executors.newFixedThreadPool(THREADS_X * THREADS_Y);
        for (int i = 0; i < THREADS_X * THREADS_Y; i++) {
            ex.execute(threads[i]);
        }

        ex.shutdown();

        while(!ex.isTerminated()) {
            // wait for all threads to finish
        }

        this.normalizeData();
    }

    private void normalizeData() {
        float max = 0;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (this.data[x][y] > max) {
                    max = this.data[x][y];
                }
            }
        }
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                this.data[x][y] /= max;
            }
        }
    }

    public void invert() {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                this.data[x][y] = 1 - this.data[x][y];
            }
        }
    }

    public void setWrapping(boolean wrap) {
        this.wrap = wrap;
    }

    public boolean getWrapping() {
        return this.wrap;
    }

    public float[][] getData() {
        return this.data;
    }

    public int[][] getPoints() {
        return this.points;
    }

    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }

    public BufferedImage toBufferedImage() {
        BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int c = (int) (this.data[x][y] * 255);
                img.setRGB(x, y, (c << 16) | (c << 8) | c);
            }
        }
        return img;
    }

    public PNG getPNG() {
        PNG p = new PNG(this.width, this.height);
        p.setRedChannel(this.data);
        p.setGreenChannel(this.data);
        p.setBlueChannel(this.data);
        return p;
    }

    static class GenerationThread implements Runnable {
        private final WorleyThreaded worley;
        private final int startX;
        private final int startY;
        private final int endX;
        private final int endY;

        public GenerationThread(WorleyThreaded worley, int startX, int startY, int endX, int endY) {
            this.worley = worley;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        @Override
        public void run() {
            if(worley.getWrapping())
                worley.generateWithWrapping(startX, startY, endX, endY);
            else
                worley.generate(startX, startY, endX, endY);
        }
    }

}
