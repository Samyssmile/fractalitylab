package de.fractalitylab.generators;

import de.fractalitylab.FractalityLab;
import de.fractalitylab.data.ImageWriter;
import de.fractalitylab.data.DataElement;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public class TricornGenerator implements ImageGenerator {
	private static final Logger LOGGER = Logger.getLogger(TricornGenerator.class.getName());
	private              Random random = new Random();

	@Override
	public List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages, int quality,
	                                       boolean isTrain) {
		List<DataElement> dataElements = Collections.synchronizedList(new ArrayList<>());
		for (int i = 0; i < numberOfImages; i++) {
			double offsetX = (random.nextDouble() - 0.5) * 0.5;
			double offsetY = (random.nextDouble() - 0.5) * 0.5;

			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double zx         = 3 * (x - width / 2 + offsetX * width) / (double) width;
					double zy         = 3 * (height / 2 - y + offsetY * height) / (double) height;
					float  brightness = computeTricorn(zx, zy, maxIterations);
					float  hue        = 0.95f + 10 * brightness + (float) i / numberOfImages;
					int    color      = Color.HSBtoRGB(hue, 0.6f, brightness);
					image.setRGB(x, y, color);
				}
			}

			image = rotateImage(image);
			image = applyQualityAdjustments(image, quality);
			UUID uuid = UUID.randomUUID();
			ImageWriter.writeImage("tricorn", uuid.toString(), image, isTrain);
			dataElements.add(new DataElement(uuid.toString(), "tricorn"));
		}
		LOGGER.info(dataElements.size() + " Tricorn generation finished.");
		return dataElements;
	}

	private float computeTricorn(double zx, double zy, int maxIterations) {
		double zxx       = zx;
		double zyy       = zy;
		int    iteration = 0;
		while (zx * zx + zy * zy < 4 && iteration < maxIterations) {
			double xtemp = zx * zx - zy * zy + zxx;
			zy = -2 * zx * zy + zyy;
			zx = xtemp;
			iteration++;
		}
		return iteration < maxIterations ? (float) iteration / maxIterations : 0;
	}


}
