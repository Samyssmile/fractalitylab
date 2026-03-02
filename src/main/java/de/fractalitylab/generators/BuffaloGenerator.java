package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Buffalo fractal images using the abs-variant iteration:
 * z_{n+1} = |Re(z_n²)| + i·|Im(z_n²)| + c
 * which expands to: |zRe² - zIm²| + i·|2·zRe·zIm| + c.
 */
public final class BuffaloGenerator implements FractalGenerator {

	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final double VIEW_SCALE = 3.5;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double offsetX = (random.nextDouble() - 0.5) * 0.5;
		double offsetY = (random.nextDouble() - 0.5) * 0.5;
		float hueOffset = random.nextFloat();

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double cRe = (x - width / 2.0) * VIEW_SCALE / width + offsetX;
				double cIm = (y - height / 2.0) * VIEW_SCALE / height + offsetY;
				double zRe = 0, zIm = 0;
				int iter = 0;

				while (zRe * zRe + zIm * zIm < ESCAPE_RADIUS_SQUARED && iter < maxIterations) {
					double newRe = Math.abs(zRe * zRe - zIm * zIm) + cRe;
					double newIm = Math.abs(2.0 * zRe * zIm) + cIm;
					zRe = newRe;
					zIm = newIm;
					iter++;
				}

				if (iter < maxIterations) {
					float hue = hueOffset + (float) iter / maxIterations;
					pixels[rowOffset + x] = Color.HSBtoRGB(hue, 0.9f, 1.0f);
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
		return "buffalo";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Buffalo Fractal",
				"Abs-variant Mandelbrot with horn-like structures",
				18, 5, 500);
	}
}
