package de.fractalitylab.generators;

import de.fractalitylab.data.DataElement;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface ImageGenerator {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    public abstract List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages, int quality, boolean isTrain);

    default BufferedImage applyQualityAdjustments(BufferedImage image, int quality) {
        // Stärkere Unschärfe basierend auf der Qualität anwenden
        float blurRadius = (quality) / 1.0f;
        if (blurRadius > 0) {
            image = applyGaussianBlur(image, blurRadius);
        }

        // Rauschen hinzufügen
        if (quality < 100) {
            image = addNoise(image, (100 - quality) / 5.0f);
        }

        // Weitere Qualitätsanpassungen können hier hinzugefügt werden

        return image;
    }

    default BufferedImage applyGaussianBlur(BufferedImage src, float radius) {
        int radiusInt = (int) Math.ceil(radius);
        int size = radiusInt * 2 + 1;
        float[] data = new float[size * size];
        float sigma = radius / 3;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = (float) (2 * Math.PI * sigma * sigma);
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0;
        int index = 0;

        for (int y = -radiusInt; y <= radiusInt; y++) {
            for (int x = -radiusInt; x <= radiusInt; x++) {
                float distance = x * x + y * y;
                if (distance > radius2) {
                    data[index] = 0;
                } else {
                    data[index] = (float) Math.exp(-distance / sigma22) / sqrtSigmaPi2;
                }
                total += data[index];
                index++;
            }
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(src, null);
    }

    default BufferedImage addNoise(BufferedImage image, float noiseLevel) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage noisyImage = new BufferedImage(width, height, image.getType());
        Random rand = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                // Zerlege die RGB-Komponenten
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Füge Rauschen hinzu
                int nr = clamp(r + (int) ((rand.nextFloat() - 0.5f) * 255 * noiseLevel));
                int ng = clamp(g + (int) ((rand.nextFloat() - 0.5f) * 255 * noiseLevel));
                int nb = clamp(b + (int) ((rand.nextFloat() - 0.5f) * 255 * noiseLevel));

                int newRGB = (a << 24) | (nr << 16) | (ng << 8) | nb;
                noisyImage.setRGB(x, y, newRGB);
            }
        }
        return noisyImage;
    }

    default int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

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
