package de.fractalitylab.generators;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class MagnetTypeOneGeneratorTest {

	private final MagnetTypeOneGenerator generator = new MagnetTypeOneGenerator();

	@Test
	void labelIsMagnettypeone() {
		assertThat(generator.label()).isEqualTo("magnettypeone");
	}

	@Test
	void generateReturnsCorrectDimensions() {
		BufferedImage image = generator.generate(128, 128, 30);

		assertThat(image.getWidth()).isEqualTo(128);
		assertThat(image.getHeight()).isEqualTo(128);
	}

	@Test
	void generateProducesImage() {
		BufferedImage image = generator.generate(64, 64, 20);

		assertThat(image).isNotNull();
		assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
	}
}
