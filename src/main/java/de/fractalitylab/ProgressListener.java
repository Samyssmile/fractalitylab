package de.fractalitylab;

import java.util.List;

/**
 * Callback interface for tracking fractal image generation progress.
 */
@FunctionalInterface
public interface ProgressListener {

	void onImageComplete(String label, boolean isTrain, int current, int total);

	/**
	 * Called before a generation phase (train or test) begins.
	 */
	default void onPhaseStart(boolean isTrain, int total, List<String> generatorLabels, int imagesPerGenerator) {}

	/**
	 * Called after a generation phase (train or test) completes.
	 */
	default void onPhaseEnd(boolean isTrain) {}

	static ProgressListener noOp() {
		return (label, isTrain, current, total) -> {};
	}
}
