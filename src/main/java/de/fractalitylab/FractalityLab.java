package de.fractalitylab;

import de.fractalitylab.cli.CliParser;
import de.fractalitylab.cli.CliParser.CliException;
import de.fractalitylab.cli.ProgressDisplay;
import de.fractalitylab.config.Configuration;
import de.fractalitylab.generators.FractalGenerator;
import de.fractalitylab.generators.FractalRegistry;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.fractalitylab.cli.AnsiColors.*;

public class FractalityLab {

	public static void main(String[] args) {
		try (var progress = new ProgressDisplay()) {
			Configuration config = CliParser.parse(args);
			if (config == null) {
				System.exit(0);
			}

			// Suppress INFO-level logs that would interfere with the progress display
			Logger.getLogger("de.fractalitylab").setLevel(Level.WARNING);

			CliParser.printBanner();
			List<FractalGenerator> generators = resolveGenerators(config);

			int totalImages = config.numberOfImages() * generators.size();
			System.out.println(colorize("Configuration:", BOLD, YELLOW));
			System.out.println(config.toDisplayString(generators.size(), totalImages));
			System.out.println();

			var orchestrator = new FractalOrchestrator(config, generators, progress);
			int total = orchestrator.generateAll();

			System.out.println(colorize("  ✓ ", BOLD, GREEN) + colorize("Complete!", BOLD, WHITE)
					+ "  " + colorize(String.valueOf(total), BOLD, CYAN) + " images generated");
			System.out.println(colorize("    └─ ", DIM) + colorize(config.outputDir().toString(), CYAN)
					+ colorize("  ·  ", DIM) + generators.size() + " fractal types");
			System.out.println();
		} catch (CliException e) {
			System.err.println(colorize("Error: ", BOLD, RED) + e.getMessage());
			System.err.println("Use --help for usage information.");
			System.exit(2);
		} catch (Exception e) {
			System.err.println(colorize("Error: ", BOLD, RED) + e.getMessage());
			System.exit(1);
		}
	}

	private static List<FractalGenerator> resolveGenerators(Configuration config) {
		if (config.selectedGenerators().isEmpty()) {
			return FractalRegistry.allGenerators();
		}
		return config.selectedGenerators().stream()
				.map(label -> FractalRegistry.findByLabel(label)
						.orElseThrow(() -> new CliException("Unknown generator: " + label)))
				.toList();
	}
}
