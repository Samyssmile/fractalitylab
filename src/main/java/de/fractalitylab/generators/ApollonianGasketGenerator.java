package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Apollonian Gasket fractal images via recursive circle packing.
 * Three mutually tangent circles are recursively filled with the inscribed fourth circle.
 */
public final class ApollonianGasketGenerator implements FractalGenerator {

	private static final int BASE_DEPTH = 5;
	private static final double MIN_RADIUS_RATIO = 0.002;

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

		double cx = width / 2.0;
		double cy = height / 2.0;
		double outerR = Math.min(width, height) * 0.45;
		double minRadius = outerR * MIN_RADIUS_RATIO;

		// outer circle
		g.setColor(Color.getHSBColor(hueOffset, 0.5f, 0.3f));
		g.drawOval((int) (cx - outerR), (int) (cy - outerR), (int) (2 * outerR), (int) (2 * outerR));

		// three inner mutually tangent circles (Descartes configuration)
		double innerR = outerR / (1 + 2 / Math.sqrt(3));
		double dist = outerR - innerR;
		double[][] centers = new double[3][2];
		for (int i = 0; i < 3; i++) {
			double angle = Math.PI / 2 + i * 2 * Math.PI / 3;
			centers[i][0] = cx + dist * Math.cos(angle);
			centers[i][1] = cy + dist * Math.sin(angle);
		}

		for (int i = 0; i < 3; i++) {
			drawCircle(g, centers[i][0], centers[i][1], innerR, 0, depth, hueOffset);
		}

		// fill the gaps recursively
		fillGap(g, cx, cy, outerR, centers[0][0], centers[0][1], innerR,
				centers[1][0], centers[1][1], innerR, centers[2][0], centers[2][1], innerR,
				depth, 1, depth, hueOffset, minRadius);

		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "apollonian";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Apollonian Gasket",
				"Recursive circle packing fractal based on Descartes' theorem",
				18, 1, 100);
	}

	private void drawCircle(Graphics2D g, double cx, double cy, double r,
	                         int currentDepth, int maxDepth, float hueOffset) {
		float hue = hueOffset + (float) currentDepth / maxDepth * 0.5f;
		float brightness = 0.5f + 0.5f * (1.0f - (float) currentDepth / maxDepth);
		g.setColor(Color.getHSBColor(hue, 0.7f, brightness));
		g.drawOval((int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r));
	}

	private void fillGap(Graphics2D g, double outerCx, double outerCy, double outerR,
	                      double cx1, double cy1, double r1,
	                      double cx2, double cy2, double r2,
	                      double cx3, double cy3, double r3,
	                      int depth, int currentDepth, int maxDepth,
	                      float hueOffset, double minRadius) {
		if (depth == 0) return;

		// Descartes circle theorem: k4 = k1 + k2 + k3 + 2*sqrt(k1*k2 + k2*k3 + k1*k3)
		double k1 = -1.0 / outerR; // outer circle has negative curvature
		double k2 = 1.0 / r1;
		double k3 = 1.0 / r2;
		double k4_candidate = k2 + k3 + (-Math.abs(k1)) + 2.0 * Math.sqrt(Math.abs(k2 * k3 + k3 * (-Math.abs(k1)) + k2 * (-Math.abs(k1))));

		if (k4_candidate <= 0) return;
		double newR = 1.0 / k4_candidate;
		if (newR < minRadius) return;

		// approximate center via weighted midpoint
		double totalW = k2 + k3 + Math.abs(k1);
		double newCx = (k2 * cx1 + k3 * cx2 + Math.abs(k1) * outerCx) / totalW;
		double newCy = (k2 * cy1 + k3 * cy2 + Math.abs(k1) * outerCy) / totalW;

		drawCircle(g, newCx, newCy, newR, currentDepth, maxDepth, hueOffset);

		fillGap(g, outerCx, outerCy, outerR, newCx, newCy, newR, cx2, cy2, r2, cx3, cy3, r3,
				depth - 1, currentDepth + 1, maxDepth, hueOffset, minRadius);
		fillGap(g, outerCx, outerCy, outerR, cx1, cy1, r1, newCx, newCy, newR, cx3, cy3, r3,
				depth - 1, currentDepth + 1, maxDepth, hueOffset, minRadius);
		fillGap(g, outerCx, outerCy, outerR, cx1, cy1, r1, cx2, cy2, r2, newCx, newCy, newR,
				depth - 1, currentDepth + 1, maxDepth, hueOffset, minRadius);
	}
}
