package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Levy C Curve fractal images via recursive 45-degree rotation subdivision.
 */
public final class LevyCurveGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 12;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int depth = BASE_DEPTH + random.nextInt(4);
		float hueOffset = random.nextFloat();

		// precompute path points
		int totalSegments = 1 << depth;
		double[] xs = new double[totalSegments + 1];
		double[] ys = new double[totalSegments + 1];

		// start and end
		xs[0] = 0;
		ys[0] = 0;
		xs[totalSegments] = 1;
		ys[totalSegments] = 0;

		subdivide(xs, ys, 0, totalSegments, depth);

		// find bounds
		double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (int i = 0; i <= totalSegments; i++) {
			minX = Math.min(minX, xs[i]);
			maxX = Math.max(maxX, xs[i]);
			minY = Math.min(minY, ys[i]);
			maxY = Math.max(maxY, ys[i]);
		}

		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		double margin = 0.1;
		double scale = Math.min(width * (1 - 2 * margin) / rangeX, height * (1 - 2 * margin) / rangeY);
		double offX = (width - rangeX * scale) / 2 - minX * scale;
		double offY = (height - rangeY * scale) / 2 - minY * scale;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setStroke(new BasicStroke(1.5f));

		for (int i = 0; i < totalSegments; i++) {
			float hue = hueOffset + (float) i / totalSegments * 0.6f;
			g.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
			g.drawLine((int) (xs[i] * scale + offX), (int) (ys[i] * scale + offY),
					(int) (xs[i + 1] * scale + offX), (int) (ys[i + 1] * scale + offY));
		}

		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "levycurve";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Levy C Curve",
				"Self-similar curve with 45° rotation at each subdivision",
				18, 1, 100);
	}

	private void subdivide(double[] xs, double[] ys, int start, int end, int depth) {
		if (depth == 0) return;

		int mid = (start + end) / 2;
		double dx = xs[end] - xs[start];
		double dy = ys[end] - ys[start];

		xs[mid] = xs[start] + (dx - dy) / 2.0;
		ys[mid] = ys[start] + (dx + dy) / 2.0;

		subdivide(xs, ys, start, mid, depth - 1);
		subdivide(xs, ys, mid, end, depth - 1);
	}
}
