package de.fractalitylab.generators;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class NewtonFractalGeneratorTest {

	private final NewtonFractalGenerator generator = new NewtonFractalGenerator();

	@Test
	void labelIsNewton() {
		assertThat(generator.label()).isEqualTo("newton");
	}

	@Test
	void generateReturnsCorrectDimensions() {
		BufferedImage image = generator.generate(64, 64, 50);

		assertThat(image.getWidth()).isEqualTo(64);
		assertThat(image.getHeight()).isEqualTo(64);
	}

	@Test
	void generateProducesImage() {
		BufferedImage image = generator.generate(64, 64, 50);

		assertThat(image).isNotNull();
		assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
	}
}
