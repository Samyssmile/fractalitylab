package de.fractalitylab.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

    public void writeToCSV(String fileName, List<DataElement> dataElements) {
        try (FileWriter csvWriter = new FileWriter(fileName)) {
            csvWriter.append("image,label\n");

            for (DataElement element : dataElements) {
                csvWriter.append(element.filename()).append(",").append(element.label()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}