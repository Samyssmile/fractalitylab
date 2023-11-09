package de.fractalitylab.generators;

import de.fractalitylab.data.ImageWriter;
import de.fractalitylab.data.DataElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public class SierpinskiGasketGenerator implements ImageGenerator {
    private static final Logger LOGGER = Logger.getLogger(SierpinskiGasketGenerator.class.getName());
    private final Random rand = new Random();

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages) {
        List<DataElement> result = new ArrayList<>();
        for (int imageNumber = 0; imageNumber < numberOfImages; imageNumber++) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);

            double scaleFactor = 0.5 + (1.5 * rand.nextDouble()); // Zoom zwischen 0.5x und 2x

            Point3D[] vertices = {
                    new Point3D(0, height * scaleFactor, 0),
                    new Point3D(width * scaleFactor, height * scaleFactor, 0),
                    new Point3D(width / 2 * scaleFactor, 0, 0),
                    new Point3D(width / 2 * scaleFactor, height / 2 * scaleFactor, Math.sqrt(3.0) * height / 2 * scaleFactor)
            };

            drawGasket(g, vertices, maxIterations);

            g.dispose();
            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("sierpinski_gasket", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "sierpinski_gasket"));
        }

        LOGGER.info("SierpinskiGasket generation finished.");

        return result;
    }

    private void drawGasket(Graphics2D g, Point3D[] vertices, int depth) {
        if (depth == 0) {
            if (rand.nextBoolean()) {
                g.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
                int[] xPoints = { (int) vertices[0].x, (int) vertices[1].x, (int) vertices[2].x };
                int[] yPoints = { (int) vertices[0].y, (int) vertices[1].y, (int) vertices[2].y };
                g.fillPolygon(xPoints, yPoints, 3);
            }
        } else {
            Point3D[] midPoints = new Point3D[6];
            for (int i = 0; i < vertices.length; i++) {
                for (int j = i + 1; j < vertices.length; j++) {
                    int index = i * vertices.length + j - (i + 1) * (i + 2) / 2;
                    midPoints[index] = vertices[i].midPoint(vertices[j]);
                }
            }

            drawGasket(g, new Point3D[] { vertices[0], midPoints[0], midPoints[1], midPoints[3] }, depth - 1);
            drawGasket(g, new Point3D[] { vertices[1], midPoints[0], midPoints[2], midPoints[4] }, depth - 1);
            drawGasket(g, new Point3D[] { vertices[2], midPoints[1], midPoints[2], midPoints[5] }, depth - 1);
            drawGasket(g, new Point3D[] { vertices[3], midPoints[3], midPoints[4], midPoints[5] }, depth - 1);
        }
    }

    private class Point3D {
        double x, y, z;

        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point3D midPoint(Point3D other) {
            return new Point3D((this.x + other.x) / 2, (this.y + other.y) / 2, (this.z + other.z) / 2);
        }
    }
}
