package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Pythagoras Tree fractal images by recursively drawing squares
 * that branch into two smaller squares on the hypotenuse of a right triangle.
 */
public final class PythagorasTreeGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 8;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int depth = BASE_DEPTH + random.nextInt(5);
		float hueOffset = random.nextFloat();
		double angle = Math.PI / 4 + (random.nextDouble() - 0.5) * 0.3;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		double sideLen = width / 5.0;
		double startX = width / 2.0 - sideLen / 2.0;
		double startY = height * 0.85;

		drawTree(g, startX, startY, startX + sideLen, startY, depth, 0, depth, hueOffset, angle);
		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "pythagorastree";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Pythagoras Tree",
				"Recursive squares branching on hypotenuse of right triangles",
				18, 1, 100);
	}

	private void drawTree(Graphics2D g, double x1, double y1, double x2, double y2,
	                       int depth, int currentDepth, int maxDepth, float hueOffset, double angle) {
		if (depth == 0) return;

		double dx = x2 - x1;
		double dy = y2 - y1;

		// square corners: (x1,y1) -> (x2,y2) -> (x3,y3) -> (x4,y4)
		double x3 = x2 - dy;
		double y3 = y2 + dx;
		double x4 = x1 - dy;
		double y4 = y1 + dx;

		float hue = hueOffset + (float) currentDepth / maxDepth * 0.4f;
		float brightness = 0.9f - 0.4f * currentDepth / maxDepth;
		g.setColor(Color.getHSBColor(hue, 0.7f, brightness));
		g.fillPolygon(
				new int[]{(int) x1, (int) x2, (int) x3, (int) x4},
				new int[]{(int) y1, (int) y2, (int) y3, (int) y4},
				4
		);

		// apex of the triangle
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double apexX = x4 + (x3 - x4) * cos * cos + (y4 - y3) * cos * sin;
		double apexY = y4 + (x3 - x4) * cos * sin - (y4 - y3) * cos * cos;

		drawTree(g, x4, y4, apexX, apexY, depth - 1, currentDepth + 1, maxDepth, hueOffset, angle);
		drawTree(g, apexX, apexY, x3, y3, depth - 1, currentDepth + 1, maxDepth, hueOffset, angle);
	}
}
