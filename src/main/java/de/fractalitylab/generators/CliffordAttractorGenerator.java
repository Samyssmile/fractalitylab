package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Clifford attractor fractal images using the iteration:
 * x_{n+1} = sin(a·y_n) + c·cos(a·x_n), y_{n+1} = sin(b·x_n) + d·cos(b·y_n).
 * Rendered as a log-scaled density histogram.
 */
public final class CliffordAttractorGenerator implements FractalGenerator {

	private static final int POINTS_MULTIPLIER = 100;
	private static final int WARMUP_ITERATIONS = 500;
	private static final int BOUNDS_ITERATIONS = 50_000;
	private static final double MIN_RANGE_THRESHOLD = 1.0;
	private static final double BOUNDS_PADDING = 0.05;
	private static final int MAX_PARAM_ATTEMPTS = 50;
	private static final double MIN_PIXEL_COVERAGE = 0.02;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		float hueBase = random.nextFloat();

		double a = 0, b = 0, c = 0, d = 0;
		double minX = 0, maxX = 0, minY = 0, maxY = 0;
		double x = 0, y = 0;

		// find parameters that produce a non-degenerate attractor with good pixel coverage
		int attempts = 0;
		boolean validAttractor;
		do {
			a = random.nextDouble() * 4.0 - 2.0;
			b = random.nextDouble() * 4.0 - 2.0;
			c = random.nextDouble() * 4.0 - 2.0;
			d = random.nextDouble() * 4.0 - 2.0;

			x = 0.1;
			y = 0.1;

			// skip transient phase so orbit settles onto the attractor
			for (int i = 0; i < WARMUP_ITERATIONS; i++) {
				double newX = Math.sin(a * y) + c * Math.cos(a * x);
				double newY = Math.sin(b * x) + d * Math.cos(b * y);
				x = newX;
				y = newY;
			}

			// check for NaN/Infinity divergence
			if (!Double.isFinite(x) || !Double.isFinite(y)) {
				attempts++;
				validAttractor = false;
				continue;
			}

			// trace attractor to find bounds
			minX = Double.MAX_VALUE;
			maxX = -Double.MAX_VALUE;
			minY = Double.MAX_VALUE;
			maxY = -Double.MAX_VALUE;
			boolean diverged = false;
			for (int i = 0; i < BOUNDS_ITERATIONS; i++) {
				double newX = Math.sin(a * y) + c * Math.cos(a * x);
				double newY = Math.sin(b * x) + d * Math.cos(b * y);
				x = newX;
				y = newY;
				if (!Double.isFinite(x) || !Double.isFinite(y)) {
					diverged = true;
					break;
				}
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}

			if (diverged || maxX - minX < MIN_RANGE_THRESHOLD || maxY - minY < MIN_RANGE_THRESHOLD) {
				attempts++;
				validAttractor = false;
				continue;
			}

			// estimate pixel coverage with a quick sample
			double trialRangeX = (maxX - minX) * (1 + 2 * BOUNDS_PADDING);
			double trialRangeY = (maxY - minY) * (1 + 2 * BOUNDS_PADDING);
			double trialMinX = minX - (maxX - minX) * BOUNDS_PADDING;
			double trialMinY = minY - (maxY - minY) * BOUNDS_PADDING;
			var occupied = new java.util.HashSet<Long>();
			int samplePoints = width * height;
			for (int i = 0; i < samplePoints; i++) {
				double newX = Math.sin(a * y) + c * Math.cos(a * x);
				double newY = Math.sin(b * x) + d * Math.cos(b * y);
				x = newX;
				y = newY;
				int px = (int) ((x - trialMinX) / trialRangeX * (width - 1));
				int py = (int) ((y - trialMinY) / trialRangeY * (height - 1));
				if (px >= 0 && px < width && py >= 0 && py < height) {
					occupied.add((long) px * height + py);
				}
			}
			double coverage = (double) occupied.size() / (width * height);
			validAttractor = coverage >= MIN_PIXEL_COVERAGE;
			attempts++;
		} while (!validAttractor && attempts < MAX_PARAM_ATTEMPTS);

		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		if (rangeX < MIN_RANGE_THRESHOLD) rangeX = MIN_RANGE_THRESHOLD;
		if (rangeY < MIN_RANGE_THRESHOLD) rangeY = MIN_RANGE_THRESHOLD;

		// add padding so points at edges are not clipped
		double padX = rangeX * BOUNDS_PADDING;
		double padY = rangeY * BOUNDS_PADDING;
		minX -= padX;
		maxX += padX;
		minY -= padY;
		maxY += padY;
		rangeX = maxX - minX;
		rangeY = maxY - minY;

		int totalPoints = width * height * POINTS_MULTIPLIER;
		int[][] density = new int[width][height];
		int maxDensity = 1;

		for (int i = 0; i < totalPoints; i++) {
			double newX = Math.sin(a * y) + c * Math.cos(a * x);
			double newY = Math.sin(b * x) + d * Math.cos(b * y);
			x = newX;
			y = newY;

			int px = (int) ((x - minX) / rangeX * (width - 1));
			int py = (int) ((y - minY) / rangeY * (height - 1));
			if (px < 0 || px >= width || py < 0 || py >= height) continue;

			density[px][py]++;
			if (density[px][py] > maxDensity) maxDensity = density[px][py];
		}

		double logMax = Math.log1p(maxDensity);
		int[] pixels = new int[width * height];
		for (int px = 0; px < width; px++) {
			for (int py = 0; py < height; py++) {
				if (density[px][py] > 0) {
					float brightness = (float) (Math.log1p(density[px][py]) / logMax);
					float hue = hueBase + 0.3f * brightness;
					pixels[py * width + px] = Color.HSBtoRGB(hue, 0.7f, brightness);
				}
			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "clifford";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Clifford Attractor",
				"Strange attractor using sin/cos iteration with 4 parameters",
				18, 1, 500);
	}
}
