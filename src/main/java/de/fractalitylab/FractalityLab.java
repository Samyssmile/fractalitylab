package de.fractalitylab;

import de.fractalitylab.data.CSVWriter;
import de.fractalitylab.data.DataElement;
import de.fractalitylab.generators.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FractalityLab {

    private static final Logger LOGGER = Logger.getLogger(FractalityLab.class.getName());
    private static final int DEFAULT_NUMBER_OF_IMAGES = 10000;
    private static final int DEFAULT_SIZE = 256;
    private static final int DEFAULT_MAX_ITERATIONS = 18;
    private static final int DEFAULT_QUALITY = 80;
    private static Configuration config;

    public static void main(String[] args) {
        config = parseArguments(args);

        ImageGenerator[] imageGenerators = {
                new TricornGenerator(),
                new MandelbrotGenerator(),
                new JuliaGenerator(),
                new BurningShipGenerator(),
                new SierpinskiGasketGenerator(),
                new NewtonFractalGenerator(),
        };

        List<DataElement> allDataElements = generateAllDataElements(imageGenerators);

        LOGGER.info("All images generated.");

        String folder = "dataset";
        new CSVWriter().writeToCSV(folder + "/images.csv", allDataElements);

        LOGGER.info("All images generated and CSV file created.");
    }

    private static List<DataElement> generateAllDataElements(ImageGenerator[] imageGenerators) {
        return List.of(imageGenerators).parallelStream()
                .flatMap(imageGenerator -> imageGenerator.generateImage(
                        config.getWidth(),
                        config.getHeight(),
                        config.getMaxIterations(),
                        config.getNumberOfImages(),
                        config.getQuality()
                ).stream())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    private static Configuration parseArguments(String[] args) {
        int numberOfImages = DEFAULT_NUMBER_OF_IMAGES;
        int size = DEFAULT_SIZE;
        int quality = DEFAULT_QUALITY;
        int maxIterations = DEFAULT_MAX_ITERATIONS;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--number":
                        if (i + 1 < args.length) {
                            numberOfImages = Integer.parseInt(args[++i]);
                            LOGGER.info("Generating " + numberOfImages + " images.");
                        } else {
                            LOGGER.warning("No value provided for --number. Using default: " + DEFAULT_NUMBER_OF_IMAGES);
                        }
                        break;
                    case "--size":
                        if (i + 1 < args.length) {
                            size = Integer.parseInt(args[++i]);
                            LOGGER.info("Using size of " + size + "x" + size + " pixels.");
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
                            } else {
                                LOGGER.info("Using quality level: " + quality);
                            }
                        } else {
                            LOGGER.warning("No value provided for --quality. Using default: " + DEFAULT_QUALITY);
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

        return new Configuration(numberOfImages, size, quality, maxIterations);
    }

    private static class Configuration {
        private final int numberOfImages;
        private final int size;
        private final int quality;
        private final int maxIterations;

        public Configuration(int numberOfImages, int size, int quality, int maxIterations) {
            this.numberOfImages = numberOfImages;
            this.size = size;
            this.quality = quality;
            this.maxIterations = maxIterations;
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
    }
}