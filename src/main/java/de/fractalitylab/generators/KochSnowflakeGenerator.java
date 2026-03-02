package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Koch Snowflake fractal images by recursively subdividing each edge
 * of an equilateral triangle with triangular spikes.
 */
public final class KochSnowflakeGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 4;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int depth = BASE_DEPTH + random.nextInt(3);
		float hueOffset = random.nextFloat();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		double margin = 0.1;
		double side = Math.min(width, height) * (1 - 2 * margin);
		double cx = width / 2.0;
		double cy = height / 2.0;
		double h3 = side * Math.sqrt(3) / 2.0;

		// equilateral triangle vertices
		double x1 = cx - side / 2.0, y1 = cy + h3 / 3.0;
		double x2 = cx + side / 2.0, y2 = y1;
		double x3 = cx, y3 = cy - 2.0 * h3 / 3.0;

		g.setStroke(new BasicStroke(1.5f));
		drawKochEdge(g, x1, y1, x2, y2, depth, 0, depth, hueOffset);
		drawKochEdge(g, x2, y2, x3, y3, depth, 0, depth, hueOffset);
		drawKochEdge(g, x3, y3, x1, y1, depth, 0, depth, hueOffset);

		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "kochsnowflake";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Koch Snowflake",
				"Equilateral triangle with recursive triangular spikes on each edge",
				18, 1, 100);
	}

	private void drawKochEdge(Graphics2D g, double x1, double y1, double x2, double y2,
	                           int depth, int currentDepth, int maxDepth, float hueOffset) {
		if (depth == 0) {
			float hue = hueOffset + (float) currentDepth / maxDepth * 0.5f;
			float brightness = 0.6f + 0.4f * (1.0f - (float) currentDepth / maxDepth);
			g.setColor(Color.getHSBColor(hue, 0.8f, brightness));
			g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
			return;
		}

		double dx = (x2 - x1) / 3.0;
		double dy = (y2 - y1) / 3.0;

		double ax = x1 + dx, ay = y1 + dy;
		double bx = x1 + 2 * dx, by = y1 + 2 * dy;

		// peak of the spike (equilateral triangle)
		double px = (ax + bx) / 2.0 - (by - ay) * Math.sqrt(3) / 2.0;
		double py = (ay + by) / 2.0 + (bx - ax) * Math.sqrt(3) / 2.0;

		drawKochEdge(g, x1, y1, ax, ay, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		drawKochEdge(g, ax, ay, px, py, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		drawKochEdge(g, px, py, bx, by, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		drawKochEdge(g, bx, by, x2, y2, depth - 1, currentDepth + 1, maxDepth, hueOffset);
	}
}
