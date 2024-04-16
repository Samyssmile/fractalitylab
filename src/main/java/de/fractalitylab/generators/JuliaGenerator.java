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

public class JuliaGenerator implements ImageGenerator {
    private static final Logger LOGGER = Logger.getLogger(JuliaGenerator.class.getName());

    ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages) {
        List<DataElement> result = new ArrayList<>();
        IntStream.range(1, numberOfImages + 1).parallel().forEach(imageNumber -> {
            BufferedImage image;
            image = generateSingleImage(width, height, maxIterations, imageNumber);

            image = rotateImage(image);
            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("julia", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "julia"));
        });
        LOGGER.info("Julia generation finished.");
        return result;
    }

    private BufferedImage generateSingleImage(int width, int height, int maxIterations, int imageNumber) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double cRe = random.nextDouble() * 2 - 1;
        double cIm = random.nextDouble() * 2 - 1;

        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                double zx = x * (2.0 / width) - 1.5;
                double zy = y * (2.0 / height) - 1;
                int iter = 0;
                double tmp;

                while (zx * zx + zy * zy < 4 && iter < maxIterations) {
                    tmp = zx * zx - zy * zy + cRe;
                    zy = 2.0 * zx * zy + cIm;
                    zx = tmp;
                    iter++;
                }

                float hue = ((iter + imageNumber) % maxIterations) / (float) maxIterations;
                int color = (iter < maxIterations) ?
                        Color.HSBtoRGB(hue, 1, iter % 2) :
                        Color.BLACK.getRGB();


                image.setRGB(x, y, color);
            }
        });
        return image;
    }

}
