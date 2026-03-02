package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Henon map attractor fractal images using the iteration:
 * x_{n+1} = 1 - a·x_n² + y_n, y_{n+1} = b·x_n.
 * Rendered as a log-scaled density histogram.
 */
public final class HenonMapGenerator implements FractalGenerator {

	private static final int POINTS_MULTIPLIER = 200;

	private static final int MAX_RETRIES = 10;
	private static final double MIN_FILL_RATIO = 0.005;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int totalPixels = width * height;
		int minFilledPixels = (int) (totalPixels * MIN_FILL_RATIO);

		for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
			BufferedImage result = tryGenerate(width, height, random);
			if (countFilledPixels(result, width, height) >= minFilledPixels) {
				return result;
			}
		}
		return tryGenerate(width, height, random);
	}

	private BufferedImage tryGenerate(int width, int height, ThreadLocalRandom random) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		double a = 1.2 + random.nextDouble() * 0.2;
		double b = 0.2 + random.nextDouble() * 0.15;
		float hueBase = random.nextFloat();

		int totalPoints = width * height * POINTS_MULTIPLIER;
		int[][] density = new int[width][height];

		double x = 0.1, y = 0.1;
		double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		int divergenceCount = 0;

		for (int i = 0; i < 5000; i++) {
			double newX = 1.0 - a * x * x + y;
			double newY = b * x;
			x = newX;
			y = newY;
			if (Double.isNaN(x) || Double.isInfinite(x)) {
				divergenceCount++;
				x = random.nextDouble() * 0.01;
				y = random.nextDouble() * 0.01;
				continue;
			}
			if (i > 100) {
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}
		}

		if (minX >= maxX) { minX = -2; maxX = 2; }
		if (minY >= maxY) { minY = -1; maxY = 1; }

		double rangeX = maxX - minX;
		double rangeY = maxY - minY;

		int maxDensity = 1;
		for (int i = 0; i < totalPoints; i++) {
			double newX = 1.0 - a * x * x + y;
			double newY = b * x;
			x = newX;
			y = newY;

			if (Double.isNaN(x) || Double.isInfinite(x)) {
				x = random.nextDouble() * 0.01;
				y = random.nextDouble() * 0.01;
				continue;
			}

			int px = (int) ((x - minX) / rangeX * (width - 1));
			int py = (int) ((y - minY) / rangeY * (height - 1));
			if (px >= 0 && px < width && py >= 0 && py < height) {
				density[px][py]++;
				if (density[px][py] > maxDensity) maxDensity = density[px][py];
			}
		}

		double logMax = Math.log1p(maxDensity);
		int[] pixels = new int[width * height];
		for (int px = 0; px < width; px++) {
			for (int py = 0; py < height; py++) {
				if (density[px][py] > 0) {
					float brightness = (float) (Math.log1p(density[px][py]) / logMax);
					float hue = hueBase + 0.2f * brightness;
					pixels[py * width + px] = Color.HSBtoRGB(hue, 0.6f, brightness);
				}
			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	private int countFilledPixels(BufferedImage image, int width, int height) {
		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		int count = 0;
		for (int pixel : pixels) {
			if ((pixel & 0xFFFFFF) != 0) count++;
		}
		return count;
	}

	@Override
	public String label() {
		return "henon";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Henon Map",
				"Strange attractor from 1 - ax² + y, bx iteration",
				18, 1, 500);
	}
}
