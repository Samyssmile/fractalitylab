package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Magnet Type I fractal images using the iteration ((z² + c - 1) / (2z + c - 2))²,
 * which converges to fixed point 1.
 */
public final class MagnetTypeOneGenerator implements FractalGenerator {

	private static final double ESCAPE_RADIUS_SQUARED = 100.0;
	private static final double CONVERGENCE_THRESHOLD = 1e-6;
	private static final double VIEW_SCALE = 4.0;

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

				while (iter < maxIterations) {
					double mod2 = zRe * zRe + zIm * zIm;
					if (mod2 > ESCAPE_RADIUS_SQUARED) break;

					double diffRe = zRe - 1.0;
					double diffIm = zIm;
					if (diffRe * diffRe + diffIm * diffIm < CONVERGENCE_THRESHOLD) break;

					// numerator = z² + c - 1
					double numRe = zRe * zRe - zIm * zIm + cRe - 1.0;
					double numIm = 2.0 * zRe * zIm + cIm;

					// denominator = 2z + c - 2
					double denRe = 2.0 * zRe + cRe - 2.0;
					double denIm = 2.0 * zIm + cIm;

					double denMod2 = denRe * denRe + denIm * denIm;
					if (denMod2 < 1e-12) break;

					// division: num / den
					double divRe = (numRe * denRe + numIm * denIm) / denMod2;
					double divIm = (numIm * denRe - numRe * denIm) / denMod2;

					// square the result
					zRe = divRe * divRe - divIm * divIm;
					zIm = 2.0 * divRe * divIm;
					iter++;
				}

				if (iter < maxIterations && iter > 0) {
					float hue = hueOffset + (float) iter / maxIterations;
					float brightness = 1.0f - 0.5f * iter / maxIterations;
					pixels[rowOffset + x] = Color.HSBtoRGB(hue, 0.85f, brightness);
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
		return "magnettypeone";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Magnet Type I",
				"Magnet fractal from ((z² + c - 1) / (2z + c - 2))² iteration",
				18, 5, 300);
	}
}
