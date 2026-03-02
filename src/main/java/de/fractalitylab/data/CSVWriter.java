package de.fractalitylab.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CSVWriter {

	private CSVWriter() {
	}

	/**
	 * Writes data elements as CSV to the specified folder with RFC 4180 escaping.
	 *
	 * @param folderPath target directory
	 * @param fileName   CSV file name
	 * @param elements   data elements to write
	 */
	public static void write(String folderPath, String fileName, List<DataElement> elements) {
		write(Path.of(folderPath), fileName, elements);
	}

	/**
	 * Writes data elements as CSV to the specified folder with RFC 4180 escaping.
	 *
	 * @param dir      target directory
	 * @param fileName CSV file name
	 * @param elements data elements to write
	 */
	public static void write(Path dir, String fileName, List<DataElement> elements) {
		try {
			Files.createDirectories(dir);
			try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve(fileName))) {
				writer.write("image,label");
				writer.newLine();
				for (DataElement element : elements) {
					writer.write(escapeCsv(element.filename()) + "," + escapeCsv(element.label()));
					writer.newLine();
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to write CSV: " + fileName, e);
		}
	}

	/**
	 * Escapes a CSV field per RFC 4180: fields containing commas, double quotes,
	 * or newlines are wrapped in double quotes, and embedded quotes are doubled.
	 */
	static String escapeCsv(String field) {
		if (field.indexOf(',') >= 0 || field.indexOf('"') >= 0
				|| field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
			return "\"" + field.replace("\"", "\"\"") + "\"";
		}
		return field;
	}
}
