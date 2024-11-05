package de.fractalitylab.generators;

import de.fractalitylab.data.DataElement;
import de.fractalitylab.data.ImageWriter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class SierpinskiGasketGenerator implements ImageGenerator {
    private static final Logger LOGGER = Logger.getLogger(SierpinskiGasketGenerator.class.getName());

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages, int quality) {
        final int iterations = maxIterations * 10; // You can adjust this multiplier for more detailed fractals
        List<DataElement> result = new ArrayList<>();
        IntStream.range(0, numberOfImages).parallel().forEach(imageNumber -> {
            double zoomFactor = ThreadLocalRandom.current().nextDouble(0.5, 1.5);
            BufferedImage image = generateSingleImage(width, height, zoomFactor, iterations);
            image = rotateImage(image);
            image = applyQualityAdjustments(image, quality);
            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("sierpinski", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "sierpinski"));
        });
        LOGGER.info("Sierpinski Gasket generation finished.");
        return result;
    }

    private BufferedImage generateSingleImage(int width, int height, double zoomFactor, int maxIterations) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        drawTriangle(g, 0, height, width, height, width / 2, 0, maxIterations(zoomFactor), 0, width, height);

        g.dispose();
        return image;
    }

    private void drawTriangle(Graphics2D g, int x1, int y1, int x2, int y2, int x3, int y3, int depth, int currentDepth, int maxWidth, int maxHeight) {
        int centerX = maxWidth / 2;
        int centerY = maxHeight / 2;
        if (depth == 0) {
            float hue = (float) currentDepth / (float) maxIterations(1.5f);
            float distanceToCenter = (float) Math.sqrt(Math.pow((x1 + x2 + x3) / 3 - centerX, 2) + Math.pow((y1 + y2 + y3) / 3 - centerY, 2));
            float saturation = 1.0f - (distanceToCenter / (float) Math.sqrt(Math.pow(centerX, 2) + Math.pow(centerY, 2)));
            float brightness = 0.5f + 0.5f * ((maxWidth - distanceToCenter) / maxWidth);
            Color color = Color.getHSBColor(hue, saturation, brightness);
            g.setColor(color);
            g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
        } else {
            int mx1 = (x1 + x2) / 2;
            int my1 = (y1 + y2) / 2;
            int mx2 = (x2 + x3) / 2;
            int my2 = (y2 + y3) / 2;
            int mx3 = (x3 + x1) / 2;
            int my3 = (y3 + y1) / 2;
            drawTriangle(g, x1, y1, mx1, my1, mx3, my3, depth - 1, currentDepth + 1, maxWidth, maxHeight);
            drawTriangle(g, mx1, my1, x2, y2, mx2, my2, depth - 1, currentDepth + 1, maxWidth, maxHeight);
            drawTriangle(g, mx3, my3, mx2, my2, x3, y3, depth - 1, currentDepth + 1, maxWidth, maxHeight);
        }
    }

    private int maxIterations(double zoomFactor) {
        return (int) (Math.log(zoomFactor) / Math.log(0.5) + 5);
    }
}
