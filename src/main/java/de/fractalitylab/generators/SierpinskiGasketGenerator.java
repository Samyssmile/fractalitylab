package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Sierpinski Gasket (triangle) fractal images via recursive subdivision.
 */
public class SierpinskiGasketGenerator implements FractalGenerator {

	private static final double ZOOM_MIN = 0.5;
	private static final double ZOOM_MAX = 1.5;
	private static final int BASE_DEPTH = 5;
	private static final float BRIGHTNESS_BASE = 0.5f;
	private static final float BRIGHTNESS_RANGE = 0.5f;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		double zoomFactor = ThreadLocalRandom.current().nextDouble(ZOOM_MIN, ZOOM_MAX);
		int depth = depthFromZoom(zoomFactor);
		int maxDepth = depthFromZoom(ZOOM_MAX);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		drawTriangle(g, 0, height, width, height, width / 2, 0, depth, 0, maxDepth, width, height);
		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "sierpinski";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Sierpinski Gasket",
				"Self-similar triangular fractal via recursive subdivision",
				18, 1, 100);
	}

	private void drawTriangle(Graphics2D g, int x1, int y1, int x2, int y2, int x3, int y3,
	                           int depth, int currentDepth, int maxDepth, int maxWidth, int maxHeight) {
		int centerX = maxWidth / 2;
		int centerY = maxHeight / 2;

		if (depth == 0) {
			float hue = (float) currentDepth / maxDepth;
			float distToCenter = (float) Math.sqrt(
					Math.pow((x1 + x2 + x3) / 3.0 - centerX, 2) +
					Math.pow((y1 + y2 + y3) / 3.0 - centerY, 2));
			float maxDist = (float) Math.sqrt(centerX * centerX + centerY * centerY);
			float saturation = 1.0f - (distToCenter / maxDist);
			float brightness = BRIGHTNESS_BASE + BRIGHTNESS_RANGE * ((maxWidth - distToCenter) / maxWidth);

			g.setColor(Color.getHSBColor(hue, saturation, brightness));
			g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
		} else {
			int mx1 = (x1 + x2) / 2, my1 = (y1 + y2) / 2;
			int mx2 = (x2 + x3) / 2, my2 = (y2 + y3) / 2;
			int mx3 = (x3 + x1) / 2, my3 = (y3 + y1) / 2;
			drawTriangle(g, x1, y1, mx1, my1, mx3, my3, depth - 1, currentDepth + 1, maxDepth, maxWidth, maxHeight);
			drawTriangle(g, mx1, my1, x2, y2, mx2, my2, depth - 1, currentDepth + 1, maxDepth, maxWidth, maxHeight);
			drawTriangle(g, mx3, my3, mx2, my2, x3, y3, depth - 1, currentDepth + 1, maxDepth, maxWidth, maxHeight);
		}
	}

	private int depthFromZoom(double zoomFactor) {
		return (int) (Math.log(zoomFactor) / Math.log(0.5) + BASE_DEPTH);
	}
}
