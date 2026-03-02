package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Sierpinski Carpet fractal images via recursive square subdivision,
 * removing the central square at each level.
 */
public final class SierpinskiCarpetGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 4;
	private static final float BRIGHTNESS_BASE = 0.5f;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int maxSafe = maxDepthForSize(Math.min(width, height));
		int depth = Math.min(BASE_DEPTH + random.nextInt(3), maxSafe);
		float hueOffset = random.nextFloat();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		drawCarpet(g, 0, 0, width, height, depth, 0, depth, hueOffset);
		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "sierpinskicarpet";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Sierpinski Carpet",
				"Square-based self-similar fractal via recursive subdivision",
				18, 1, 100);
	}

	private int maxDepthForSize(int size) {
		int depth = 0;
		while (size / 3 >= 1) {
			size /= 3;
			depth++;
		}
		return Math.max(depth, 1);
	}

	private void drawCarpet(Graphics2D g, int x, int y, int w, int h, int depth,
	                         int currentDepth, int maxDepth, float hueOffset) {
		if (depth == 0 || w < 1 || h < 1) {
			float hue = hueOffset + (float) currentDepth / maxDepth * 0.5f;
			float brightness = BRIGHTNESS_BASE + 0.5f * (1.0f - (float) currentDepth / maxDepth);
			g.setColor(Color.getHSBColor(hue, 0.8f, brightness));
			g.fillRect(x, y, w, h);
			return;
		}

		int subW = w / 3;
		int subH = h / 3;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (row == 1 && col == 1) continue; // remove center
				drawCarpet(g, x + col * subW, y + row * subH, subW, subH,
						depth - 1, currentDepth + 1, maxDepth, hueOffset);
			}
		}
	}
}
