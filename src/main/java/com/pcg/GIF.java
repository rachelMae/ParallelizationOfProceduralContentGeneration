package com.pcg;

import java.io.File;
import java.io.IOException;


public class GIF {

    private static final int DEFAULT_DELAY = 5;
    private final int width;
    private final int height;
    private final GifFrame[] frames;

    public GIF(int width, int height, int numFrames, int delay) {
        this.width = width;
        this.height = height;
        this.frames = new GifFrame[numFrames];

        for (int i = 0; i < numFrames; i++) {
            frames[i] = new GifFrame(width, height, delay);
        }
    }

    public GIF(int width, int height, int numFrames) {
        this(width, height, numFrames, DEFAULT_DELAY);
    }

    public GIF(int width, int height, GifFrame[] frames) {
        this.width = width;
        this.height = height;
        this.frames = frames;
    }

    public GIF(PNG[] pngs) {
        this.width = pngs[0].getWidth();
        this.height = pngs[0].getHeight();
        this.frames = new GifFrame[pngs.length];
        for (int i = 0; i < pngs.length; i++) {
            frames[i] = new GifFrame(width, height, DEFAULT_DELAY);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    frames[i].set(x, y, pngs[i].getPixel(x, y).getRGBA());
                }
            }
        }
    }

    public void setFrame(int index, GifFrame frame) {
        frames[index] = frame;
    }

    public GifFrame getFrame(int index) {
        return frames[index];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumFrames() {
        return frames.length;
    }

    public GifFrame[] getFrames() {
        return frames;
    }

    public void saveAsPNGSequence(String folderName) throws IOException {
        File folder = new File(folderName);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                System.out.println("Failed to create folder");
                return;
            }
        }

        for (int i = 0; i < frames.length; i++) {
            frames[i].save(folderName + "/frame" + i + ".png");
        }
    }
}
