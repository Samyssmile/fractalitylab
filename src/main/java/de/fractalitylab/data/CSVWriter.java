package de.fractalitylab.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

    public void writeToCSV(String folderPath, String fileName, List<DataElement> dataElements) {
        if (!new java.io.File(folderPath).exists()) {
            new java.io.File(folderPath).mkdirs();
        }
        String fileNameWithPath = folderPath + "/" + fileName;
        try (FileWriter csvWriter = new FileWriter(fileNameWithPath)) {
            csvWriter.append("image,label\n");

            for (DataElement element : dataElements) {
                csvWriter.append(element.filename()).append(",").append(element.label()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}