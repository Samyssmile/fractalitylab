package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Vicsek fractal images via plus-shaped recursive subdivision
 * (center and 4 edge-midpoints retained, 4 corners removed).
 */
public final class VicsekGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 4;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int maxSafe = maxDepthForSize(Math.min(width, height));
		int depth = Math.min(BASE_DEPTH + random.nextInt(3), maxSafe);
		float hueOffset = random.nextFloat();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		drawVicsek(g, 0, 0, width, height, depth, 0, depth, hueOffset);
		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "vicsek";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Vicsek Fractal",
				"Plus-shaped self-similar fractal via recursive subdivision",
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

	private void drawVicsek(Graphics2D g, int x, int y, int w, int h, int depth,
	                         int currentDepth, int maxDepth, float hueOffset) {
		if (depth == 0 || w < 1 || h < 1) {
			float hue = hueOffset + (float) currentDepth / maxDepth * 0.5f;
			float brightness = 0.6f + 0.4f * (1.0f - (float) currentDepth / maxDepth);
			g.setColor(Color.getHSBColor(hue, 0.75f, brightness));
			g.fillRect(x, y, w, h);
			return;
		}

		int subW = w / 3;
		int subH = h / 3;
		// center
		drawVicsek(g, x + subW, y + subH, subW, subH, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		// top
		drawVicsek(g, x + subW, y, subW, subH, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		// bottom
		drawVicsek(g, x + subW, y + 2 * subH, subW, subH, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		// left
		drawVicsek(g, x, y + subH, subW, subH, depth - 1, currentDepth + 1, maxDepth, hueOffset);
		// right
		drawVicsek(g, x + 2 * subW, y + subH, subW, subH, depth - 1, currentDepth + 1, maxDepth, hueOffset);
	}
}
