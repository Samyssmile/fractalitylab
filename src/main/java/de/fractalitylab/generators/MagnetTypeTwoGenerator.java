package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Magnet Type II fractal images using a higher-order magnet formula:
 * z = ((z³ + 3(c-1)z + (c-1)(c-2)) / (3z² + 3(c-2)z + (c-1)(c-2) + 1))².
 */
public final class MagnetTypeTwoGenerator implements FractalGenerator {

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

				// precompute (c-1) and (c-2)
				double c1Re = cRe - 1.0, c1Im = cIm;
				double c2Re = cRe - 2.0, c2Im = cIm;

				// (c-1)(c-2)
				double c1c2Re = c1Re * c2Re - c1Im * c2Im;
				double c1c2Im = c1Re * c2Im + c1Im * c2Re;

				while (iter < maxIterations) {
					double mod2 = zRe * zRe + zIm * zIm;
					if (mod2 > ESCAPE_RADIUS_SQUARED) break;

					double diffRe = zRe - 1.0;
					double diffIm = zIm;
					if (diffRe * diffRe + diffIm * diffIm < CONVERGENCE_THRESHOLD) break;

					// z³
					double z2Re = zRe * zRe - zIm * zIm;
					double z2Im = 2.0 * zRe * zIm;
					double z3Re = z2Re * zRe - z2Im * zIm;
					double z3Im = z2Re * zIm + z2Im * zRe;

					// 3(c-1)z
					double t1Re = 3.0 * (c1Re * zRe - c1Im * zIm);
					double t1Im = 3.0 * (c1Re * zIm + c1Im * zRe);

					// numerator = z³ + 3(c-1)z + (c-1)(c-2)
					double numRe = z3Re + t1Re + c1c2Re;
					double numIm = z3Im + t1Im + c1c2Im;

					// 3z²
					double t2Re = 3.0 * z2Re;
					double t2Im = 3.0 * z2Im;

					// 3(c-2)z
					double t3Re = 3.0 * (c2Re * zRe - c2Im * zIm);
					double t3Im = 3.0 * (c2Re * zIm + c2Im * zRe);

					// denominator = 3z² + 3(c-2)z + (c-1)(c-2) + 1
					double denRe = t2Re + t3Re + c1c2Re + 1.0;
					double denIm = t2Im + t3Im + c1c2Im;

					double denMod2 = denRe * denRe + denIm * denIm;
					if (denMod2 < 1e-12) break;

					double divRe = (numRe * denRe + numIm * denIm) / denMod2;
					double divIm = (numIm * denRe - numRe * denIm) / denMod2;

					zRe = divRe * divRe - divIm * divIm;
					zIm = 2.0 * divRe * divIm;
					iter++;
				}

				if (iter < maxIterations && iter > 0) {
					float hue = hueOffset + (float) iter / maxIterations;
					float brightness = 1.0f - 0.4f * iter / maxIterations;
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
		return "magnettypetwo";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Magnet Type II",
				"Higher-order magnet fractal with cubic numerator/denominator",
				18, 5, 300);
	}
}
