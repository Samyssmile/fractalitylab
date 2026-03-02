package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Barnsley Fern fractal images using the chaos game with 4 affine transforms,
 * rendered as a density map.
 */
public final class BarnsleyFernGenerator implements FractalGenerator {

	private static final int POINTS_MULTIPLIER = 50;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int[][] density = new int[width][height];
		int totalPoints = width * height * POINTS_MULTIPLIER;
		float hueBase = random.nextFloat();
		double rotation = random.nextDouble() * 0.1 - 0.05;

		double x = 0, y = 0;
		int maxDensity = 1;

		for (int i = 0; i < totalPoints; i++) {
			double r = random.nextDouble();
			double newX, newY;

			if (r < 0.01) {
				newX = 0;
				newY = 0.16 * y;
			} else if (r < 0.86) {
				newX = 0.85 * x + (0.04 + rotation) * y;
				newY = -0.04 * x + 0.85 * y + 1.6;
			} else if (r < 0.93) {
				newX = 0.20 * x - 0.26 * y;
				newY = 0.23 * x + 0.22 * y + 1.6;
			} else {
				newX = -0.15 * x + 0.28 * y;
				newY = 0.26 * x + 0.24 * y + 0.44;
			}
			x = newX;
			y = newY;

			int px = (int) ((x + 2.5) / 6.0 * width);
			int py = height - 1 - (int) (y / 10.5 * height);
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
					float hue = hueBase + 0.25f * brightness;
					pixels[py * width + px] = Color.HSBtoRGB(hue, 0.8f, brightness);
				}
			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "barnsleyfern";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Barnsley Fern",
				"IFS fractal using 4 affine transforms via chaos game",
				18, 1, 500);
	}
}
