package de.fractalitylab.generators;

import de.fractalitylab.data.DataElement;

import java.util.List;

public interface ImageGenerator {

    List<DataElement> generateImage(int width, int height, int maxIterations, int numberOfImages);
}
