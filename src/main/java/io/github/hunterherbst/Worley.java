package io.github.hunterherbst;

public class Worley {

    private int width;
    private int height;
    private float[][] data;
    private int numPoints;
    private int[][] points;
    private boolean wrap;

    public Worley(int width, int height, int numPoints, boolean wrap) {
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

        if (wrap)
            this.generateWithWrapping();
        else
            this.generate();
    }

    public Worley(int width, int height, int numPoints) {
        this(width, height, numPoints, true);
    }

    // create new worley from just data set
    public Worley(float[][] data, int numPoints, int[][] points) {
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

    public void generate() {
        // generate the worley noise by calculating the distance from each point to each pixel
        // and setting the pixel to the closest point
        // the range for these numbers should be 0 to 1
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
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

        // normalize
        this.normalizeData();
    }

    public void generateWithWrapping() {
        // generate the worley noise by calculating the distance from each point to each pixel
        // and setting the pixel to the closest point
        // the range for these numbers should be 0 to 1
        // the difference is that this now should be wrapping around the edges
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                float minDistance = distance(x, y, this.points[0][0], this.points[0][1]);
                for (int i = 1; i < this.numPoints; i++) {
                    float d = distanceWrapped(x, y, this.points[i][0], this.points[i][1], this.width, this.height);
                    if (d < minDistance) {
                        minDistance = d;
                    }
                }
                this.data[x][y] = minDistance;
            }
        }

        // normalize
        this.normalizeData();
    }

    public void regenerate() {
        this.points = new int[numPoints][2];
        for (int i = 0; i < numPoints; i++) {
            this.points[i][0] = (int) (Math.random() * width);
            this.points[i][1] = (int) (Math.random() * height);
        }

        if (wrap)
            this.generateWithWrapping();
        else
            this.generate();
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

}
