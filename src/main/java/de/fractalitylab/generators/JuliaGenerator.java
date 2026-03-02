package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Julia set fractal images with randomized c-parameter and hue shift.
 */
public class JuliaGenerator implements FractalGenerator {

	private static final double C_RANGE = 2.0;
	private static final double VIEW_SCALE = 3.0;
	private static final double ESCAPE_RADIUS_SQUARED = 4.0;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		double cRe = random.nextDouble() * C_RANGE - 1;
		double cIm = random.nextDouble() * C_RANGE - 1;
		int hueShift = random.nextInt(maxIterations);

		IntStream.range(0, height).forEach(y -> {
			int rowOffset = y * width;
			for (int x = 0; x < width; x++) {
				double zx = (x - width / 2.0) * (VIEW_SCALE / width);
				double zy = (y - height / 2.0) * (VIEW_SCALE / height);
				int iter = 0;

				while (zx * zx + zy * zy < ESCAPE_RADIUS_SQUARED && iter < maxIterations) {
					double tmp = zx * zx - zy * zy + cRe;
					zy = 2.0 * zx * zy + cIm;
					zx = tmp;
					iter++;
				}

				float hue = ((iter + hueShift) % maxIterations) / (float) maxIterations;
				pixels[rowOffset + x] = (iter < maxIterations)
						? Color.HSBtoRGB(hue, 1, iter % 2)
						: Color.BLACK.getRGB();
			}
		});
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "julia";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Julia Set",
				"Escape-time fractal with a fixed complex c-parameter",
				18, 5, 500);
	}
}
