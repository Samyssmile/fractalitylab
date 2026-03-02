package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Phoenix fractal images using the iteration z_{n+1} = z_n^2 + Re(c) + Im(c) * z_{n-1}.
 */
public final class PhoenixGenerator implements FractalGenerator {

	private static final double ESCAPE_RADIUS_SQUARED = 4.0;
	private static final double VIEW_SCALE = 3.0;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double cRe = random.nextDouble() * 1.0 - 0.5;
		double cIm = random.nextDouble() * 0.8 - 0.4;
		float hueOffset = random.nextFloat();

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double zRe = (x - width / 2.0) * VIEW_SCALE / width;
				double zIm = (y - height / 2.0) * VIEW_SCALE / height;
				double prevRe = 0, prevIm = 0;
				int iter = 0;

				while (zRe * zRe + zIm * zIm < ESCAPE_RADIUS_SQUARED && iter < maxIterations) {
					double newRe = zRe * zRe - zIm * zIm + cRe + cIm * prevRe;
					double newIm = 2.0 * zRe * zIm + cIm * prevIm;
					prevRe = zRe;
					prevIm = zIm;
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
		return "phoenix";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Phoenix Fractal",
				"Fractal using z² + Re(c) + Im(c) * z_{n-1} iteration with memory",
				18, 5, 500);
	}
}
