package com.pcg;

public class GifFrame extends PNG {
    private int delay;

    public GifFrame(int width, int height, int delay) {
        super(width, height);
        this.delay = delay;
    }

    public GifFrame(PNG png, int delay) {
        super(png.getWidth(), png.getHeight());
        this.delay = delay;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                set(x, y, png.getPixel(x, y).getRGBA());
            }
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public void set(int x, int y, int rgba) {
        int r = (rgba >> 16) & 0xFF;
        int g = (rgba >> 8) & 0xFF;
        int b = rgba & 0xFF;
        int a = (rgba >> 24) & 0xFF;
        super.setPixel(x, y, new Pixel(r / 255f, g / 255f, b / 255f, a / 255f));
    }
}