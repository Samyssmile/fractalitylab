package de.fractalitylab.generators;

import de.fractalitylab.data.DataElement;
import de.fractalitylab.data.ImageWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class NewtonFractalGenerator implements ImageGenerator {
    private static final Logger LOGGER = Logger.getLogger(NewtonFractalGenerator.class.getName());

    ThreadLocalRandom random = ThreadLocalRandom.current();


    public NewtonFractalGenerator() {
    }

    public BufferedImage generateImage(int width, int height, int quality) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Parameters for fractal generation
        double zoom = 1 + 1000 * random.nextDouble(); // Random zoom
        double rotation = 2 * Math.PI * random.nextDouble(); // Random rotation

        // Generate fractal (this is a placeholder for the actual fractal generation logic)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Apply rotation and zoom to the current point
                double nx = (x - width / 2) / zoom;
                double ny = (y - height / 2) / zoom;
                double angle = Math.atan2(ny, nx) + rotation;
                double radius = Math.sqrt(nx * nx + ny * ny);
                nx = radius * Math.cos(angle);
                ny = radius * Math.sin(angle);

                // Determine the color of the point based on the Newton iteration (placeholder)
                int color = getColorFromIteration(nx, ny, quality);
                image.setRGB(x, y, color);
            }
        }

        g.dispose();
        return image;
    }

    @Override
    public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages, int quality) {
        List<DataElement> result = Collections.synchronizedList(new ArrayList<>());
        IntStream.range(1, numberOfImages + 1).parallel().forEach(imageNumber -> {
            BufferedImage image;
            image = generateImage(width, height, maxIterations * random.nextInt(1, 300));
            image = applyQualityAdjustments(image, quality);
            image = rotateImage(image);
            UUID uuid = UUID.randomUUID();
            ImageWriter.writeImage("newton", uuid.toString(), image);
            result.add(new DataElement(uuid.toString(), "newton"));
        });

        LOGGER.info(result.size()+ " Newton generation finished.");
        return result;
    }

    private int getColorFromIteration(double x, double y, int quality) {
        final int MAX_ITERATIONS = quality;
        final double CONVERGENCE_THRESHOLD = 1e-6;

        // Wurzeln des Polynoms z^3 - 1
        Complex[] roots = {
                new Complex(1, 0),
                new Complex(-0.5, Math.sqrt(3) / 2),
                new Complex(-0.5, -Math.sqrt(3) / 2)
        };
        // Farben für die Wurzeln
        Color[] colors = {
                new Color(255, 0, 0), // Rot
                new Color(0, 255, 0), // Grün
                new Color(0, 0, 255)  // Blau
        };

        Complex z = new Complex(x, y);
        Complex zPrev;

        int iterations = 0;
        double minDistance = Double.MAX_VALUE;
        int closestRootIndex = -1;
        do {
            zPrev = z;
            z = z.subtract(f(z).divide(fPrime(z)));

            // Finde die nächste Wurzel und ihre Distanz
            for (int i = 0; i < roots.length; i++) {
                double distance = z.subtract(roots[i]).modulus();
                if (distance < minDistance) {
                    minDistance = distance;
                    closestRootIndex = i;
                }
            }

            if (minDistance < CONVERGENCE_THRESHOLD) {
                break;
            }

            iterations++;
        } while (iterations < MAX_ITERATIONS);

        // Falls keine Konvergenz erreicht wurde, setzen wir die Farbe auf Schwarz.
        if (closestRootIndex == -1) {
            return 0x000000;
        }

        // Erzeuge Farbübergänge zwischen den Wurzeln
        float[] colorWeights = new float[roots.length];
        for (int i = 0; i < roots.length; i++) {
            if (i == closestRootIndex) {
                colorWeights[i] = 1.0f - (float) iterations / MAX_ITERATIONS;
            } else {
                colorWeights[i] = 0.0f;
            }
        }

        // Berechne die neue Farbe
        int r = 0, g = 0, b = 0;
        for (int i = 0; i < roots.length; i++) {
            r += colorWeights[i] * colors[i].getRed();
            g += colorWeights[i] * colors[i].getGreen();
            b += colorWeights[i] * colors[i].getBlue();
        }

        // Anpassung für mehr Helligkeit
        float brightnessAdjustment = (float) (1.0f - ((float) minDistance / CONVERGENCE_THRESHOLD));
        float colorAdjustment = 1.0f + brightnessAdjustment;
        r = (int) (r * colorAdjustment * brightnessAdjustment) % 256;
        g = (int) (g * colorAdjustment * brightnessAdjustment) % 256;
        b = (int) (b * colorAdjustment * brightnessAdjustment) % 256;

        return new Color(r, g, b).getRGB();
    }


    // Die Funktion f(z) für die Newton-Raphson-Iteration
    private Complex f(Complex z) {
        // Beispiel für ein Polynom: z^3 - 1
        return z.pow(3).subtract(new Complex(1, 0));
    }

    // Die Ableitung von f(z)
    private Complex fPrime(Complex z) {
        // Die Ableitung von z^3 - 1 ist 3z^2
        return z.pow(2).multiply(new Complex(3, 0));
    }


    // Einfache komplexe Zahl Klasse
    class Complex {
        private double re;
        private double im;

        public Complex(double real, double imaginary) {
            this.re = real;
            this.im = imaginary;
        }

        public Complex subtract(Complex b) {
            return new Complex(this.re - b.re, this.im - b.im);
        }

        public Complex multiply(Complex b) {
            return new Complex(this.re * b.re - this.im * b.im, this.re * b.im + this.im * b.re);
        }

        public Complex divide(Complex b) {
            Complex conjugate = b.conjugate();
            Complex numerator = this.multiply(conjugate);
            double denominator = b.re * b.re + b.im * b.im;
            return new Complex(numerator.re / denominator, numerator.im / denominator);
        }

        public Complex pow(int power) {
            Complex result = this;
            for (int i = 1; i < power; i++) {
                result = result.multiply(this);
            }
            return result;
        }

        public double modulus() {
            return Math.sqrt(re * re + im * im);
        }

        public Complex conjugate() {
            return new Complex(re, -im);
        }

    }


}
