package de.fractalitylab.cli;

import de.fractalitylab.config.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CliParser")
class CliParserTest {

	@Test
	@DisplayName("empty args returns defaults")
	void emptyArgsReturnsDefaults() {
		Configuration config = CliParser.parse(new String[]{});

		assertThat(config.numberOfImages()).isEqualTo(100);
		assertThat(config.size()).isEqualTo(256);
		assertThat(config.quality()).isEqualTo(80);
		assertThat(config.maxIterations()).isEqualTo(18);
		assertThat(config.ratio()).isEqualTo(0.8);
		assertThat(config.outputDir()).isEqualTo(Path.of("dataset"));
		assertThat(config.selectedGenerators()).isEmpty();
	}

	@Test
	@DisplayName("--numberPerClass flag is parsed")
	void numberPerClassFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--numberPerClass", "50"});

		assertThat(config.numberOfImages()).isEqualTo(50);
	}

	@Test
	@DisplayName("-n short flag is parsed")
	void numberShortFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"-n", "50"});

		assertThat(config.numberOfImages()).isEqualTo(50);
	}

	@Test
	@DisplayName("--resolution flag is parsed")
	void resolutionFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--resolution", "512"});

		assertThat(config.size()).isEqualTo(512);
	}

	@Test
	@DisplayName("-s short flag is parsed")
	void sizeShortFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"-s", "512"});

		assertThat(config.size()).isEqualTo(512);
	}

	@Test
	@DisplayName("--quality flag is parsed")
	void qualityFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--quality", "90"});

		assertThat(config.quality()).isEqualTo(90);
	}

	@Test
	@DisplayName("--maxIterations flag is parsed")
	void maxIterationsFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--maxIterations", "50"});

		assertThat(config.maxIterations()).isEqualTo(50);
	}

	@Test
	@DisplayName("--trainTestRatio flag is parsed")
	void trainTestRatioFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--trainTestRatio", "0.7"});

		assertThat(config.ratio()).isEqualTo(0.7);
	}

	@Test
	@DisplayName("--output flag sets output directory")
	void outputFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--output", "my-output"});

		assertThat(config.outputDir()).isEqualTo(Path.of("my-output"));
	}

	@Test
	@DisplayName("-o short flag sets output directory")
	void outputShortFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"-o", "my-output"});

		assertThat(config.outputDir()).isEqualTo(Path.of("my-output"));
	}

	@Test
	@DisplayName("--generators flag selects specific generators")
	void generatorsFlagIsParsed() {
		Configuration config = CliParser.parse(new String[]{"--generators", "mandelbrot,julia"});

		assertThat(config.selectedGenerators()).containsExactly("mandelbrot", "julia");
	}

	@Test
	@DisplayName("all flags combined")
	void allFlagsCombined() {
		Configuration config = CliParser.parse(new String[]{
				"--numberPerClass", "200",
				"--resolution", "128",
				"--quality", "50",
				"--maxIterations", "30",
				"--trainTestRatio", "0.9",
				"--output", "custom",
				"--generators", "mandelbrot"
		});

		assertThat(config.numberOfImages()).isEqualTo(200);
		assertThat(config.size()).isEqualTo(128);
		assertThat(config.quality()).isEqualTo(50);
		assertThat(config.maxIterations()).isEqualTo(30);
		assertThat(config.ratio()).isEqualTo(0.9);
		assertThat(config.outputDir()).isEqualTo(Path.of("custom"));
		assertThat(config.selectedGenerators()).containsExactly("mandelbrot");
	}

	@Test
	@DisplayName("invalid quality throws CliException")
	void invalidQualityThrows() {
		assertThatThrownBy(() -> CliParser.parse(new String[]{"--quality", "150"}))
				.isInstanceOf(CliParser.CliException.class)
				.hasMessageContaining("quality");
	}

	@Test
	@DisplayName("invalid ratio throws CliException")
	void invalidRatioThrows() {
		assertThatThrownBy(() -> CliParser.parse(new String[]{"--trainTestRatio", "1.5"}))
				.isInstanceOf(CliParser.CliException.class)
				.hasMessageContaining("trainTestRatio");
	}

	@Test
	@DisplayName("unknown argument throws CliException")
	void unknownArgumentThrows() {
		assertThatThrownBy(() -> CliParser.parse(new String[]{"--unknown", "value"}))
				.isInstanceOf(CliParser.CliException.class)
				.hasMessageContaining("Unknown argument");
	}

	@Test
	@DisplayName("non-numeric value for --numberPerClass throws CliException")
	void nonNumericNumberThrows() {
		assertThatThrownBy(() -> CliParser.parse(new String[]{"--numberPerClass", "abc"}))
				.isInstanceOf(CliParser.CliException.class)
				.hasMessageContaining("integer");
	}

	@Test
	@DisplayName("missing value for flag throws CliException")
	void missingValueThrows() {
		assertThatThrownBy(() -> CliParser.parse(new String[]{"--numberPerClass"}))
				.isInstanceOf(CliParser.CliException.class)
				.hasMessageContaining("requires a value");
	}

	@Test
	@DisplayName("unknown generator label throws CliException")
	void unknownGeneratorThrows() {
		assertThatThrownBy(() -> CliParser.parse(new String[]{"--generators", "nonexistent"}))
				.isInstanceOf(CliParser.CliException.class)
				.hasMessageContaining("Unknown generator");
	}

	@Test
	@DisplayName("--help returns null configuration")
	void helpReturnsNull() {
		Configuration config = CliParser.parse(new String[]{"--help"});

		assertThat(config).isNull();
	}

	@Test
	@DisplayName("--version returns null configuration")
	void versionReturnsNull() {
		Configuration config = CliParser.parse(new String[]{"--version"});

		assertThat(config).isNull();
	}
}
