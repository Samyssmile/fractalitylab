package de.fractalitylab.generators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FractalGenerator contract tests")
class FractalGeneratorContractTest {

	static Stream<FractalGenerator> allGenerators() {
		return FractalRegistry.allGenerators().stream();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("generates image with correct dimensions")
	void generatesCorrectDimensions(FractalGenerator generator) {
		BufferedImage image = generator.generate(64, 64, 18);

		assertThat(image.getWidth()).isEqualTo(64);
		assertThat(image.getHeight()).isEqualTo(64);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("returns non-null image")
	void returnsNonNullImage(FractalGenerator generator) {
		BufferedImage image = generator.generate(32, 32, 10);

		assertThat(image).isNotNull();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("uses TYPE_INT_RGB image type")
	void usesRgbImageType(FractalGenerator generator) {
		BufferedImage image = generator.generate(32, 32, 10);

		assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("label is non-empty lowercase string")
	void labelIsValid(FractalGenerator generator) {
		String label = generator.label();

		assertThat(label).isNotEmpty();
		assertThat(label).isEqualTo(label.toLowerCase());
		assertThat(label).matches("[a-z]+");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("metadata returns non-null with valid fields")
	void metadataIsValid(FractalGenerator generator) {
		FractalMetadata metadata = generator.metadata();

		assertThat(metadata).isNotNull();
		assertThat(metadata.displayName()).isNotEmpty();
		assertThat(metadata.minIterations()).isPositive();
		assertThat(metadata.maxIterations()).isGreaterThanOrEqualTo(metadata.minIterations());
		assertThat(metadata.defaultIterations()).isBetween(metadata.minIterations(), metadata.maxIterations());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("generates with size=1 without crashing")
	void handlesSizeOne(FractalGenerator generator) {
		BufferedImage image = generator.generate(1, 1, 10);

		assertThat(image.getWidth()).isEqualTo(1);
		assertThat(image.getHeight()).isEqualTo(1);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("generates with maxIterations=1 without crashing")
	void handlesMinIterations(FractalGenerator generator) {
		BufferedImage image = generator.generate(16, 16, 1);

		assertThat(image).isNotNull();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("allGenerators")
	@DisplayName("produces some non-black pixels")
	void producesNonBlackPixels(FractalGenerator generator) {
		BufferedImage image = generator.generate(64, 64, 18);
		int blackRgb = Color.BLACK.getRGB();

		boolean hasNonBlack = false;
		for (int y = 0; y < image.getHeight() && !hasNonBlack; y++) {
			for (int x = 0; x < image.getWidth() && !hasNonBlack; x++) {
				if (image.getRGB(x, y) != blackRgb) {
					hasNonBlack = true;
				}
			}
		}
		assertThat(hasNonBlack).as("Image from %s should contain non-black pixels", generator.label()).isTrue();
	}
}
