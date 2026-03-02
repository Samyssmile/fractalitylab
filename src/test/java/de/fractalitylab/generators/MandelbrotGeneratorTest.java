package de.fractalitylab.generators;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class MandelbrotGeneratorTest {

	private final MandelbrotGenerator generator = new MandelbrotGenerator();

	@Test
	void labelIsMandelbrot() {
		assertThat(generator.label()).isEqualTo("mandelbrot");
	}

	@Test
	void generateReturnsCorrectDimensions() {
		BufferedImage image = generator.generate(64, 64, 20);

		assertThat(image.getWidth()).isEqualTo(64);
		assertThat(image.getHeight()).isEqualTo(64);
	}

	@Test
	void generateProducesNonBlackImage() {
		BufferedImage image = generator.generate(64, 64, 50);

		assertThat(hasNonBlackPixel(image)).isTrue();
	}

	private boolean hasNonBlackPixel(BufferedImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (image.getRGB(x, y) != Color.BLACK.getRGB()) {
					return true;
				}
			}
		}
		return false;
	}
}
