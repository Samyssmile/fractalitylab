package de.fractalitylab.data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageWriter {

	private ImageWriter() {
	}

	/**
	 * Writes an image to the dataset directory structure.
	 *
	 * @param className    fractal class label (subdirectory name)
	 * @param fileName     file name without extension
	 * @param image        the image to write
	 * @param isTrainImage true for train split, false for test split
	 * @param outputDir    base output directory
	 */
	public static void writeImage(String className, String fileName, BufferedImage image,
	                               boolean isTrainImage, Path outputDir) {
		String split = isTrainImage ? "train" : "test";
		Path targetFolder = outputDir.resolve(split).resolve(className);
		try {
			Files.createDirectories(targetFolder);
			ImageIO.write(image, "png", targetFolder.resolve(fileName + ".png").toFile());
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to write image: " + fileName, e);
		}
	}

	/**
	 * Writes an image using the default "dataset" output directory.
	 */
	public static void writeImage(String className, String fileName, BufferedImage image,
	                               boolean isTrainImage) {
		writeImage(className, fileName, image, isTrainImage, Path.of("dataset"));
	}
}
