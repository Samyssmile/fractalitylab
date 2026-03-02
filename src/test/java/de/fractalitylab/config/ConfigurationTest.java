package de.fractalitylab.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Configuration record")
class ConfigurationTest {

	@Test
	@DisplayName("valid configuration with all fields")
	void validConfigurationCreatesRecord() {
		var config = new Configuration(100, 256, 80, 18, 0.8, Path.of("out"), List.of("mandelbrot"));

		assertThat(config.numberOfImages()).isEqualTo(100);
		assertThat(config.size()).isEqualTo(256);
		assertThat(config.quality()).isEqualTo(80);
		assertThat(config.maxIterations()).isEqualTo(18);
		assertThat(config.ratio()).isEqualTo(0.8);
		assertThat(config.outputDir()).isEqualTo(Path.of("out"));
		assertThat(config.selectedGenerators()).containsExactly("mandelbrot");
	}

	@Test
	@DisplayName("backwards-compatible constructor uses defaults for outputDir and generators")
	void backwardsCompatibleConstructor() {
		var config = new Configuration(100, 256, 80, 18, 0.8);

		assertThat(config.outputDir()).isEqualTo(Path.of("dataset"));
		assertThat(config.selectedGenerators()).isEmpty();
	}

	@Test
	@DisplayName("width() and height() return size")
	void widthAndHeightReturnSize() {
		var config = new Configuration(10, 512, 50, 10, 0.5);

		assertThat(config.width()).isEqualTo(512);
		assertThat(config.height()).isEqualTo(512);
	}

	@Test
	@DisplayName("boundary quality values (0 and 100) are valid")
	void boundaryQualityValuesAreValid() {
		assertThat(new Configuration(1, 1, 0, 1, 0.0).quality()).isZero();
		assertThat(new Configuration(1, 1, 100, 1, 1.0).quality()).isEqualTo(100);
	}

	@Test
	@DisplayName("negative numberOfImages throws")
	void negativeNumberOfImagesThrows() {
		assertThatThrownBy(() -> new Configuration(-1, 256, 80, 18, 0.8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("numberOfImages");
	}

	@Test
	@DisplayName("zero size throws")
	void zeroSizeThrows() {
		assertThatThrownBy(() -> new Configuration(10, 0, 80, 18, 0.8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("size");
	}

	@Test
	@DisplayName("quality out of range throws")
	void qualityOutOfRangeThrows() {
		assertThatThrownBy(() -> new Configuration(10, 256, -1, 18, 0.8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("quality");

		assertThatThrownBy(() -> new Configuration(10, 256, 101, 18, 0.8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("quality");
	}

	@Test
	@DisplayName("negative maxIterations throws")
	void negativeMaxIterationsThrows() {
		assertThatThrownBy(() -> new Configuration(10, 256, 80, -5, 0.8))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("maxIterations");
	}

	@Test
	@DisplayName("ratio out of range throws")
	void ratioOutOfRangeThrows() {
		assertThatThrownBy(() -> new Configuration(10, 256, 80, 18, -0.1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ratio");

		assertThatThrownBy(() -> new Configuration(10, 256, 80, 18, 1.1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ratio");
	}

	@Test
	@DisplayName("null outputDir throws")
	void nullOutputDirThrows() {
		assertThatThrownBy(() -> new Configuration(10, 256, 80, 18, 0.8, null, List.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("outputDir");
	}

	@Test
	@DisplayName("null selectedGenerators defaults to empty list")
	void nullSelectedGeneratorsDefaultsToEmpty() {
		var config = new Configuration(10, 256, 80, 18, 0.8, Path.of("dataset"), null);

		assertThat(config.selectedGenerators()).isEmpty();
	}

	@Test
	@DisplayName("selectedGenerators list is immutable copy")
	void selectedGeneratorsIsImmutableCopy() {
		var mutable = new java.util.ArrayList<>(List.of("mandelbrot", "julia"));
		var config = new Configuration(10, 256, 80, 18, 0.8, Path.of("dataset"), mutable);
		mutable.add("tricorn");

		assertThat(config.selectedGenerators()).hasSize(2);
	}

	@Test
	@DisplayName("toString contains all fields")
	void toStringContainsAllFields() {
		var config = new Configuration(50, 128, 90, 20, 0.7, Path.of("my-output"), List.of("julia"));
		String str = config.toString();

		assertThat(str).contains("50", "128", "90", "20", "0.7", "my-output", "julia");
	}
}
