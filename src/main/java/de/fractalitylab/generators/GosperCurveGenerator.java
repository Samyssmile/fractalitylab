package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Gosper curve (flowsnake) fractal images using an L-system with turtle graphics.
 * Axiom: A, Rules: A -> A-B--B+A++AA+B-, B -> +A-BB--B-A++A+B. Angle: 60 degrees.
 */
public final class GosperCurveGenerator implements FractalGenerator {

	private static final int BASE_ORDER = 3;
	private static final double ANGLE_DEG = 60.0;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int order = BASE_ORDER + random.nextInt(2);
		float hueOffset = random.nextFloat();

		String instructions = buildLSystem(order);

		// trace path
		double x = 0, y = 0;
		double angle = 0;
		double step = 1.0;
		double angleRad = Math.toRadians(ANGLE_DEG);

		int segmentCount = 0;
		for (int i = 0; i < instructions.length(); i++) {
			char c = instructions.charAt(i);
			if (c == 'A' || c == 'B') segmentCount++;
		}

		double[] xs = new double[segmentCount + 1];
		double[] ys = new double[segmentCount + 1];
		int idx = 0;
		xs[0] = x;
		ys[0] = y;

		for (int i = 0; i < instructions.length(); i++) {
			switch (instructions.charAt(i)) {
				case 'A', 'B' -> {
					x += step * Math.cos(angle);
					y += step * Math.sin(angle);
					idx++;
					xs[idx] = x;
					ys[idx] = y;
				}
				case '+' -> angle += angleRad;
				case '-' -> angle -= angleRad;
				default -> { }
			}
		}

		// find bounds
		double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		for (int i = 0; i <= idx; i++) {
			minX = Math.min(minX, xs[i]);
			maxX = Math.max(maxX, xs[i]);
			minY = Math.min(minY, ys[i]);
			maxY = Math.max(maxY, ys[i]);
		}

		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		if (rangeX == 0) rangeX = 1;
		if (rangeY == 0) rangeY = 1;
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

		for (int i = 0; i < idx; i++) {
			float hue = hueOffset + (float) i / idx * 0.7f;
			g.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
			g.drawLine((int) (xs[i] * scale + offX), (int) (ys[i] * scale + offY),
					(int) (xs[i + 1] * scale + offX), (int) (ys[i + 1] * scale + offY));
		}

		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "gospercurve";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Gosper Curve",
				"Hexagonal space-filling L-system curve (flowsnake)",
				18, 1, 100);
	}

	private String buildLSystem(int order) {
		String current = "A";
		for (int i = 0; i < order; i++) {
			StringBuilder next = new StringBuilder(current.length() * 7);
			for (int j = 0; j < current.length(); j++) {
				switch (current.charAt(j)) {
					case 'A' -> next.append("A-B--B+A++AA+B-");
					case 'B' -> next.append("+A-BB--B-A++A+B");
					default -> next.append(current.charAt(j));
				}
			}
			current = next.toString();
		}
		return current;
	}
}
