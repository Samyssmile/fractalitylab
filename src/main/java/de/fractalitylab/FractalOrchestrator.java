package de.fractalitylab;

import de.fractalitylab.config.Configuration;
import de.fractalitylab.data.CSVWriter;
import de.fractalitylab.data.DataElement;
import de.fractalitylab.data.ImageWriter;
import de.fractalitylab.generators.FractalGenerator;
import de.fractalitylab.processing.QualityProcessor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates fractal image generation across all registered generators.
 * Uses Virtual Threads for lightweight task management combined with a
 * {@link Semaphore} sized to {@code availableProcessors()} to bound
 * CPU-bound concurrency — ensuring all cores are fully utilized without
 * oversubscription or ForkJoinPool contention.
 */
public class FractalOrchestrator {

	private static final Logger LOGGER = Logger.getLogger(FractalOrchestrator.class.getName());
	private static final int CPU_PARALLELISM = Runtime.getRuntime().availableProcessors();

	private final Configuration config;
	private final List<FractalGenerator> generators;
	private final ProgressListener progressListener;
	private final Semaphore cpuPermits = new Semaphore(CPU_PARALLELISM);

	public FractalOrchestrator(Configuration config, List<FractalGenerator> generators) {
		this(config, generators, ProgressListener.noOp());
	}

	public FractalOrchestrator(Configuration config, List<FractalGenerator> generators,
	                           ProgressListener progressListener) {
		this.config = config;
		this.generators = generators;
		this.progressListener = progressListener;
	}

	/**
	 * Generates all images (train + test) concurrently, writes them to disk,
	 * and creates CSV metadata files.
	 *
	 * @return total number of generated images
	 */
	public int generateAll() {
		cleanOutputDirectory(config.outputDir());

		List<DataElement> trainData = generate(true);
		List<DataElement> testData = generate(false);

		Path outputDir = config.outputDir();
		CSVWriter.write(outputDir.resolve("train"), "images.csv", trainData);
		CSVWriter.write(outputDir.resolve("test"), "images.csv", testData);

		return trainData.size() + testData.size();
	}

	/**
	 * Generates images for all registered generators.
	 * Each image task acquires a CPU permit before rendering, ensuring at most
	 * {@code availableProcessors()} images render concurrently.
	 *
	 * @param isTrain true for training set, false for test set
	 * @return list of generated data elements with filenames and labels
	 */
	public List<DataElement> generate(boolean isTrain) {
		int countPerGenerator = calculateNumberOfImages(isTrain);
		int totalImages = countPerGenerator * generators.size();
		List<String> labels = generators.stream().map(FractalGenerator::label).toList();
		progressListener.onPhaseStart(isTrain, totalImages, labels, countPerGenerator);

		ConcurrentLinkedQueue<DataElement> results = new ConcurrentLinkedQueue<>();
		AtomicInteger completed = new AtomicInteger(0);
		Path outputDir = config.outputDir();

		preCreateDirectories(isTrain, outputDir);

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<?>> futures = new ArrayList<>();

			for (FractalGenerator generator : generators) {
				for (int i = 0; i < countPerGenerator; i++) {
					futures.add(executor.submit(() -> {
						try {
							cpuPermits.acquire();
							try {
								BufferedImage image = generator.generate(
										config.width(), config.height(), config.maxIterations());
								image = QualityProcessor.applyQualityAdjustments(image, config.quality());
								image = QualityProcessor.rotateImage(image);

								String uuid = UUID.randomUUID().toString();
								ImageWriter.writeImage(generator.label(), uuid, image, isTrain, outputDir);
								results.add(new DataElement(uuid, generator.label()));
							} finally {
								cpuPermits.release();
							}

							int done = completed.incrementAndGet();
							progressListener.onImageComplete(generator.label(), isTrain, done, totalImages);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							LOGGER.log(Level.WARNING, "Image generation interrupted for " + generator.label(), e);
						} catch (Exception e) {
							LOGGER.log(Level.WARNING,
									"Failed to generate " + generator.label() + " image", e);
						}
					}));
				}
			}

			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Image generation task failed", e);
				}
			}
		}

		progressListener.onPhaseEnd(isTrain);
		List<DataElement> resultList = new ArrayList<>(results);
		LOGGER.info(resultList.size() + " images generated (" + (isTrain ? "train" : "test") + ").");
		return resultList;
	}

	/**
	 * Pre-creates all class directories for a given split, eliminating redundant
	 * {@code Files.createDirectories()} syscalls during concurrent image writing.
	 */
	private void preCreateDirectories(boolean isTrain, Path outputDir) {
		String split = isTrain ? "train" : "test";
		for (FractalGenerator generator : generators) {
			Path classDir = outputDir.resolve(split).resolve(generator.label());
			try {
				Files.createDirectories(classDir);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create directory: " + classDir, e);
			}
		}
	}

	/**
	 * Removes all contents of the output directory if it exists and is non-empty.
	 * The directory itself is preserved; only its contents are deleted.
	 */
	private void cleanOutputDirectory(Path outputDir) {
		if (!Files.exists(outputDir)) {
			return;
		}

		try (var entries = Files.list(outputDir)) {
			if (entries.findAny().isEmpty()) {
				return;
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read output directory: " + outputDir, e);
		}

		LOGGER.info("Output directory '%s' is not empty — cleaning before generation.".formatted(outputDir));

		try {
			Files.walkFileTree(outputDir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (!dir.equals(outputDir)) {
						Files.delete(dir);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException("Failed to clean output directory: " + outputDir, e);
		}
	}

	private int calculateNumberOfImages(boolean isTrain) {
		int trainCount = (int) Math.round(config.numberOfImages() * config.ratio());
		return isTrain ? trainCount : config.numberOfImages() - trainCount;
	}
}
