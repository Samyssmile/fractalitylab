package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates T-Square fractal images by recursively placing squares at each corner
 * of the parent square, each half the parent's side length.
 */
public final class TSquareGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 6;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int depth = BASE_DEPTH + random.nextInt(4);
		float hueOffset = random.nextFloat();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		int side = Math.min(width, height) / 2;
		int startX = (width - side) / 2;
		int startY = (height - side) / 2;

		drawTSquare(g, startX, startY, side, depth, 0, depth, hueOffset);
		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "tsquare";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("T-Square Fractal",
				"Recursive fractal with overlapping corner squares",
				18, 1, 100);
	}

	private void drawTSquare(Graphics2D g, int x, int y, int side, int depth,
	                          int currentDepth, int maxDepth, float hueOffset) {
		if (depth == 0 || side < 1) return;

		float hue = hueOffset + (float) currentDepth / maxDepth * 0.5f;
		float brightness = 0.5f + 0.5f * (1.0f - (float) currentDepth / maxDepth);
		g.setColor(Color.getHSBColor(hue, 0.7f, brightness));
		g.fillRect(x, y, side, side);

		int half = side / 2;
		drawTSquare(g, x - half / 2, y - half / 2, half, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		drawTSquare(g, x + side - half / 2, y - half / 2, half, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		drawTSquare(g, x - half / 2, y + side - half / 2, half, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		drawTSquare(g, x + side - half / 2, y + side - half / 2, half, depth - 1, currentDepth + 1, maxDepth, hueOffset);
	}
}
