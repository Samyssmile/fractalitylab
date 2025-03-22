package de.fractalitylab;

import de.fractalitylab.config.Configuration;
import de.fractalitylab.data.CSVWriter;
import de.fractalitylab.data.DataElement;
import de.fractalitylab.generators.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FractalityLab {

	private static final Logger        LOGGER                   = Logger.getLogger(FractalityLab.class.getName());
	private static final int           DEFAULT_NUMBER_OF_IMAGES = 100;
	private static final int           DEFAULT_SIZE             = 256;
	private static final int           DEFAULT_MAX_ITERATIONS   = 18;
	private static final int           DEFAULT_QUALITY          = 80;
	private static final double        DEFAULT_RATIO            = 0.8;
	private static       Configuration config;

	public static void main(String[] args) {
		config = parseArguments(args);
		printConfig(config);
		LOGGER.info("Starting FractalityLab generators...");

		ImageGenerator[] imageGenerators = {
				new TricornGenerator(),
				new MandelbrotGenerator(),
				new JuliaGenerator(),
				new BurningShipGenerator(),
				new SierpinskiGasketGenerator(),
				new NewtonFractalGenerator(),
		};

		String trainFolder = "dataset/train";
		String testFolder = "dataset/test";

		List<DataElement> trainDataList = generateAllDataElements(imageGenerators, true);
		List<DataElement> testDataList = generateAllDataElements(imageGenerators, false);

		LOGGER.info("All images generated.");


		new CSVWriter().writeToCSV(trainFolder ,"images.csv", trainDataList);
		new CSVWriter().writeToCSV(testFolder ,"images.csv", testDataList);

		LOGGER.info("All images generated and CSV file created.");
	}

	private static void printConfig(Configuration config) {
		LOGGER.info(config.toString());
	}

	private static List<DataElement> generateAllDataElements(ImageGenerator[] imageGenerators, boolean isTrainData) {
		return List.of(imageGenerators).parallelStream()
				.flatMap(imageGenerator -> imageGenerator.generateImage(
						config.getWidth(),
						config.getHeight(),
						config.getMaxIterations(),
						calculateNumberOfImages(config.getNumberOfImages(), config.getRatio(), isTrainData),
						config.getQuality(),
						isTrainData
				).stream())
				.collect(Collectors.toCollection(CopyOnWriteArrayList::new));
	}

	private static int calculateNumberOfImages(int numberOfImages, double ratio, boolean isTrainData) {
		if (isTrainData) {
			return (int) (numberOfImages * ratio);
		}
		return (int) (numberOfImages * (1 - ratio));

	}

	private static Configuration parseArguments(String[] args) {
		int    numberOfImages = DEFAULT_NUMBER_OF_IMAGES;
		int    size           = DEFAULT_SIZE;
		int    quality        = DEFAULT_QUALITY;
		int    maxIterations  = DEFAULT_MAX_ITERATIONS;
		double ratio          = DEFAULT_RATIO;

		try {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "--number":
						if (i + 1 < args.length) {
							numberOfImages = Integer.parseInt(args[++i]);
						} else {
							LOGGER.warning("No value provided for --number. Using default: " + DEFAULT_NUMBER_OF_IMAGES);
						}
						break;
					case "--size":
						if (i + 1 < args.length) {
							size = Integer.parseInt(args[++i]);
						} else {
							LOGGER.warning("No value provided for --size. Using default: " + DEFAULT_SIZE);
						}
						break;
					case "--quality":
						if (i + 1 < args.length) {
							quality = Integer.parseInt(args[++i]);
							if (quality < 0 || quality > 100) {
								LOGGER.warning("Quality must be between 0 and 100. Using default: " + DEFAULT_QUALITY);
								quality = DEFAULT_QUALITY;
							}
						} else {
							LOGGER.warning("No value provided for --quality. Using default: " + DEFAULT_QUALITY);
						}
						break;
					case "--trainTestRatio":
						if (i + 1 < args.length) {
							ratio = Double.parseDouble(args[++i]);
							if (ratio < 0 || ratio > 1) {
								LOGGER.warning("Ratio must be between 0 and 1. Using default: " + DEFAULT_RATIO);
								ratio = DEFAULT_RATIO;
							}
						} else {
							LOGGER.warning("No value provided for --ratio. Using default: " + DEFAULT_RATIO);
						}
						break;
					default:
						LOGGER.warning("Unknown argument: " + args[i]);
						break;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.severe("Invalid number format for arguments. Using default values.");
		}

		return new Configuration(numberOfImages, size, quality, maxIterations, ratio);
	}


}