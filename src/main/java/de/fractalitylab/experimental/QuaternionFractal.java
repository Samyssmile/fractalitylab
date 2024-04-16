package de.fractalitylab.experimental;

import org.apache.commons.math3.complex.Quaternion;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class QuaternionFractal {
    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    private static final int MAX_ITER = 1000;
    private static final double ZOOM = 1.0;

    public BufferedImage generateFractal() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                double zx = 1.5 * (x - WIDTH / 2) / (0.5 * ZOOM * WIDTH);
                double zy = (y - HEIGHT / 2) / (0.5 * ZOOM * HEIGHT);
                float brightness = iterate(new Quaternion(zx, zy, 0.0, 0.0));
                int color = calculateColor(brightness);
                image.setRGB(x, y, color);
            }
        }

        return image;
    }

    private float iterate(Quaternion z) {
        Quaternion c = new Quaternion(z.getQ0(), z.getQ1(), z.getQ2(), z.getQ3());
        int i;
        for (i = 0; i < MAX_ITER; i++) {
            if (z.getNorm() > 2.0) break;
            z = z.multiply(z).add(c);
        }
        return (float)i / MAX_ITER;
    }

    private int calculateColor(float brightness) {
        int alpha = 255;
        int red = (int)(brightness * 255);
        int green = (int)(brightness * 255);
        int blue = (int)(brightness * 255);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public void saveImage(BufferedImage image, String filename) {
        try {
            File outputFile = new File(filename);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Fractal image saved as " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        QuaternionFractal fractal = new QuaternionFractal();
        BufferedImage fractalImage = fractal.generateFractal();
        fractal.saveImage(fractalImage, "quaternion_fractal.png");
    }
}
