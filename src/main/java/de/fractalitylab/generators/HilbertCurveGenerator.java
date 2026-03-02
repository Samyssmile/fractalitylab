package de.fractalitylab.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates Hilbert space-filling curve fractal images using L-system turtle graphics.
 * Axiom: A, Rules: A -> -BF+AFA+FB-, B -> +AF-BFB-FA+. Angle: 90 degrees.
 */
public final class HilbertCurveGenerator implements FractalGenerator {

	private static final int BASE_ORDER = 4;

	@Override
	public BufferedImage generate(int width, int height, int maxIterations) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int order = BASE_ORDER + random.nextInt(3);
		float hueOffset = random.nextFloat();

		int n = 1 << order;
		int totalPoints = n * n;
		int[] xs = new int[totalPoints];
		int[] ys = new int[totalPoints];

		for (int i = 0; i < totalPoints; i++) {
			int[] coords = d2xy(order, i);
			xs[i] = coords[0];
			ys[i] = coords[1];
		}

		double margin = 0.05;
		double cellW = width * (1 - 2 * margin) / (n - 1);
		double cellH = height * (1 - 2 * margin) / (n - 1);
		double offX = width * margin;
		double offY = height * margin;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setStroke(new BasicStroke(1.5f));

		for (int i = 0; i < totalPoints - 1; i++) {
			float hue = hueOffset + (float) i / totalPoints * 0.7f;
			g.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
			int px1 = (int) (xs[i] * cellW + offX);
			int py1 = (int) (ys[i] * cellH + offY);
			int px2 = (int) (xs[i + 1] * cellW + offX);
			int py2 = (int) (ys[i + 1] * cellH + offY);
			g.drawLine(px1, py1, px2, py2);
		}

		g.dispose();
		return image;
	}

	@Override
	public String label() {
		return "hilbertcurve";
	}

	@Override
	public FractalMetadata metadata() {
		return new FractalMetadata("Hilbert Curve",
				"Space-filling curve that visits every cell in a grid",
				18, 1, 100);
	}

	/**
	 * Converts a 1D index to 2D Hilbert curve coordinates using bit manipulation.
	 */
	private int[] d2xy(int order, int d) {
		int x = 0, y = 0;
		for (int s = 1; s < (1 << order); s <<= 1) {
			int rx = (d / 2) & 1;
			int ry = (d & 1) ^ rx;
			if (ry == 0) {
				if (rx == 1) {
					x = s - 1 - x;
					y = s - 1 - y;
				}
				int tmp = x;
				x = y;
				y = tmp;
			}
			x += s * rx;
			y += s * ry;
			d /= 4;
		}
		return new int[]{x, y};
	}
}
