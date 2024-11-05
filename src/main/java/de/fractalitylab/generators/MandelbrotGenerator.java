package de.fractalitylab.generators;

import de.fractalitylab.data.DataElement;
import de.fractalitylab.data.ImageWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class MandelbrotGenerator implements ImageGenerator {
    private static final Logger LOGGER = Logger.getLogger(MandelbrotGenerator.class.getName());

    ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages, int quality) {
        List<DataElement> result = new ArrayList<>();

        int minIterations = 10; // Minimale Anzahl von Iterationen

        IntStream.range(1, numberOfImages + 1).parallel().forEach(imageNumber -> {
            boolean isValid;
            BufferedImage image;
            do {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                int iterations = (int) (minIterations + (maxIterations - minIterations) * (quality / 100.0));
                double zoom = 1000.0 + random.nextDouble() * 10.0;
                double moveX = -0.7 + random.nextDouble() * 0.7;
                double moveY = random.nextDouble() * 0.7;
                BufferedImage finalImage = image;
                int finalIterations = iterations;
                IntStream.range(0, height).parallel().forEach(y -> {
                    for (int x = 0; x < width; x++) {
                        double zx = (x - width / 2.0) / zoom + moveX;
                        double zy = (y - height / 2.0) / zoom + moveY;
                        double cX = zx;
                        double cY = zy;
                        int iter = 0;
                        double tmp;
                        while ((zx * zx + zy * zy < 4) && (iter < finalIterations)) {
                            tmp = zx * zx - zy * zy + cX;
                            zy = 2.0 * zx * zy + cY;
                            zx = tmp;
                            iter++;
                        }
                        int color = Color.HSBtoRGB((float) iter / finalIterations + (iter % 2) * 0.5f, 1, iter < finalIterations ? 1 : 0);
                        finalImage.setRGB(x, y, color);
                    }
                });
                isValid = containsFractal(finalImage);
            } while (!isValid);


            image = applyQualityAdjustments(image, quality);
            image = rotateImage(image);

            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("mandelbrot", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "mandelbrot"));
        });

        LOGGER.info("Mandelbrot-Generierung abgeschlossen.");
        return result;
    }

    private boolean containsFractal(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (image.getRGB(x, y) != Color.BLACK.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }
}