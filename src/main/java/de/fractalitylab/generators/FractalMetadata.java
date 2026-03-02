package de.fractalitylab.generators;

/**
 * Metadata describing a fractal generator's characteristics and iteration bounds.
 *
 * @param displayName       human-readable name
 * @param description       short description of the fractal type
 * @param defaultIterations recommended default iteration count
 * @param minIterations     minimum useful iteration count
 * @param maxIterations     maximum recommended iteration count
 */
public record FractalMetadata(
		String displayName,
		String description,
		int defaultIterations,
		int minIterations,
		int maxIterations
) {
}
