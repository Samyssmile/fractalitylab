package de.fractalitylab.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CSVWriter")
class CSVWriterTest {

	@Test
	@DisplayName("writes CSV with correct content")
	void writeCreatesCSVWithCorrectContent(@TempDir Path tempDir) throws Exception {
		List<DataElement> elements = List.of(
				new DataElement("abc-123", "mandelbrot"),
				new DataElement("def-456", "julia")
		);

		CSVWriter.write(tempDir.toString(), "test.csv", elements);

		Path csvFile = tempDir.resolve("test.csv");
		assertThat(csvFile).exists();

		List<String> lines = Files.readAllLines(csvFile);
		assertThat(lines).hasSize(3);
		assertThat(lines.getFirst()).isEqualTo("image,label");
		assertThat(lines.get(1)).isEqualTo("abc-123,mandelbrot");
		assertThat(lines.get(2)).isEqualTo("def-456,julia");
	}

	@Test
	@DisplayName("writes CSV using Path overload")
	void writeWithPathOverload(@TempDir Path tempDir) throws Exception {
		List<DataElement> elements = List.of(new DataElement("uuid-1", "newton"));

		CSVWriter.write(tempDir, "test.csv", elements);

		List<String> lines = Files.readAllLines(tempDir.resolve("test.csv"));
		assertThat(lines).hasSize(2);
		assertThat(lines.get(1)).isEqualTo("uuid-1,newton");
	}

	@Test
	@DisplayName("creates nested directories if needed")
	void writeCreatesDirectoriesIfNeeded(@TempDir Path tempDir) {
		Path nested = tempDir.resolve("a").resolve("b").resolve("c");

		CSVWriter.write(nested.toString(), "data.csv", List.of());

		assertThat(nested.resolve("data.csv")).exists();
	}

	@Test
	@DisplayName("empty list creates header-only file")
	void writeEmptyListCreatesHeaderOnly(@TempDir Path tempDir) throws Exception {
		CSVWriter.write(tempDir.toString(), "empty.csv", List.of());

		List<String> lines = Files.readAllLines(tempDir.resolve("empty.csv"));
		assertThat(lines).hasSize(1);
		assertThat(lines.getFirst()).isEqualTo("image,label");
	}

	@Test
	@DisplayName("escapes fields with commas per RFC 4180")
	void escapesFieldsWithCommas(@TempDir Path tempDir) throws Exception {
		List<DataElement> elements = List.of(new DataElement("file,with,commas", "label"));

		CSVWriter.write(tempDir.toString(), "test.csv", elements);

		List<String> lines = Files.readAllLines(tempDir.resolve("test.csv"));
		assertThat(lines.get(1)).isEqualTo("\"file,with,commas\",label");
	}

	@Test
	@DisplayName("escapes fields with double quotes per RFC 4180")
	void escapesFieldsWithQuotes(@TempDir Path tempDir) throws Exception {
		List<DataElement> elements = List.of(new DataElement("file\"name", "label"));

		CSVWriter.write(tempDir.toString(), "test.csv", elements);

		List<String> lines = Files.readAllLines(tempDir.resolve("test.csv"));
		assertThat(lines.get(1)).isEqualTo("\"file\"\"name\",label");
	}

	@Test
	@DisplayName("escapes fields with newlines per RFC 4180")
	void escapesFieldsWithNewlines(@TempDir Path tempDir) throws Exception {
		List<DataElement> elements = List.of(new DataElement("file\nname", "label"));

		CSVWriter.write(tempDir, "test.csv", elements);

		String content = Files.readString(tempDir.resolve("test.csv"));
		assertThat(content).contains("\"file\nname\"");
	}

	@Test
	@DisplayName("escapeCsv() leaves plain strings unchanged")
	void escapeCsvPlainString() {
		assertThat(CSVWriter.escapeCsv("simple")).isEqualTo("simple");
	}

	@Test
	@DisplayName("escapeCsv() wraps field with comma in quotes")
	void escapeCsvComma() {
		assertThat(CSVWriter.escapeCsv("a,b")).isEqualTo("\"a,b\"");
	}

	@Test
	@DisplayName("escapeCsv() doubles embedded quotes")
	void escapeCsvQuotes() {
		assertThat(CSVWriter.escapeCsv("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
	}
}
