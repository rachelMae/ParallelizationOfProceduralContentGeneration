package io.github.hunterherbst;

import java.io.IOException;

public class Application {
    public static void main(String[] args) {
        // Default packaged code in the JAR
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Welcome to Worley Noise Generator!");
//        System.out.println("Please enter the width of the image: ");
//        int width = scan.nextInt();
//        System.out.println("Please enter the height of the image: ");
//        int height = scan.nextInt();
//        System.out.println("Please enter the depth of the image: ");
//        int depth = scan.nextInt();
//        System.out.println("Please enter the number of points: ");
//        int numPoints = scan.nextInt();
//        scan.close();
//
//
//        System.out.println("Generating 3D Worley noise...");
//        Worley3D w = new Worley3D(width, height, depth, numPoints);
//        w.generate();
//        System.out.println("Saving 3D Worley noise...");
//        PGM[] pgms = w.getPGMs();
//        PGM.savePNGSequence(pgms, "worleyGif");


//        Worley3D w3 = new Worley3D(250, 250, 250, 25);
//        PNG[] pngs = w3.getPNGs();
//        w3.regenerate();
//        for(int i = 0; i < w3.getDepth(); i++) {
//            pngs[i].setRedChannel(w3.getSlice(i));
//        }
//        w3.regenerate();
//        for(int i = 0; i < w3.getDepth(); i++) {
//            pngs[i].setGreenChannel(w3.getSlice(i));
//        }
//        GIF g = new GIF(pngs);
//        try {
//            g.saveAsPNGSequence("rgbGif");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        // GET START TIME
//        long startTime = System.currentTimeMillis();

//        Worley w = new Worley(4096, 4096, 500);

//        long worleyEndTime = System.currentTimeMillis();
//
//        WorleyThreaded wt = new WorleyThreaded(4096, 4096, 500);
//
//        long worleyThreadedEndTime = System.currentTimeMillis();
//
////        System.out.println("Worley Time: " + (worleyEndTime - startTime) + "ms");
//        System.out.println("WorleyThreaded Time: " + (worleyThreadedEndTime - worleyEndTime) + "ms");
//
//        wt.invert();
//
//        try{
//            wt.getPNG().save("worleyThreaded.png");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        //This takes an absolutely stupid big amount of memory, we're talking likely 8gigs minimum
        Worley3DThreaded wt = new Worley3DThreaded(250, 250, 500, 100);

        wt.invert();

        try {
            (new GIF(wt.getPNGs())).saveAsPNGSequence("taxicab");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}