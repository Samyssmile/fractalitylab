package de.fractalitylab.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImageWriter")
class ImageWriterTest {

	@Test
	@DisplayName("writes train image to correct path")
	void writeImageCreatesTrainFile(@TempDir Path tempDir) {
		BufferedImage image = createTestImage();

		ImageWriter.writeImage("mandelbrot", "test-uuid", image, true, tempDir);

		Path expected = tempDir.resolve("train").resolve("mandelbrot").resolve("test-uuid.png");
		assertThat(expected).exists();
		assertThat(expected.toFile().length()).isGreaterThan(0);
	}

	@Test
	@DisplayName("writes test image to correct path")
	void writeImageCreatesTestFile(@TempDir Path tempDir) {
		BufferedImage image = createTestImage();

		ImageWriter.writeImage("julia", "test-uuid", image, false, tempDir);

		Path expected = tempDir.resolve("test").resolve("julia").resolve("test-uuid.png");
		assertThat(expected).exists();
		assertThat(expected.toFile().length()).isGreaterThan(0);
	}

	@Test
	@DisplayName("creates directory structure automatically")
	void writeImageCreatesDirectories(@TempDir Path tempDir) {
		BufferedImage image = createTestImage();
		Path outputDir = tempDir.resolve("nested").resolve("output");

		ImageWriter.writeImage("newton", "uuid", image, true, outputDir);

		assertThat(outputDir.resolve("train").resolve("newton").resolve("uuid.png")).exists();
	}

	private BufferedImage createTestImage() {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.RED);
		g.fillRect(0, 0, 32, 32);
		g.dispose();
		return image;
	}
}
