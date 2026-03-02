package de.fractalitylab;

import de.fractalitylab.config.Configuration;
import de.fractalitylab.data.DataElement;
import de.fractalitylab.generators.FractalGenerator;
import de.fractalitylab.generators.FractalMetadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FractalOrchestrator")
class FractalOrchestratorTest {

	@Test
	@DisplayName("generates correct number of elements per split")
	void generateProducesCorrectNumberOfElements(@TempDir Path tempDir) {
		var config = new Configuration(10, 32, 100, 10, 0.5, tempDir, List.of());
		var orchestrator = new FractalOrchestrator(config, List.of(stubGenerator("stub")));

		List<DataElement> trainData = orchestrator.generate(true);
		List<DataElement> testData = orchestrator.generate(false);

		assertThat(trainData).hasSize(5);
		assertThat(testData).hasSize(5);
		assertThat(trainData).allMatch(e -> e.label().equals("stub"));
	}

	@Test
	@DisplayName("generateAll writes train and test CSV files")
	void generateAllWritesCsvFiles(@TempDir Path tempDir) {
		var config = new Configuration(4, 32, 100, 10, 0.5, tempDir, List.of());
		var orchestrator = new FractalOrchestrator(config, List.of(stubGenerator("stub")));

		int total = orchestrator.generateAll();

		assertThat(total).isEqualTo(4); // 2 train + 2 test
		assertThat(tempDir.resolve("train").resolve("images.csv")).exists();
		assertThat(tempDir.resolve("test").resolve("images.csv")).exists();
	}

	@Test
	@DisplayName("supports multiple generators")
	void supportsMultipleGenerators(@TempDir Path tempDir) {
		var config = new Configuration(4, 32, 100, 10, 0.5, tempDir, List.of());
		var orchestrator = new FractalOrchestrator(config,
				List.of(stubGenerator("alpha"), stubGenerator("beta")));

		List<DataElement> trainData = orchestrator.generate(true);

		assertThat(trainData).hasSize(4); // 2 per generator
		assertThat(trainData.stream().map(DataElement::label).distinct().toList())
				.containsExactlyInAnyOrder("alpha", "beta");
	}

	@Test
	@DisplayName("progress listener receives callbacks")
	void progressListenerReceivesCallbacks(@TempDir Path tempDir) {
		var config = new Configuration(4, 32, 100, 10, 0.5, tempDir, List.of());
		AtomicInteger callbackCount = new AtomicInteger(0);

		ProgressListener listener = (label, isTrain, current, total) -> callbackCount.incrementAndGet();

		var orchestrator = new FractalOrchestrator(config, List.of(stubGenerator("stub")), listener);
		orchestrator.generate(true);

		assertThat(callbackCount.get()).isEqualTo(2); // 4 * 0.5 = 2 images
	}

	@Test
	@DisplayName("handles generator exception gracefully")
	void handlesGeneratorException(@TempDir Path tempDir) {
		FractalGenerator failingGenerator = new FractalGenerator() {
			@Override
			public BufferedImage generate(int width, int height, int maxIterations) {
				throw new RuntimeException("intentional test failure");
			}

			@Override
			public String label() {
				return "failing";
			}
		};

		var config = new Configuration(2, 32, 100, 10, 0.5, tempDir, List.of());
		var orchestrator = new FractalOrchestrator(config, List.of(failingGenerator));

		List<DataElement> results = orchestrator.generate(true);

		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("writes image files to correct directory")
	void writesImageFiles(@TempDir Path tempDir) throws Exception {
		var config = new Configuration(2, 32, 100, 10, 1.0, tempDir, List.of());
		var orchestrator = new FractalOrchestrator(config, List.of(stubGenerator("testgen")));

		orchestrator.generate(true);

		Path trainDir = tempDir.resolve("train").resolve("testgen");
		assertThat(trainDir).exists();
		long pngCount = Files.list(trainDir).filter(p -> p.toString().endsWith(".png")).count();
		assertThat(pngCount).isEqualTo(2);
	}

	@Test
	@DisplayName("generateAll cleans existing output directory before generation")
	void generateAllCleansExistingOutputDirectory(@TempDir Path tempDir) throws Exception {
		var config = new Configuration(2, 32, 100, 10, 1.0, tempDir, List.of());
		var orchestrator = new FractalOrchestrator(config, List.of(stubGenerator("stub")));

		// First run — produces images and CSV
		orchestrator.generateAll();

		Path trainDir = tempDir.resolve("train").resolve("stub");
		long firstRunCount = Files.list(trainDir).filter(p -> p.toString().endsWith(".png")).count();
		assertThat(firstRunCount).isEqualTo(2);

		// Second run — must NOT accumulate; old files must be gone
		int total = orchestrator.generateAll();

		long secondRunCount = Files.list(trainDir).filter(p -> p.toString().endsWith(".png")).count();
		assertThat(secondRunCount)
				.as("Old images must be removed before generating new ones")
				.isEqualTo(2);
		assertThat(total).isEqualTo(2); // all train, 0 test (ratio=1.0)
	}

	private FractalGenerator stubGenerator(String label) {
		return new FractalGenerator() {
			@Override
			public BufferedImage generate(int width, int height, int maxIterations) {
				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = img.createGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, width, height);
				g.dispose();
				return img;
			}

			@Override
			public String label() {
				return label;
			}

			@Override
			public String toString() {
				return label;
			}
		};
	}
}
