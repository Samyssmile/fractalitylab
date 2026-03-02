package de.fractalitylab.generators;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class SierpinskiGasketGeneratorTest {

	private final SierpinskiGasketGenerator generator = new SierpinskiGasketGenerator();

	@Test
	void labelIsSierpinski() {
		assertThat(generator.label()).isEqualTo("sierpinski");
	}

	@Test
	void generateReturnsCorrectDimensions() {
		BufferedImage image = generator.generate(64, 64, 20);

		assertThat(image.getWidth()).isEqualTo(64);
		assertThat(image.getHeight()).isEqualTo(64);
	}

	@Test
	void generateProducesImage() {
		BufferedImage image = generator.generate(64, 64, 20);

		assertThat(image).isNotNull();
		assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
	}
}
