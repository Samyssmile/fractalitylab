package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Generates Lyapunov fractal images by computing Lyapunov exponents over a logistic map
 * with alternating parameters from a binary sequence string (e.g. "AABB").
 */
public final class LyapunovGenerator implements FractalGenerator {

	private static final String[] SEQUENCES = {"AB", "AABB", "ABAB", "BBBBBBAAAAAA", "AAABBB"};
	private static final int WARMUP = 100;
	private static final double X_MIN = 2.0;
	private static final double X_MAX = 4.0;
	private static final double Y_MIN = 2.0;
	private static final double Y_MAX = 4.0;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int[] pixels = new int[width * height];

		String sequence = SEQUENCES[random.nextInt(SEQUENCES.length)];
		int seqLen = sequence.length();

		double xOff = (random.nextDouble() - 0.5) * 0.3;
		double yOff = (random.nextDouble() - 0.5) * 0.3;

		IntStream.range(0, height).forEach(row -> {
			int rowOffset = row * width;
			for (int col = 0; col < width; col++) {
				double a = X_MIN + (X_MAX - X_MIN) * col / width + xOff;
				double b = Y_MIN + (Y_MAX - Y_MIN) * row / height + yOff;
				a = Math.clamp(a, 0.01, 4.0);
				b = Math.clamp(b, 0.01, 4.0);

				double x = 0.5;
				// warmup
				for (int i = 0; i < WARMUP; i++) {
					double r = (sequence.charAt(i % seqLen) == 'A') ? a : b;
					x = r * x * (1.0 - x);
					x = Math.clamp(x, 1e-10, 1.0 - 1e-10);
				}

				double lyapunov = 0;
				for (int i = 0; i < maxIterations; i++) {
					double r = (sequence.charAt(i % seqLen) == 'A') ? a : b;
					x = r * x * (1.0 - x);
					x = Math.clamp(x, 1e-10, 1.0 - 1e-10);
					double derivative = Math.abs(r * (1.0 - 2.0 * x));
					if (derivative > 0) {
						lyapunov += Math.log(derivative);
					}
				}
				lyapunov /= maxIterations;

				int rgb;
				if (lyapunov < 0) {
					float intensity = (float) Math.clamp(-lyapunov / 2.0, 0.0, 1.0);
					rgb = Color.HSBtoRGB(0.6f, intensity, 1.0f);
				} else if (lyapunov > 0) {
					float intensity = (float) Math.clamp(lyapunov / 2.0, 0.0, 1.0);
					rgb = Color.HSBtoRGB(0.0f, intensity, 1.0f - 0.8f * intensity);
				} else {
					rgb = Color.BLACK.getRGB();
				}
				pixels[rowOffset + col] = rgb;
			}
		});
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	@Override
	public String label() {
		return "lyapunov";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Lyapunov Fractal",
				"Lyapunov exponent over a logistic map with alternating parameters",
				50, 10, 500);
	}
}
