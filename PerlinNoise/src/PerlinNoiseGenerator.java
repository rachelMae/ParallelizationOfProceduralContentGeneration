import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PerlinNoiseGenerator {


    private PerlinNoise perlin;

    public PerlinNoiseGenerator() {
        perlin = new PerlinNoise();
    }

    public double[][][] generatePerlinNoise3D(int width, int height, int depth, double scale) {
        double[][][] perlinArray = new double[width][height][depth];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    double value = perlin.noise(x * scale, y * scale, z * scale);
                    perlinArray[x][y][z] = value;
                }
            }
        }

        return perlinArray;
    }

    public double[][] generatePerlinNoise2D(int width, int height, double scale) {
        double[][] perlinArray = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = perlin.noise(x * scale, y * scale);
                perlinArray[x][y] = value;
            }
        }

        return perlinArray;
    }

    public void saveArrayAsImage(double[][][] array, String fileName) {
        int width = array.length;
        int height = array[0].length;
        int depth = array[0][0].length; // Assuming depth is the third dimension

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Map and normalize the array values to grayscale colors
        // Map and normalize the array values to grayscale colors
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) { // Iterate over the depth
                    double value = array[x][y][z];

                    // Ensure the value is within the range [-1, 1]
                    value = Math.max(-1.0, Math.min(1.0, value));

                    // Normalize the value to the [0, 1] range
                    float normalizedValue = (float) ((value + 1.0) / 2.0);

                    int color = (int) (normalizedValue * 255);
                    int rgb = (color << 16) | (color << 8) | color;
                    image.setRGB(x, y, rgb);
                }
            }
        }

        // Save the image to a file
        try {
            File output = new File(fileName);
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveArrayAsImageMultithreading(double[][][] array, String fileName) {
        int width = array.length;
        int height = array[0].length;
        int depth = array[0][0].length;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Thread[] threads = new Thread[height];
        for (int y = 0; y < height; y++) {
            final int yIndex = y;
            threads[y] = new Thread(() -> {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        double value = array[x][yIndex][z];
                        value = Math.max(-1.0, Math.min(1.0, value));
                        float normalizedValue = (float) ((value + 1.0) / 2.0);
                        int color = (int) (normalizedValue * 255);
                        int rgb = (color << 16) | (color << 8) | color;
                        synchronized (image) {
                            image.setRGB(x, yIndex, rgb);
                        }
                    }
                }
            });
            threads[y].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Save the image to a file
        try {
            File output = new File(fileName);
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArrayAsImage2D(double[][] array, String fileName) {
        int width = array.length;
        int height = array[0].length;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Map and normalize the array values to grayscale colors
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = array[x][y];

                // Ensure the value is within the range [-1, 1]
                value = Math.max(-1.0, Math.min(1.0, value));

                // Normalize the value to the [0, 1] range
                float normalizedValue = (float) ((value + 1.0) / 2.0);

                int color = (int) (normalizedValue * 255);
                int rgb = (color << 16) | (color << 8) | color;
                image.setRGB(x, y, rgb);
            }
        }

        // Save the image to a file
        try {
            File output = new File(fileName);
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArrayAsImage2DMultithreading(double[][] array, String fileName) {
        int width = array.length;
        int height = array[0].length;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create threads for each row
        Thread[] threads = new Thread[height];
        for (int y = 0; y < height; y++) {
            final int yIndex = y;
            threads[y] = new Thread(() -> {
                for (int x = 0; x < width; x++) {
                    double value = array[x][yIndex];
                    value = Math.max(-1.0, Math.min(1.0, value));
                    float normalizedValue = (float) ((value + 1.0) / 2.0);
                    int color = (int) (normalizedValue * 255);
                    int rgb = (color << 16) | (color << 8) | color;
                    synchronized (image) {
                        image.setRGB(x, yIndex, rgb);
                    }
                }
            });
            threads[y].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Save the image to a file
        try {
            File output = new File(fileName);
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        PerlinNoiseGenerator generator = new PerlinNoiseGenerator();

        int width = 1000; //adjust 
        int height = 1000; //adjust
        int depth = 5; //adjust
        double scale = 0.1;
        double[][][] perlinArray = generator.generatePerlinNoise3D(width, height, depth, scale);

        // Save the generated array as a PNG image
        generator.saveArrayAsImage(perlinArray, "perlin_noise_3d.png");


        // Generate and save 2D Perlin noise
        int width2D = 1000; //adjust
        int height2D = 1000; //adjust
        double scale2D = 0.05;
        double[][] perlinArray2D = generator.generatePerlinNoise2D(width2D, height2D, scale2D);
        generator.saveArrayAsImage2D(perlinArray2D, "perlin_noise_2d.png");

        System.out.println("Non-Multithreaded complete");

        //multithreading
        double[][][] perlinArrayMultithreading = generator.generatePerlinNoise3D(width, height, depth, scale);
        generator.saveArrayAsImageMultithreading(perlinArrayMultithreading, "perlin_noise_3d_multi.png");

        double[][] perlinArray2DMultithreading = generator.generatePerlinNoise2D(width2D, height2D, scale2D);
        generator.saveArrayAsImage2D(perlinArray2DMultithreading, "perlin_noise_2d_multi.png");
    
        System.out.println("Multithreaded complete");

    }
}
