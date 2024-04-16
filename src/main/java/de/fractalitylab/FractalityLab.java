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
    private static final int DEFAULT_NUMBER_OF_IMAGES = 100;
    private static final int DEFAULT_SIZE = 256;
    private static final int MAX_ITERATIONS = 18;
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
                .flatMap(imageGenerator -> imageGenerator.generateImage(config.getWidth(), config.getHeight(), MAX_ITERATIONS, config.getNumberOfImages()).stream())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    private static Configuration parseArguments(String[] args) {
        int numberOfImages = DEFAULT_NUMBER_OF_IMAGES;
        int size = DEFAULT_SIZE;

        try {
            if (args.length >= 1) {
                numberOfImages = Integer.parseInt(args[0]);
                LOGGER.info("Generating " + numberOfImages + " images.");
            }
            if (args.length >= 2) {
                size = Integer.parseInt(args[1]);
                LOGGER.info("Using size of " + size + "x" + size + " pixels.");
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid number format for arguments. Using default values.");
        }

        return new Configuration(numberOfImages, size);
    }

    private static class Configuration {
        private final int numberOfImages;
        private final int size;

        public Configuration(int numberOfImages, int size) {
            this.numberOfImages = numberOfImages;
            this.size = size;
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
    }
}
