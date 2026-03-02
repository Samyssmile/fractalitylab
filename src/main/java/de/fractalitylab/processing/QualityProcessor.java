package de.fractalitylab.processing;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies post-processing quality adjustments to fractal images:
 * Gaussian blur, noise injection, and random rotation.
 */
public final class QualityProcessor {

	private static final float BLUR_DIVISOR = 10.0f;
	private static final float NOISE_DIVISOR = 200.0f;

	private QualityProcessor() {
	}

	/**
	 * Applies blur and noise based on the quality level.
	 * quality=100 produces pristine images, quality=0 produces heavily degraded ones.
	 *
	 * @param image   source image
	 * @param quality degradation level (0 = heavy, 100 = pristine)
	 * @return processed image
	 */
	public static BufferedImage applyQualityAdjustments(BufferedImage image, int quality) {
		float blurRadius = (100 - quality) / BLUR_DIVISOR;
		if (blurRadius > 0) {
			image = applyGaussianBlur(image, blurRadius);
		}
		if (quality < 100) {
			image = addNoise(image, (100 - quality) / NOISE_DIVISOR);
		}
		return image;
	}

	/**
	 * Rotates the image by a random angle (0-360 degrees).
	 */
	public static BufferedImage rotateImage(BufferedImage originalImage) {
		double angle = ThreadLocalRandom.current().nextDouble() * 360;
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		BufferedImage rotatedImage = new BufferedImage(w, h, originalImage.getType());
		Graphics2D g2d = rotatedImage.createGraphics();
		AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(angle), w / 2.0, h / 2.0);
		g2d.setTransform(at);
		g2d.drawImage(originalImage, 0, 0, null);
		g2d.dispose();
		return rotatedImage;
	}

	static BufferedImage applyGaussianBlur(BufferedImage src, float radius) {
		int radiusInt = (int) Math.ceil(radius);
		int size = radiusInt * 2 + 1;
		float sigma = radius / 3;
		float sigma22 = 2 * sigma * sigma;
		float radius2 = radius * radius;

		// compute 1D kernel
		float[] kernel1D = new float[size];
		float total = 0;
		for (int i = -radiusInt; i <= radiusInt; i++) {
			float distance = i * i;
			if (distance > radius2) {
				kernel1D[i + radiusInt] = 0;
			} else {
				kernel1D[i + radiusInt] = (float) Math.exp(-distance / sigma22);
			}
			total += kernel1D[i + radiusInt];
		}
		for (int i = 0; i < size; i++) {
			kernel1D[i] /= total;
		}

		// horizontal pass
		Kernel hKernel = new Kernel(size, 1, kernel1D);
		ConvolveOp hOp = new ConvolveOp(hKernel, ConvolveOp.EDGE_NO_OP, null);
		BufferedImage temp = hOp.filter(src, null);

		// vertical pass
		Kernel vKernel = new Kernel(1, size, kernel1D);
		ConvolveOp vOp = new ConvolveOp(vKernel, ConvolveOp.EDGE_NO_OP, null);
		return vOp.filter(temp, null);
	}

	static BufferedImage addNoise(BufferedImage image, float noiseLevel) {
		int width = image.getWidth();
		int height = image.getHeight();
		ThreadLocalRandom rand = ThreadLocalRandom.current();

		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		for (int i = 0; i < pixels.length; i++) {
			int rgb = pixels[i];
			int a = (rgb >> 24) & 0xff;
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = rgb & 0xff;

			int nr = clamp(r + (int) ((rand.nextFloat() - 0.5f) * 255 * noiseLevel));
			int ng = clamp(g + (int) ((rand.nextFloat() - 0.5f) * 255 * noiseLevel));
			int nb = clamp(b + (int) ((rand.nextFloat() - 0.5f) * 255 * noiseLevel));

			pixels[i] = (a << 24) | (nr << 16) | (ng << 8) | nb;
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	static int clamp(int value) {
		return Math.clamp(value, 0, 255);
	}
}
