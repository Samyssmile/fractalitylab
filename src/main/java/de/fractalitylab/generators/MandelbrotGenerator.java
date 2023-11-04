package de.fractalitylab.generators;

import de.fractalitylab.data.ImageWriter;
import de.fractalitylab.data.DataElement;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class MandelbrotGenerator implements ImageGenerator {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages) {
        List<DataElement> result = new ArrayList<>();
        int iterations = maxIterations * 100;
        IntStream.range(1, numberOfImages + 1).parallel().forEach(imageNumber -> {
            BufferedImage image;
            do {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                double zoom = 1000.0 + random.nextDouble() * 10000.0;
                double moveX = -0.7 + random.nextDouble() * 0.7;
                double moveY = random.nextDouble() * 0.7;
                BufferedImage finalImage = image;
                IntStream.range(0, height).parallel().forEach(y -> {
                    for (int x = 0; x < width; x++) {
                        double zx = (x - width / 2.0) / zoom + moveX;
                        double zy = (y - height / 2.0) / zoom + moveY;
                        double cX = zx;
                        double cY = zy;
                        int iter = 0;
                        double tmp;
                        while ((zx * zx + zy * zy < 4) && (iter < iterations)) {
                            tmp = zx * zx - zy * zy + cX;
                            zy = 2.0 * zx * zy + cY;
                            zx = tmp;
                            iter++;
                        }
                        int color = Color.HSBtoRGB((float) iter / iterations + (iter % 2) * 0.5f, 1, iter < iterations ? 1 : 0);
                        finalImage.setRGB(x, y, color);
                    }
                });
            } while (isImageAllBlack(image));
            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("mandelbrot", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "mandelbrot"));
        });
        return result;
    }

    private boolean isImageAllBlack(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != Color.BLACK.getRGB()) {
                    return false;
                }
            }
        }
        return true;
    }
}
