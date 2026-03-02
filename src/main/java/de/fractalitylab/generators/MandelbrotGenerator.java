package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Mandelbrot set fractal images with randomized viewport parameters.
 * Uses retry logic to ensure generated images contain visible fractal structures.
 */
public class MandelbrotGenerator implements FractalGenerator {

	private static final int MAX_RETRIES = 50;
	private static final double ZOOM_BASE = 1000.0;
	private static final double ZOOM_RANGE = 10.0;
	private static final double MOVE_X_BASE = -0.7;
	private static final double MOVE_X_RANGE = 0.7;
	private static final double MOVE_Y_RANGE = 0.7;
	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final float HUE_SHIFT = 0.5f;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
			int[] pixels = new int[width * height];
			double zoom = ZOOM_BASE + random.nextDouble() * ZOOM_RANGE;
			double moveX = MOVE_X_BASE + random.nextDouble() * MOVE_X_RANGE;
			double moveY = random.nextDouble() * MOVE_Y_RANGE;

			int iters = maxIterations;
			IntStream.range(0, height).forEach(y -> {
				int rowOffset = y * width;
				for (int x = 0; x < width; x++) {
					double zx = (x - width / 2.0) / zoom + moveX;
					double zy = (y - height / 2.0) / zoom + moveY;
					double cX = zx;
					double cY = zy;
					int iter = 0;
					while ((zx * zx + zy * zy < ESCAPE_RADIUS_SQUARED) && (iter < iters)) {
						double tmp = zx * zx - zy * zy + cX;
						zy = 2.0 * zx * zy + cY;
						zx = tmp;
						iter++;
					}
					pixels[rowOffset + x] = Color.HSBtoRGB((float) iter / iters + (iter % 2) * HUE_SHIFT, 1, iter < iters ? 1 : 0);
				}
			});

			if (containsFractal(pixels)) {
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				image.setRGB(0, 0, width, height, pixels, 0, width);
				return image;
			}
		}
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	@Override
	public String label() {
		return "mandelbrot";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Mandelbrot Set",
				"Classic escape-time fractal based on z = z² + c iteration",
				18, 5, 500);
	}

	private boolean containsFractal(int[] pixels) {
		int blackRgb = Color.BLACK.getRGB();
		for (int pixel : pixels) {
			if (pixel != blackRgb) {
				return true;
			}
		}
		return false;
	}
}
