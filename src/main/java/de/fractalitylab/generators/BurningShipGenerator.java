package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Burning Ship fractal images with randomized rotation and viewport.
 */
public class BurningShipGenerator implements FractalGenerator {

	private static final double ZOOM_MIN = 30.0;
	private static final double ZOOM_RANGE = 500.0;
	private static final double MOVE_X_CENTER = -0.5;
	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final float HUE_BASE = 0.05f;
	private static final float HUE_RANGE = 0.95f;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double rotationAngle = random.nextDouble() * Math.PI * 2;
		double zoom = ZOOM_MIN + random.nextDouble() * ZOOM_RANGE;
		double moveX = MOVE_X_CENTER + (random.nextDouble() - 0.5) / zoom;
		double moveY = (random.nextDouble() - 0.5) / zoom;

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double newX = (x - width / 2.0) / zoom;
				double newY = (y - height / 2.0) / zoom;
				double cosA = Math.cos(rotationAngle);
				double sinA = Math.sin(rotationAngle);
				double rx = cosA * newX - sinA * newY;
				double ry = sinA * newX + cosA * newY;

				double zx = rx + moveX;
				double zy = ry + moveY;
				double cRe = zx;
				double cIm = zy;
				int iter = 0;

				while (zx * zx + zy * zy < ESCAPE_RADIUS_SQUARED && iter < maxIterations) {
					double tmp = zx * zx - zy * zy + cRe;
					zy = Math.abs(2.0 * zx * zy) + cIm;
					zx = tmp;
					iter++;
				}

				if (iter < maxIterations) {
					double mu = iter - Math.log(Math.log(zx * zx + zy * zy)) / Math.log(2);
					mu = Math.max(mu, 0);
					float hue = HUE_BASE + HUE_RANGE * (float) (maxIterations - mu) / maxIterations;
					float brightness = (float) Math.sqrt(mu / maxIterations);
					pixels[rowOffset + x] = Color.HSBtoRGB(hue, 1.0f, brightness);
				} else {
					pixels[rowOffset + x] = Color.BLACK.getRGB();
				}
			}
		});
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "burningship";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Burning Ship",
				"Variant of Mandelbrot using absolute values in iteration",
				18, 5, 500);
	}
}
