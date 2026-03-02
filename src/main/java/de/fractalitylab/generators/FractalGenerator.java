package de.fractalitylab.generators;

import java.awt.image.BufferedImage;

/**
 * Generates a single fractal image. Implementations are stateless and thread-safe.
 */
public interface FractalGenerator {

	/**
	 * Generates one fractal image with the given dimensions and iteration depth.
	 *
	 * @param width         image width in pixels
	 * @param height        image height in pixels
	 * @param maxIterations maximum iteration count for the fractal algorithm
	 * @return rendered fractal image
	 */
	BufferedImage generate(int width, int height, int maxIterations);

	/**
	 * @return the class label used for dataset organization (e.g. "mandelbrot", "julia")
	 */
	String label();

	/**
	 * @return metadata describing this generator's characteristics and recommended iteration bounds
	 */
	default FractalMetadata metadata() {
		return new FractalMetadata(label(), "", 18, 1, 1000);
	}
}
