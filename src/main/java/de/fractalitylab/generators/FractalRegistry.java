package de.fractalitylab.generators;

import java.util.List;
import java.util.Optional;

/**
 * Central registry of all available fractal generators.
 * To add a new fractal type, create a class implementing {@link FractalGenerator}
 * and add one line in {@link #allGenerators()}.
 */
public final class FractalRegistry {

	private static final List<FractalGenerator> GENERATORS = List.of(
			// Original 6
			new MandelbrotGenerator(),
			new JuliaGenerator(),
			new BurningShipGenerator(),
			new TricornGenerator(),
			new SierpinskiGasketGenerator(),
			new NewtonFractalGenerator(),
			// Escape-time
			new MultibrotGenerator(),
			new PhoenixGenerator(),
			new MagnetTypeOneGenerator(),
			new MagnetTypeTwoGenerator(),
			new CelticGenerator(),
			new BuffaloGenerator(),
			new PerpendicularGenerator(),
			new CollatzGenerator(),
			new LyapunovGenerator(),
			new BuddhabrotGenerator(),
			// IFS
			new SierpinskiCarpetGenerator(),
			new BarnsleyFernGenerator(),
			new PythagorasTreeGenerator(),
			new VicsekGenerator(),
			new TSquareGenerator(),
			new KochSnowflakeGenerator(),
			new ApollonianGasketGenerator(),
			// L-System curves
			new DragonCurveGenerator(),
			new LevyCurveGenerator(),
			new HilbertCurveGenerator(),
			new GosperCurveGenerator(),
			// Strange attractors
			new CliffordAttractorGenerator(),
			new DeJongAttractorGenerator(),
			new HenonMapGenerator()
	);

	private FractalRegistry() {
	}

	/**
	 * @return all registered fractal generators
	 */
	public static List<FractalGenerator> allGenerators() {
		return GENERATORS;
	}

	/**
	 * Finds a generator by its label (case-insensitive).
	 *
	 * @param label the generator label to search for
	 * @return the matching generator, or empty if not found
	 */
	public static Optional<FractalGenerator> findByLabel(String label) {
		return GENERATORS.stream()
				.filter(g -> g.label().equalsIgnoreCase(label))
				.findFirst();
	}

	/**
	 * @return all registered generator labels
	 */
	public static List<String> labels() {
		return GENERATORS.stream()
				.map(FractalGenerator::label)
				.toList();
	}
}
