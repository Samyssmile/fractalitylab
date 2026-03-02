package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Tricorn (Mandelbar) fractal images using complex conjugate iteration.
 */
public class TricornGenerator implements FractalGenerator {

	private static final double OFFSET_RANGE = 0.5;
	private static final double VIEW_SCALE = 3.0;
	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final float HUE_BASE = 0.95f;
	private static final float HUE_SCALE = 10.0f;
	private static final float SATURATION = 0.6f;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double offsetX = (random.nextDouble() - 0.5) * OFFSET_RANGE;
		double offsetY = (random.nextDouble() - 0.5) * OFFSET_RANGE;
		float hueOffset = random.nextFloat();

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double zx = VIEW_SCALE * (x - width / 2.0 + offsetX * width) / width;
				double zy = VIEW_SCALE * (height / 2.0 - y + offsetY * height) / height;
				float brightness = computeTricorn(zx, zy, maxIterations);
				float hue = HUE_BASE + HUE_SCALE * brightness + hueOffset;
				pixels[rowOffset + x] = Color.HSBtoRGB(hue, SATURATION, brightness);
			}
		});
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "tricorn";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Tricorn",
				"Complex conjugate variant of the Mandelbrot set (Mandelbar)",
				18, 5, 500);
	}

	private float computeTricorn(double zx, double zy, int maxIterations) {
		double cX = zx;
		double cY = zy;
		int iteration = 0;
		while (zx * zx + zy * zy < ESCAPE_RADIUS_SQUARED && iteration < maxIterations) {
			double xtemp = zx * zx - zy * zy + cX;
			zy = -2 * zx * zy + cY;
			zx = xtemp;
			iteration++;
		}
		return iteration < maxIterations ? (float) iteration / maxIterations : 0;
	}
}
