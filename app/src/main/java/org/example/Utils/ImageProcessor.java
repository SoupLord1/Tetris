package org.example.Utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessor {
    public BufferedImage image;
    public int imageWidth;
    public int imageHeight;
    public int[] pixels;

    public ImageProcessor(String filepath, float hue) {
        try {
            image = ImageIO.read(new File(filepath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        pixels = image.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];

            int r = (p >> 16) & 0xff;
            int g = (p >> 8) & 0xff;
            int b = p & 0xff;

            float[] hsbValues = new float[3];

            Color.RGBtoHSB(r, g, b, hsbValues);

            int newPixel = Color.HSBtoRGB(hue, 1f, hsbValues[2]);

            pixels[i] = newPixel;

        }

        image.setRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);

    }


}

