package de.fractalitylab.processing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QualityProcessor")
class QualityProcessorTest {

	@Test
	@DisplayName("quality=100 does not modify image")
	void quality100DoesNotModifyImage() {
		BufferedImage original = createSolidImage(64, 64, Color.RED);
		BufferedImage result = QualityProcessor.applyQualityAdjustments(original, 100);

		assertThat(result.getRGB(32, 32)).isEqualTo(original.getRGB(32, 32));
	}

	@Test
	@DisplayName("quality=0 applies heavy modifications")
	void quality0ModifiesImage() {
		BufferedImage original = createSolidImage(64, 64, Color.RED);
		BufferedImage result = QualityProcessor.applyQualityAdjustments(original, 0);

		boolean anyDifferent = false;
		for (int y = 0; y < 64 && !anyDifferent; y++) {
			for (int x = 0; x < 64 && !anyDifferent; x++) {
				if (result.getRGB(x, y) != original.getRGB(x, y)) {
					anyDifferent = true;
				}
			}
		}
		assertThat(anyDifferent).isTrue();
	}

	@Test
	@DisplayName("quality=50 modifies image moderately")
	void quality50ModifiesImageModerately() {
		BufferedImage original = createSolidImage(64, 64, Color.RED);
		BufferedImage result = QualityProcessor.applyQualityAdjustments(original, 50);

		boolean anyDifferent = false;
		for (int y = 0; y < 64 && !anyDifferent; y++) {
			for (int x = 0; x < 64 && !anyDifferent; x++) {
				if (result.getRGB(x, y) != original.getRGB(x, y)) {
					anyDifferent = true;
				}
			}
		}
		assertThat(anyDifferent).isTrue();
	}

	@Test
	@DisplayName("quality=25 modifies image")
	void quality25ModifiesImage() {
		BufferedImage original = createSolidImage(64, 64, Color.RED);
		BufferedImage result = QualityProcessor.applyQualityAdjustments(original, 25);

		boolean anyDifferent = false;
		for (int y = 0; y < 64 && !anyDifferent; y++) {
			for (int x = 0; x < 64 && !anyDifferent; x++) {
				if (result.getRGB(x, y) != original.getRGB(x, y)) {
					anyDifferent = true;
				}
			}
		}
		assertThat(anyDifferent).isTrue();
	}

	@Test
	@DisplayName("quality=75 modifies image")
	void quality75ModifiesImage() {
		BufferedImage original = createSolidImage(64, 64, Color.RED);
		BufferedImage result = QualityProcessor.applyQualityAdjustments(original, 75);

		boolean anyDifferent = false;
		for (int y = 0; y < 64 && !anyDifferent; y++) {
			for (int x = 0; x < 64 && !anyDifferent; x++) {
				if (result.getRGB(x, y) != original.getRGB(x, y)) {
					anyDifferent = true;
				}
			}
		}
		assertThat(anyDifferent).isTrue();
	}

	@Test
	@DisplayName("rotateImage preserves dimensions")
	void rotateImagePreservesDimensions() {
		BufferedImage original = createSolidImage(100, 100, Color.BLUE);
		BufferedImage rotated = QualityProcessor.rotateImage(original);

		assertThat(rotated.getWidth()).isEqualTo(100);
		assertThat(rotated.getHeight()).isEqualTo(100);
	}

	@Test
	@DisplayName("rotateImage preserves image type")
	void rotateImagePreservesType() {
		BufferedImage original = createSolidImage(64, 64, Color.GREEN);
		BufferedImage rotated = QualityProcessor.rotateImage(original);

		assertThat(rotated.getType()).isEqualTo(original.getType());
	}

	@Test
	@DisplayName("addNoise produces different output with noiseLevel > 0")
	void addNoiseModifiesImage() {
		BufferedImage original = createSolidImage(64, 64, Color.RED);
		int originalPixel = original.getRGB(32, 32);
		BufferedImage noisy = QualityProcessor.addNoise(original, 10.0f);

		boolean anyDifferent = false;
		for (int y = 0; y < 64 && !anyDifferent; y++) {
			for (int x = 0; x < 64 && !anyDifferent; x++) {
				if (noisy.getRGB(x, y) != originalPixel) {
					anyDifferent = true;
				}
			}
		}
		assertThat(anyDifferent).isTrue();
	}

	@Test
	@DisplayName("applyGaussianBlur with small radius doesn't crash")
	void gaussianBlurSmallRadius() {
		BufferedImage original = createSolidImage(32, 32, Color.RED);
		BufferedImage blurred = QualityProcessor.applyGaussianBlur(original, 1.0f);

		assertThat(blurred.getWidth()).isEqualTo(32);
		assertThat(blurred.getHeight()).isEqualTo(32);
	}

	private BufferedImage createSolidImage(int width, int height, Color color) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
		g.dispose();
		return image;
	}

}
