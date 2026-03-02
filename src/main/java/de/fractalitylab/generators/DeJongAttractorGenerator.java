package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Peter de Jong attractor fractal images using the iteration:
 * x_{n+1} = sin(a·y_n) - cos(b·x_n), y_{n+1} = sin(c·x_n) - cos(d·y_n).
 * Rendered as a log-scaled density histogram.
 */
public final class DeJongAttractorGenerator implements FractalGenerator {

	private static final int POINTS_MULTIPLIER = 100;

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

		double a = random.nextDouble() * 6.0 - 3.0;
		double b = random.nextDouble() * 6.0 - 3.0;
		double c = random.nextDouble() * 6.0 - 3.0;
		double d = random.nextDouble() * 6.0 - 3.0;
		float hueBase = random.nextFloat();

		int totalPoints = width * height * POINTS_MULTIPLIER;
		int[][] density = new int[width][height];

		double x = 0.1, y = 0.1;
		double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

		for (int i = 0; i < 1000; i++) {
			double newX = Math.sin(a * y) - Math.cos(b * x);
			double newY = Math.sin(c * x) - Math.cos(d * y);
			x = newX;
			y = newY;
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}

		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		if (rangeX < 0.01) rangeX = 1;
		if (rangeY < 0.01) rangeY = 1;

		int maxDensity = 1;
		for (int i = 0; i < totalPoints; i++) {
			double newX = Math.sin(a * y) - Math.cos(b * x);
			double newY = Math.sin(c * x) - Math.cos(d * y);
			x = newX;
			y = newY;

			int px = (int) ((x - minX) / rangeX * (width - 1));
			int py = (int) ((y - minY) / rangeY * (height - 1));
			px = Math.clamp(px, 0, width - 1);
			py = Math.clamp(py, 0, height - 1);

			density[px][py]++;
			if (density[px][py] > maxDensity) maxDensity = density[px][py];
		}

		double logMax = Math.log1p(maxDensity);
		int[] pixels = new int[width * height];
		for (int px = 0; px < width; px++) {
			for (int py = 0; py < height; py++) {
				if (density[px][py] > 0) {
					float brightness = (float) (Math.log1p(density[px][py]) / logMax);
					float hue = hueBase + 0.4f * brightness;
					pixels[py * width + px] = Color.HSBtoRGB(hue, 0.75f, brightness);
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
		return "dejong";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("De Jong Attractor",
				"Peter de Jong strange attractor with sin/cos iteration",
				18, 1, 500);
	}
}
