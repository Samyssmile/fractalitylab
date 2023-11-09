package de.fractalitylab.generators;

import de.fractalitylab.data.ImageWriter;
import de.fractalitylab.data.DataElement;

import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class BurningShipGenerator implements ImageGenerator{
    private static final Logger LOGGER = Logger.getLogger(BurningShipGenerator.class.getName());

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages) {
        List<DataElement> result = new ArrayList<>();
        IntStream.range(1, numberOfImages + 1).forEach(imageNumber -> {
            BufferedImage image;
            image = generateSingleImage(width, height, maxIterations*10 );

            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("burningship", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "burningship"));
        });
        LOGGER.info("BurningShip generation finished.");
        return result;
    }

    private BufferedImage generateSingleImage(int width, int height, int maxIterations) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double rotationAngle = random.nextDouble() * Math.PI * 2;

        double zoom = 30 + random.nextDouble() * 500;
        double moveX = -0.5 + (random.nextDouble() - 0.5) / zoom;
        double moveY = (random.nextDouble() - 0.5) / zoom;

        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                double newX = (x - width / 2) / zoom;
                double newY = (y - height / 2) / zoom;
                double[] rotated = rotatePoint(newX, newY, rotationAngle);

                double zx = rotated[0] + moveX;
                double zy = rotated[1] + moveY;
                double cRe = zx;
                double cIm = zy;
                int iter = 0;

                while (zx * zx + zy * zy < 4 && iter < maxIterations) {
                    double tmp = zx * zx - zy * zy + cRe;
                    zy = Math.abs(2.0 * zx * zy) + cIm;
                    zx = tmp;
                    iter++;
                }

                float hueShift = random.nextFloat();

                if (iter < maxIterations) {
                    // Glättungsfunktion für einen sanfteren Farbverlauf
                    double mu = iter - Math.log(Math.log(zx*zx + zy*zy)) / Math.log(2);
                    mu = Math.max(mu, 0);

                    // Farbverlauf von Orange zu Schwarz
                    float hue = 0.05f + 0.95f * (float)(maxIterations - mu) / maxIterations; // Farbton von Gelb-Orange zu Rot
                    float saturation = 1.0f; // volle Sättigung für lebendige Farben
                    float brightness = (float)Math.sqrt(mu / maxIterations); // Helligkeit abhängig von der Nähe zu maxIterations

                    // Umsetzung des Farbwerts in einen RGB-Wert
                    int color = Color.HSBtoRGB(hue, saturation, brightness);
                    image.setRGB(x, y, color);
                } else {
                    // Die Punkte, die gegen unendlich tendieren, werden Schwarz
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        });
        return image;
    }



    private double[] rotatePoint(double x, double y, double angle) {
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        return new double[]{
                cosAngle * x - sinAngle * y,
                sinAngle * x + cosAngle * y
        };
    }
}
