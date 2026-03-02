package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Newton fractal images using Newton's method for f(z) = z³ - 1.
 * Colors indicate which root is converged to, brightness indicates convergence speed.
 */
public class NewtonFractalGenerator implements FractalGenerator {

	private static final Complex[] ROOTS = {
			new Complex(1, 0),
			new Complex(-0.5, Math.sqrt(3) / 2),
			new Complex(-0.5, -Math.sqrt(3) / 2)
	};

	private static final Color[] ROOT_COLORS = {
			new Color(255, 0, 0),
			new Color(0, 255, 0),
			new Color(0, 0, 255)
	};

	private static final double CONVERGENCE_THRESHOLD = 1e-6;
	private static final double ZOOM_MIN = 1.0;
	private static final double ZOOM_RANGE = 1000.0;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double zoom = ZOOM_MIN + ZOOM_RANGE * random.nextDouble();
		double rotation = 2 * Math.PI * random.nextDouble();

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double nx = (x - width / 2.0) / zoom;
				double ny = (y - height / 2.0) / zoom;
				double angle = Math.atan2(ny, nx) + rotation;
				double radius = Math.sqrt(nx * nx + ny * ny);
				nx = radius * Math.cos(angle);
				ny = radius * Math.sin(angle);

				pixels[rowOffset + x] = colorForPoint(nx, ny, maxIterations);
			}
		});
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "newton";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Newton Fractal",
				"Fractal from Newton's method applied to z³ - 1",
				18, 5, 200);
	}

	private int colorForPoint(double x, double y, int maxIterations) {
		Complex z = new Complex(x, y);
		int iterations = 0;
		double minDistance;
		int closestRootIndex;

		do {
			minDistance = Double.MAX_VALUE;
			closestRootIndex = -1;

			// Newton step: z = z - f(z)/f'(z) where f(z) = z³ - 1
			Complex z3 = z.pow(3).subtract(new Complex(1, 0));
			Complex z2 = z.pow(2).multiply(new Complex(3, 0));
			z = z.subtract(z3.divide(z2));

			for (int i = 0; i < ROOTS.length; i++) {
				double distance = z.subtract(ROOTS[i]).modulus();
				if (distance < minDistance) {
					minDistance = distance;
					closestRootIndex = i;
				}
			}

			if (minDistance < CONVERGENCE_THRESHOLD) {
				break;
			}
			iterations++;
		} while (iterations < maxIterations);

		if (closestRootIndex == -1) {
			return 0x000000;
		}

		float weight = 1.0f - (float) iterations / maxIterations;
		Color rootColor = ROOT_COLORS[closestRootIndex];
		int r = (int) (weight * rootColor.getRed());
		int g = (int) (weight * rootColor.getGreen());
		int b = (int) (weight * rootColor.getBlue());

		float brightnessAdj = (float) (1.0f - ((float) minDistance / CONVERGENCE_THRESHOLD));
		float colorAdj = 1.0f + brightnessAdj;
		r = Math.clamp((int) (r * colorAdj * brightnessAdj), 0, 255);
		g = Math.clamp((int) (g * colorAdj * brightnessAdj), 0, 255);
		b = Math.clamp((int) (b * colorAdj * brightnessAdj), 0, 255);

		return new Color(r, g, b).getRGB();
	}
}
