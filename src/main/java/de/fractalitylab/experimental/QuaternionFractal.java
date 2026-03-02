package de.fractalitylab.experimental;

import org.apache.commons.math3.complex.Quaternion;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Experimental 4D quaternion fractal generator.
 * Not yet integrated as a {@code FractalGenerator} — reserved for Phase 2.
 */
public class QuaternionFractal {

	private static final Logger LOGGER = Logger.getLogger(QuaternionFractal.class.getName());

	private static final int WIDTH = 512;
	private static final int HEIGHT = 512;
	private static final int MAX_ITER = 1000;
	private static final double ZOOM = 1.0;
	private static final double ESCAPE_RADIUS = 2.0;

	public BufferedImage generateFractal() {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				double zx = 1.5 * (x - WIDTH / 2) / (0.5 * ZOOM * WIDTH);
				double zy = (y - HEIGHT / 2) / (0.5 * ZOOM * HEIGHT);
				float brightness = iterate(new Quaternion(zx, zy, 0.0, 0.0));
				int color = calculateColor(brightness);
				image.setRGB(x, y, color);
			}
		}

		return image;
	}

	/**
	 * Saves the fractal image to the specified file path.
	 *
	 * @param image    the image to save
	 * @param filename target file path
	 */
	public void saveImage(BufferedImage image, String filename) {
		try {
			ImageIO.write(image, "png", Path.of(filename).toFile());
			LOGGER.info("Fractal image saved as " + filename);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to save quaternion fractal image: " + filename, e);
		}
	}

	private float iterate(Quaternion z) {
		Quaternion c = new Quaternion(z.getQ0(), z.getQ1(), z.getQ2(), z.getQ3());
		int i;
		for (i = 0; i < MAX_ITER; i++) {
			if (z.getNorm() > ESCAPE_RADIUS) break;
			z = z.multiply(z).add(c);
		}
		return (float) i / MAX_ITER;
	}

	private int calculateColor(float brightness) {
		int channel = (int) (brightness * 255);
		return (0xFF << 24) | (channel << 16) | (channel << 8) | channel;
	}
}
