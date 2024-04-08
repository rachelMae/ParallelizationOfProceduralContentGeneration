import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PerlinNoiseMultithread {


    private static final int P = 512;
    private int[] perm;

    public PerlinNoiseMultithread() {
        perm = new int[P];
        for (int i = 0; i < P; i++) {
            perm[i] = i;
        }

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
        double grad = 1 + (h & 7); 
        if ((h & 8) != 0)
            grad = -grad; 
        return (grad * x); 
    }

    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double gradX = 1 + (h & 7); 
        double gradY = 1 + ((h + 1) & 7); 
        double gradZ = 1 + ((h + 2) & 7); 

        if ((h & 8) != 0)
            gradX = -gradX; 
        if (((h + 1) & 8) != 0)
            gradY = -gradY; 
        if (((h + 2) & 8) != 0)
            gradZ = -gradZ; 

        return (gradX * (x - ((h & 1) == 0 ? 0 : 1)) + gradY * (y - ((h & 2) == 0 ? 0 : 1))
                + gradZ * (z - ((h & 4) == 0 ? 0 : 1)));
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 7; 
        double gradX = 1 + (h & 1) * 2 - 1; 
        double gradY = 1 + (h >> 1 & 1) * 2 - 1; 
        return (gradX * x + gradY * y); 
    }

    public double noise(double x) {
        final double inputX = x; // Final variable to store the value of x
        Callable<Double> task = () -> {
            int X = (int) Math.floor(inputX) & 255;
            double localX = inputX - Math.floor(inputX); // Use local variable inside the lambda
            double u = fade(localX);
            return lerp(u, grad(perm[X], localX), grad(perm[X + 1], localX - 1));
        };
        return executeTask(task);
    }
    
    public double noise(double x, double y) {
        final double inputX = x; // Final variable to store the value of x
        final double inputY = y; // Final variable to store the value of y
        Callable<Double> task = () -> {
            int X = (int) Math.floor(inputX) & 255;
            int Y = (int) Math.floor(inputY) & 255;
            double localX = inputX - Math.floor(inputX); // Use local variable inside the lambda
            double localY = inputY - Math.floor(inputY); // Use local variable inside the lambda
            double u = fade(localX);
            double v = fade(localY);
            int A = perm[X] + Y;
            int AA = perm[A & 255];
            int AB = perm[(A + 1) & 255];
            int B = perm[X + 1] + Y;
            int BA = perm[B & 255];
            int BB = perm[(B + 1) & 255];
            return lerp(v, lerp(u, grad(perm[AA], localX, localY), grad(perm[BA], localX - 1, localY)),
                    lerp(u, grad(perm[AB], localX, localY - 1), grad(perm[BB], localX - 1, localY - 1)));
        };
        return executeTask(task);
    }
    
    public double noise(double x, double y, double z) {
        final double inputX = x; // Final variable to store the value of x
        final double inputY = y; // Final variable to store the value of y
        final double inputZ = z; // Final variable to store the value of z
        Callable<Double> task = () -> {
            int X = (int) Math.floor(inputX) & 255;
            int Y = (int) Math.floor(inputY) & 255;
            int Z = (int) Math.floor(inputZ) & 255;
            double localX = inputX - Math.floor(inputX); // Use local variable inside the lambda
            double localY = inputY - Math.floor(inputY); // Use local variable inside the lambda
            double localZ = inputZ - Math.floor(inputZ); // Use local variable inside the lambda
            double u = fade(localX);
            double v = fade(localY);
            double w = fade(localZ);
            int A = perm[X] + Y;
            int AA = perm[A & 255] + Z;
            int AB = perm[(A + 1) & 255] + Z;
            int B = perm[X + 1] + Y;
            int BA = perm[B & 255] + Z;
            int BB = perm[(B + 1) & 255] + Z;
            return lerp(w, lerp(v, lerp(u, grad(perm[AA], localX, localY, localZ), grad(perm[BA], localX - 1, localY, localZ)),
                    lerp(u, grad(perm[AB], localX, localY - 1, localZ), grad(perm[BB], localX - 1, localY - 1, localZ))),
                    lerp(v, lerp(u, grad(perm[(AA + 1) & 255], localX, localY, localZ - 1), grad(perm[(BA + 1) & 255], localX - 1, localY, localZ - 1)),
                            lerp(u, grad(perm[(AB + 1) & 255], localX, localY - 1, localZ - 1),
                                    grad(perm[(BB + 1) & 255], localX - 1, localY - 1, localZ - 1))));
        };
        return executeTask(task);
    }

    private double executeTask(Callable<Double> task) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Double> future = executor.submit(task);
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        PerlinNoiseMultithread perlin = new PerlinNoiseMultithread();

        // Example usage
        // System.out.println(perlin.noise(1.23));
        // System.out.println(perlin.noise(2.45, 3.67));
        System.out.println(perlin.noise(4.56, 7.89, 10.11));
    }
}


