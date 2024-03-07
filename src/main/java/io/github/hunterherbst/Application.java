package io.github.hunterherbst;

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


        Worley3D w3 = new Worley3D(250, 250, 250, 25);
        PNG[] pngs = w3.getPNGs();
        w3.regenerate();
        for(int i = 0; i < w3.getDepth(); i++) {
            pngs[i].setRedChannel(w3.getSlice(i));
        }
        w3.regenerate();
        for(int i = 0; i < w3.getDepth(); i++) {
            pngs[i].setGreenChannel(w3.getSlice(i));
        }
        GIF g = new GIF(pngs);
        try {
            g.saveAsPNGSequence("rgbGif");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}