package de.fractalitylab.generators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FractalRegistry")
class FractalRegistryTest {

	@Test
	@DisplayName("allGenerators() returns all generators")
	void allGeneratorsReturnsAllGenerators() {
		List<FractalGenerator> generators = FractalRegistry.allGenerators();

		assertThat(generators).hasSize(30);
	}

	@Test
	@DisplayName("all generators have unique labels")
	void allGeneratorsHaveUniqueLabels() {
		List<String> labels = FractalRegistry.allGenerators().stream()
				.map(FractalGenerator::label)
				.toList();

		assertThat(labels).doesNotHaveDuplicates();
	}

	@Test
	@DisplayName("all expected labels are present")
	void allExpectedLabelsPresent() {
		List<String> labels = FractalRegistry.allGenerators().stream()
				.map(FractalGenerator::label)
				.toList();

		assertThat(labels).containsExactlyInAnyOrder(
				"mandelbrot", "julia", "burningship", "tricorn", "sierpinski", "newton",
				"multibrot", "phoenix", "magnettypeone", "magnettypetwo",
				"celtic", "buffalo", "perpendicular", "collatz", "lyapunov", "buddhabrot",
				"sierpinskicarpet", "barnsleyfern", "pythagorastree", "vicsek",
				"tsquare", "kochsnowflake", "apollonian",
				"dragoncurve", "levycurve", "hilbertcurve", "gospercurve",
				"clifford", "dejong", "henon"
		);
	}

	@Test
	@DisplayName("labels() returns same as allGenerators().label()")
	void labelsMatchesGenerators() {
		assertThat(FractalRegistry.labels()).containsExactlyElementsOf(
				FractalRegistry.allGenerators().stream().map(FractalGenerator::label).toList()
		);
	}

	@Test
	@DisplayName("findByLabel() returns matching generator")
	void findByLabelReturnsMatch() {
		assertThat(FractalRegistry.findByLabel("mandelbrot"))
				.isPresent()
				.get()
				.extracting(FractalGenerator::label)
				.isEqualTo("mandelbrot");
	}

	@Test
	@DisplayName("findByLabel() is case-insensitive")
	void findByLabelCaseInsensitive() {
		assertThat(FractalRegistry.findByLabel("MANDELBROT")).isPresent();
		assertThat(FractalRegistry.findByLabel("Julia")).isPresent();
	}

	@Test
	@DisplayName("findByLabel() returns empty for unknown label")
	void findByLabelReturnsEmptyForUnknown() {
		assertThat(FractalRegistry.findByLabel("nonexistent")).isEmpty();
	}
}
