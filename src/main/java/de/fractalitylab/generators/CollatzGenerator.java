package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Collatz fractal images using the continuous extension:
 * f(z) = ¼(2 + 7z - (2 + 5z)cos(πz)).
 */
public final class CollatzGenerator implements FractalGenerator {

	private static final double ESCAPE_RADIUS_SQUARED = 100.0;
	private static final double VIEW_SCALE = 6.0;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double offsetX = (random.nextDouble() - 0.5) * 2.0;
		double offsetY = (random.nextDouble() - 0.5) * 2.0;
		float hueOffset = random.nextFloat();

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double zRe = (x - width / 2.0) * VIEW_SCALE / width + offsetX;
				double zIm = (y - height / 2.0) * VIEW_SCALE / height + offsetY;
				int iter = 0;

				while (zRe * zRe + zIm * zIm < ESCAPE_RADIUS_SQUARED && iter < maxIterations) {
					// cos(π·z) = cos(π·Re)·cosh(π·Im) - i·sin(π·Re)·sinh(π·Im)
					double piRe = Math.PI * zRe;
					double piIm = Math.PI * zIm;
					double cosRe = Math.cos(piRe) * Math.cosh(piIm);
					double cosIm = -Math.sin(piRe) * Math.sinh(piIm);

					// (2 + 5z)
					double aRe = 2.0 + 5.0 * zRe;
					double aIm = 5.0 * zIm;

					// (2 + 5z) * cos(πz)
					double prodRe = aRe * cosRe - aIm * cosIm;
					double prodIm = aRe * cosIm + aIm * cosRe;

					// ¼(2 + 7z - prod)
					double newRe = 0.25 * (2.0 + 7.0 * zRe - prodRe);
					double newIm = 0.25 * (7.0 * zIm - prodIm);
					zRe = newRe;
					zIm = newIm;
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
		return "collatz";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Collatz Fractal",
				"Continuous extension of the Collatz conjecture to the complex plane",
				18, 5, 200);
	}
}
