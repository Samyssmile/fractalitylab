package de.fractalitylab.config;

import java.nio.file.Path;
import java.util.List;

/**
 * Immutable configuration holding all CLI parameters for fractal generation.
 *
 * @param numberOfImages     number of images to generate per fractal class
 * @param size               pixel dimension (width = height)
 * @param quality            image quality (0 = heavily degraded, 100 = pristine)
 * @param maxIterations      base iteration count for fractal computation
 * @param ratio              train/test split ratio (0.0 to 1.0)
 * @param outputDir          output directory for generated dataset
 * @param selectedGenerators generator labels to use (empty = all)
 */
public record Configuration(
		int numberOfImages,
		int size,
		int quality,
		int maxIterations,
		double ratio,
		Path outputDir,
		List<String> selectedGenerators
) {

	public Configuration {
		if (numberOfImages <= 0)
			throw new IllegalArgumentException("numberOfImages must be positive, got: " + numberOfImages);
		if (size <= 0)
			throw new IllegalArgumentException("size must be positive, got: " + size);
		if (quality < 0 || quality > 100)
			throw new IllegalArgumentException("quality must be 0-100, got: " + quality);
		if (maxIterations <= 0)
			throw new IllegalArgumentException("maxIterations must be positive, got: " + maxIterations);
		if (ratio < 0.0 || ratio > 1.0)
			throw new IllegalArgumentException("ratio must be 0.0-1.0, got: " + ratio);
		if (outputDir == null)
			throw new IllegalArgumentException("outputDir must not be null");
		selectedGenerators = selectedGenerators == null ? List.of() : List.copyOf(selectedGenerators);
	}

	/**
	 * Convenience constructor for backwards compatibility (uses default outputDir and all generators).
	 */
	public Configuration(int numberOfImages, int size, int quality, int maxIterations, double ratio) {
		this(numberOfImages, size, quality, maxIterations, ratio, Path.of("dataset"), List.of());
	}

	public int width() {
		return size;
	}

	public int height() {
		return size;
	}

	/**
	 * Formats the configuration for display, including resolved generator statistics.
	 *
	 * @param classCount  number of active fractal classes
	 * @param totalImages total number of images to generate
	 * @return formatted configuration string
	 */
	public String toDisplayString(int classCount, int totalImages) {
		var sb = new StringBuilder();
		sb.append("\nConfiguration {\n");
		sb.append("  numberOfImages     : ").append(numberOfImages).append(",\n");
		sb.append("  size               : ").append(size).append("x").append(size).append(",\n");
		sb.append("  quality            : ").append(quality).append(",\n");
		sb.append("  maxIterations      : ").append(maxIterations).append(",\n");
		sb.append("  ratio (train/test) : ").append(ratio).append(",\n");
		sb.append("  outputDir          : ").append(outputDir).append(",\n");
		sb.append("  generators         : ").append(selectedGenerators.isEmpty() ? "all" : String.join(", ", selectedGenerators)).append(",\n");
		sb.append("  classes            : ").append(classCount).append(",\n");
		sb.append("  total images       : ").append(totalImages).append("\n");
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String toString() {
		return toDisplayString(0, 0);
	}
}
