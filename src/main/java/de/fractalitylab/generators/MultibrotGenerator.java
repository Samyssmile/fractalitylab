package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Multibrot set fractal images using z^d + c with a randomly chosen integer exponent d in [3, 6].
 */
public final class MultibrotGenerator implements FractalGenerator {

	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final double VIEW_SCALE = 3.0;
	private static final int MIN_DEGREE = 3;
	private static final int MAX_DEGREE_EXCLUSIVE = 7;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		int degree = random.nextInt(MIN_DEGREE, MAX_DEGREE_EXCLUSIVE);
		double offsetX = (random.nextDouble() - 0.5) * 0.4;
		double offsetY = (random.nextDouble() - 0.5) * 0.4;
		float hueOffset = random.nextFloat();

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double cRe = (x - width / 2.0) * VIEW_SCALE / width + offsetX;
				double cIm = (y - height / 2.0) * VIEW_SCALE / height + offsetY;
				double zRe = 0, zIm = 0;
				int iter = 0;

				while (zRe * zRe + zIm * zIm < ESCAPE_RADIUS_SQUARED && iter < maxIterations) {
					Complex z = new Complex(zRe, zIm).pow(degree);
					zRe = z.re() + cRe;
					zIm = z.im() + cIm;
					iter++;
				}

				if (iter < maxIterations) {
					float hue = hueOffset + (float) iter / maxIterations;
					pixels[rowOffset + x] = Color.HSBtoRGB(hue, 0.8f, 1.0f);
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
		return "multibrot";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Multibrot Set",
				"Generalized Mandelbrot with z^d + c where d is randomly 3-6",
				18, 5, 500);
	}
}
