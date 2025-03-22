package de.fractalitylab.config;

import java.util.StringJoiner;

public  class Configuration {
	private final int    numberOfImages;
	private final int    size;
	private final int    quality;
	private final int    maxIterations;
	private final double ratio;

	public Configuration(int numberOfImages, int size, int quality, int maxIterations, double ratio) {
		this.numberOfImages = numberOfImages;
		this.size = size;
		this.quality = quality;
		this.maxIterations = maxIterations;
		this.ratio = ratio;
	}

	public int getNumberOfImages() {
		return numberOfImages;
	}

	public int getWidth() {
		return size;
	}

	public int getHeight() {
		return size;
	}

	public int getQuality() {
		return quality;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public double getRatio() {
		return ratio;
	}

	@Override
	public String toString() {
		return "\n" + Configuration.class.getSimpleName() + " {\n" +
				"  numberOfImages     : " + numberOfImages + ",\n" +
				"  size               : " + size + "x" + size + ",\n" +
				"  quality            : " + quality + ",\n" +
				"  maxIterations      : " + maxIterations + ",\n" +
				"  ratio (train/test) : " + ratio + "\n" +
				"}";
	}


}