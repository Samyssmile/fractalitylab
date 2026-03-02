package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Dragon Curve fractal images using an L-system with turtle graphics.
 * Axiom: FX, Rules: X -> X+YF+, Y -> -FX-Y. Angle: 90 degrees.
 */
public final class DragonCurveGenerator implements FractalGenerator {

	private static final int BASE_ITERATIONS = 12;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int iterations = BASE_ITERATIONS + random.nextInt(4);
		float hueOffset = random.nextFloat();

		// build L-system string using fold sequence
		boolean[] turns = buildTurns(iterations);
		int totalSteps = turns.length + 1;

		// trace path to find bounds
		double[] xs = new double[totalSteps + 1];
		double[] ys = new double[totalSteps + 1];
		double x = 0, y = 0, angle = 0;
		xs[0] = x;
		ys[0] = y;

		for (int i = 0; i < turns.length; i++) {
			x += Math.cos(angle);
			y += Math.sin(angle);
			xs[i + 1] = x;
			ys[i + 1] = y;
			angle += turns[i] ? Math.PI / 2 : -Math.PI / 2;
		}
		x += Math.cos(angle);
		y += Math.sin(angle);
		xs[totalSteps] = x;
		ys[totalSteps] = y;

		// find bounds
		double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (int i = 0; i <= totalSteps; i++) {
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

		for (int i = 0; i < totalSteps; i++) {
			float hue = hueOffset + (float) i / totalSteps * 0.6f;
			g.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
			int px1 = (int) (xs[i] * scale + offX);
			int py1 = (int) (ys[i] * scale + offY);
			int px2 = (int) (xs[i + 1] * scale + offX);
			int py2 = (int) (ys[i + 1] * scale + offY);
			g.drawLine(px1, py1, px2, py2);
		}

		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "dragoncurve";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Dragon Curve",
				"L-system space-filling curve with 90° turns",
				18, 1, 100);
	}

	private boolean[] buildTurns(int iterations) {
		int len = (1 << iterations) - 1;
		boolean[] turns = new boolean[len];
		for (int i = 0; i < len; i++) {
			// bit-based: turn direction at step i is based on the bit above the lowest set bit
			int bit = ((i + 1) & (-(i + 1))) << 1;
			turns[i] = ((i + 1) & bit) == 0;
		}
		return turns;
	}
}
