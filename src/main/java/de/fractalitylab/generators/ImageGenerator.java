package de.fractalitylab.generators;

import de.fractalitylab.data.DataElement;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public interface ImageGenerator {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages);

    default BufferedImage rotateImage(BufferedImage originalImage) {
        double angle = random.nextDouble() * 360;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        BufferedImage rotatedImage = new BufferedImage(w, h, originalImage.getType());
        Graphics2D g2d = rotatedImage.createGraphics();
        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(angle), w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        return rotatedImage;
    }
}
