package de.fractalitylab.cli;

import de.fractalitylab.config.Configuration;
import de.fractalitylab.generators.FractalRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import static de.fractalitylab.cli.AnsiColors.*;

/**
 * Parses command-line arguments into a {@link Configuration}.
 * Supports long and short flags, colored help output, and clear error messages.
 */
public final class CliParser {

	static final int DEFAULT_NUMBER_OF_IMAGES = 100;
	static final int DEFAULT_SIZE = 256;
	static final int DEFAULT_QUALITY = 80;
	static final int DEFAULT_MAX_ITERATIONS = 18;
	static final double DEFAULT_RATIO = 0.8;
	static final String DEFAULT_OUTPUT_DIR = "dataset";
	static final String VERSION = loadVersion();

	private CliParser() {
	}

	/**
	 * Parses CLI arguments into a Configuration.
	 *
	 * @param args command-line arguments
	 * @return parsed configuration with defaults for missing values
	 * @throws CliException if arguments are invalid
	 */
	public static Configuration parse(String[] args) {
		int numberOfImages = DEFAULT_NUMBER_OF_IMAGES;
		int size = DEFAULT_SIZE;
		int quality = DEFAULT_QUALITY;
		int maxIterations = DEFAULT_MAX_ITERATIONS;
		double ratio = DEFAULT_RATIO;
		String outputDir = DEFAULT_OUTPUT_DIR;
		List<String> selectedGenerators = List.of();

		// Short flag → long flag mapping
		Map<String, String> shortFlags = Map.of(
				"-n", "--numberPerClass",
				"-s", "--resolution",
				"-q", "--quality",
				"-i", "--maxIterations",
				"-r", "--trainTestRatio",
				"-o", "--output",
				"-g", "--generators",
				"-h", "--help"
		);

		for (int i = 0; i < args.length; i++) {
			String flag = shortFlags.getOrDefault(args[i], args[i]);

			switch (flag) {
				case "--help" -> {
					printHelp();
					return null;
				}
				case "--version" -> {
					System.out.println("FractalityLab " + VERSION);
					return null;
				}
				case "--numberPerClass" -> numberOfImages = requirePositiveInt(args, i++, "--numberPerClass");
				case "--resolution" -> size = requirePositiveInt(args, i++, "--resolution");
				case "--quality" -> {
					quality = requireInt(args, i++, "--quality");
					if (quality < 0 || quality > 100) {
						throw new CliException("--quality must be between 0 and 100, got: " + quality);
					}
				}
				case "--maxIterations" -> maxIterations = requirePositiveInt(args, i++, "--maxIterations");
				case "--trainTestRatio" -> {
					ratio = requireDouble(args, i++, "--trainTestRatio");
					if (ratio < 0.0 || ratio > 1.0) {
						throw new CliException("--trainTestRatio must be between 0.0 and 1.0, got: " + ratio);
					}
				}
				case "--output" -> outputDir = requireString(args, i++, "--output");
				case "--generators" -> {
					String value = requireString(args, i++, "--generators");
					selectedGenerators = Arrays.stream(value.split(","))
							.map(String::trim)
							.filter(s -> !s.isEmpty())
							.toList();
					List<String> validLabels = FractalRegistry.labels();
					for (String label : selectedGenerators) {
						if (!validLabels.contains(label)) {
							throw new CliException("Unknown generator: '" + label
									+ "'. Available: " + String.join(", ", validLabels));
						}
					}
				}
				default -> throw new CliException("Unknown argument: " + args[i]);
			}
		}

		return new Configuration(numberOfImages, size, quality, maxIterations, ratio,
				Path.of(outputDir), selectedGenerators);
	}

	/**
	 * Prints the colored banner header.
	 */
	public static void printBanner() {
		String title = "FractalityLab " + VERSION;
		int boxWidth = 39;
		int padding = boxWidth - title.length();
		int leftPad = padding / 2;
		int rightPad = padding - leftPad;
		String titleLine = " ".repeat(leftPad) + title + " ".repeat(rightPad);
		String banner = """

				  ╔═══════════════════════════════════════╗
				  ║%s║
				  ║   Fractal Dataset Generator for ML    ║
				  ╚═══════════════════════════════════════╝
				""".formatted(titleLine);
		System.out.println(colorize(banner, BOLD, CYAN));
	}

	private static void printHelp() {
		printBanner();

		System.out.println(colorize("USAGE:", BOLD, YELLOW));
		System.out.println("  java -jar FractalityLab.jar [options]\n");

		System.out.println(colorize("OPTIONS:", BOLD, YELLOW));
		printOption("-n, --numberPerClass <n>", "Number of images per fractal class", String.valueOf(DEFAULT_NUMBER_OF_IMAGES));
		printOption("-s, --resolution <n>", "Image resolution in pixels (width=height)", String.valueOf(DEFAULT_SIZE));
		printOption("-q, --quality <0-100>", "Image quality (0=degraded, 100=pristine)", String.valueOf(DEFAULT_QUALITY));
		printOption("-i, --maxIterations <n>", "Base iteration count for fractals", String.valueOf(DEFAULT_MAX_ITERATIONS));
		printOption("-r, --trainTestRatio <r>", "Train/test split ratio 0.0-1.0", String.valueOf(DEFAULT_RATIO));
		printOption("-o, --output <dir>", "Output directory", DEFAULT_OUTPUT_DIR);
		printOption("-g, --generators <list>", "Comma-separated generator labels", "all");
		printOption("-h, --help", "Show this help message", "");
		printOption("    --version", "Show version", "");

		System.out.println(colorize("\nAVAILABLE GENERATORS:", BOLD, YELLOW));
		FractalRegistry.labels().forEach(label ->
				System.out.println("  " + colorize("•", GREEN) + " " + label));

		System.out.println(colorize("\nEXAMPLES:", BOLD, YELLOW));
		System.out.println("  java -jar FractalityLab.jar --numberPerClass 100 --resolution 256 --quality 80");
		System.out.println("  java -jar FractalityLab.jar --generators mandelbrot,julia --output my-dataset");
		System.out.println();
	}

	private static void printOption(String flags, String description, String defaultValue) {
		String flagStr = colorize("  " + String.format("%-30s", flags), GREEN);
		String desc = description;
		if (!defaultValue.isEmpty()) {
			desc += colorize(" [" + defaultValue + "]", DIM);
		}
		System.out.println(flagStr + desc);
	}

	private static int requirePositiveInt(String[] args, int index, String flag) {
		int value = requireInt(args, index, flag);
		if (value <= 0) {
			throw new CliException(flag + " must be a positive integer, got: " + value);
		}
		return value;
	}

	private static int requireInt(String[] args, int index, String flag) {
		String value = requireString(args, index, flag);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new CliException(flag + " requires an integer value, got: '" + value + "'");
		}
	}

	private static double requireDouble(String[] args, int index, String flag) {
		String value = requireString(args, index, flag);
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new CliException(flag + " requires a numeric value, got: '" + value + "'");
		}
	}

	private static String requireString(String[] args, int index, String flag) {
		if (index + 1 >= args.length) {
			throw new CliException(flag + " requires a value");
		}
		return args[index + 1];
	}

	private static String loadVersion() {
		var props = new Properties();
		try (var in = CliParser.class.getResourceAsStream("/version.properties")) {
			if (in != null) {
				props.load(in);
				return props.getProperty("version", "unknown");
			}
		} catch (IOException ignored) {
			// fall through
		}
		return "unknown";
	}

	/**
	 * Exception for CLI argument parsing errors.
	 */
	public static class CliException extends RuntimeException {
		public CliException(String message) {
			super(message);
		}
	}
}
