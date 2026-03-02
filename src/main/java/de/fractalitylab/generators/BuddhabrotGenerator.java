package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Buddhabrot fractal images using a density-map approach:
 * samples random c-values, traces escaping Mandelbrot trajectories,
 * and accumulates visit counts into a histogram.
 */
public final class BuddhabrotGenerator implements FractalGenerator {

	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final double VIEW_MIN = -2.0;
	private static final double VIEW_MAX = 2.0;
	private static final int SAMPLES_PER_PIXEL = 8;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[][] histogram = new int[width][height];
		int totalSamples = width * height * SAMPLES_PER_PIXEL;
		double viewRange = VIEW_MAX - VIEW_MIN;

		var random = ThreadLocalRandom.current();
		for (int s = 0; s < totalSamples; s++) {
			double cRe = VIEW_MIN + random.nextDouble() * viewRange;
			double cIm = VIEW_MIN + random.nextDouble() * viewRange;

			double zRe = 0, zIm = 0;
			int escapeIter = 0;
			double[] trajRe = new double[maxIterations];
			double[] trajIm = new double[maxIterations];

			for (int i = 0; i < maxIterations; i++) {
				double tmp = zRe * zRe - zIm * zIm + cRe;
				zIm = 2.0 * zRe * zIm + cIm;
				zRe = tmp;
				trajRe[i] = zRe;
				trajIm[i] = zIm;
				escapeIter = i + 1;
				if (zRe * zRe + zIm * zIm > ESCAPE_RADIUS_SQUARED) break;
			}

			if (zRe * zRe + zIm * zIm > ESCAPE_RADIUS_SQUARED) {
				for (int i = 0; i < escapeIter; i++) {
					int px = (int) ((trajRe[i] - VIEW_MIN) / viewRange * width);
					int py = (int) ((trajIm[i] - VIEW_MIN) / viewRange * height);
					if (px >= 0 && px < width && py >= 0 && py < height) {
						histogram[px][py]++;
					}
				}
			}
		}

		// find maximum for normalization
		int maxCount = 1;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (histogram[x][y] > maxCount) maxCount = histogram[x][y];
			}
		}

		double logMax = Math.log1p(maxCount);
		float hueBase = ThreadLocalRandom.current().nextFloat();
		int[] pixels = new int[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (histogram[x][y] > 0) {
					float brightness = (float) (Math.log1p(histogram[x][y]) / logMax);
					float hue = hueBase + 0.3f * brightness;
					pixels[y * width + x] = Color.HSBtoRGB(hue, 0.7f, brightness);
				}
			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "buddhabrot";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Buddhabrot",
				"Density map of escaping Mandelbrot trajectories",
				50, 10, 1000);
	}
}
