package de.fractalitylab.cli;

import de.fractalitylab.ProgressListener;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static de.fractalitylab.cli.AnsiColors.*;

/**
 * Rich terminal progress display with live-updating progress bars,
 * per-generator tracking, throughput metrics, and ETA estimation.
 * Uses ANSI cursor control for flicker-free in-place rendering.
 */
public class ProgressDisplay implements ProgressListener, AutoCloseable {

	private static final String[] SPINNER = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
	private static final long MIN_RENDER_INTERVAL_NS = 60_000_000L; // ~16 FPS
	private static final int OVERALL_BAR_WIDTH = 50;
	private static final int GEN_BAR_WIDTH = 10;
	private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

	private static final String FILLED = "█";
	private static final String EMPTY = "░";
	private static final String[] PARTIAL = {"▉", "▊", "▋", "▌", "▍", "▎", "▏"};

	private static final String HIDE_CURSOR = "\033[?25l";
	private static final String SHOW_CURSOR = "\033[?25h";
	private static final String CLEAR_LINE = "\033[2K";

	private final PrintStream out;
	private final ConcurrentHashMap<String, AtomicInteger> perGenerator = new ConcurrentHashMap<>();

	private volatile List<String> labels;
	private volatile int perGeneratorTotal;
	private volatile boolean currentIsTrain;
	private volatile int currentTotal;
	private volatile long phaseStartNanos;
	private volatile long lastRenderNanos;

	// Only accessed inside synchronized doRender
	private int lastRenderedLines;
	private int spinnerIndex;

	public ProgressDisplay() {
		this(System.out);
	}

	ProgressDisplay(PrintStream out) {
		this.out = out;
	}

	@Override
	public void onPhaseStart(boolean isTrain, int total, List<String> generatorLabels, int imagesPerGenerator) {
		this.currentIsTrain = isTrain;
		this.currentTotal = total;
		this.labels = generatorLabels;
		this.perGeneratorTotal = imagesPerGenerator;
		this.perGenerator.clear();
		generatorLabels.forEach(l -> perGenerator.put(l, new AtomicInteger(0)));
		this.phaseStartNanos = System.nanoTime();
		this.lastRenderNanos = 0;
		this.lastRenderedLines = 0;

		if (isColorEnabled()) {
			out.print(HIDE_CURSOR);
		}
		doRender(0);
	}

	@Override
	public void onPhaseEnd(boolean isTrain) {
		forceRender(currentTotal);
		out.println();
		if (isColorEnabled()) {
			out.print(SHOW_CURSOR);
			out.flush();
		}
	}

	@Override
	public void onImageComplete(String label, boolean isTrain, int current, int total) {
		var counter = perGenerator.get(label);
		if (counter != null) {
			counter.incrementAndGet();
		}
		long now = System.nanoTime();
		if (current >= total || now - lastRenderNanos >= MIN_RENDER_INTERVAL_NS) {
			doRender(current);
		}
	}

	@Override
	public void close() {
		if (isColorEnabled()) {
			out.print(SHOW_CURSOR);
			out.flush();
		}
	}

	private void forceRender(int current) {
		lastRenderNanos = 0;
		doRender(current);
	}

	private synchronized void doRender(int current) {
		long now = System.nanoTime();
		if (current < currentTotal && current > 0 && now - lastRenderNanos < MIN_RENDER_INTERVAL_NS) {
			return;
		}
		lastRenderNanos = now;

		var frame = new StringBuilder(2048);

		if (lastRenderedLines > 0 && isColorEnabled()) {
			frame.append("\033[").append(lastRenderedLines).append("A\r");
		}

		int lines = 0;

		// ── Phase header + overall bar ──
		String phase = currentIsTrain ? "TRAINING" : "TESTING";
		double pct = currentTotal > 0 ? (double) current / currentTotal * 100 : 0;

		frame.append(clearLn())
				.append("  ").append(colorize("◆", BOLD, CYAN)).append(" ")
				.append(colorize(phase, BOLD, WHITE)).append("  ")
				.append(buildBar(pct, OVERALL_BAR_WIDTH)).append("  ")
				.append(colorize("%3.0f%%".formatted(pct), BOLD, pct >= 100 ? GREEN : WHITE)).append("  ")
				.append(colorize("%,d / %,d".formatted(current, currentTotal), DIM))
				.append('\n');
		lines++;

		frame.append(clearLn()).append('\n');
		lines++;

		// ── Per-generator grid ──
		if (labels != null && !labels.isEmpty()) {
			int maxLen = labels.stream().mapToInt(String::length).max().orElse(8);
			int cols = calculateColumns(labels.size(), maxLen);

			for (int i = 0; i < labels.size(); i++) {
				int col = i % cols;
				String label = labels.get(i);
				var counter = perGenerator.get(label);
				int done = counter != null ? counter.get() : 0;
				double genPct = perGeneratorTotal > 0 ? (double) done / perGeneratorTotal * 100 : 0;

				String pctColor;
				String labelColor;
				if (genPct >= 100) {
					pctColor = GREEN;
					labelColor = GREEN;
				} else if (genPct > 0) {
					pctColor = CYAN;
					labelColor = DIM;
				} else {
					pctColor = DIM;
					labelColor = DIM;
				}

				if (col == 0) {
					frame.append(clearLn()).append("  ");
				}

				frame.append(colorize(String.format("%-" + maxLen + "s", label), labelColor))
						.append("  ")
						.append(buildBar(genPct, GEN_BAR_WIDTH))
						.append(" ")
						.append(colorize("%3.0f%%".formatted(genPct), pctColor));

				if (col < cols - 1 && i < labels.size() - 1) {
					frame.append(colorize("  │  ", DIM));
				} else {
					frame.append('\n');
					lines++;
				}
			}
		}

		frame.append(clearLn()).append('\n');
		lines++;

		// ── Stats line ──
		double elapsedSec = (now - phaseStartNanos) / 1_000_000_000.0;
		double throughput = elapsedSec > 0.5 ? current / elapsedSec : 0;

		String etaStr;
		if (current > 0 && current < currentTotal && throughput > 0) {
			etaStr = "~" + formatDuration((currentTotal - current) / throughput) + " remaining";
		} else if (current >= currentTotal) {
			etaStr = colorize("complete", GREEN);
		} else {
			etaStr = "calculating...";
		}

		spinnerIndex = (spinnerIndex + 1) % SPINNER.length;

		frame.append(clearLn())
				.append("  ").append(colorize("⏱", DIM)).append("  ")
				.append(colorize(formatDuration(elapsedSec), WHITE))
				.append(colorize("  ·  ", DIM))
				.append(colorize("%.1f img/s".formatted(throughput), YELLOW))
				.append(colorize("  ·  ", DIM))
				.append(colorize("%d cores".formatted(CPU_CORES), MAGENTA))
				.append(colorize("  ·  ", DIM))
				.append(colorize(etaStr, DIM))
				.append("  ").append(colorize(SPINNER[spinnerIndex], CYAN))
				.append('\n');
		lines++;

		out.print(frame);
		out.flush();
		lastRenderedLines = lines;
	}

	private static String buildBar(double percent, int width) {
		double filled = width * Math.min(percent, 100) / 100.0;
		int full = (int) filled;
		double remainder = filled - full;

		var sb = new StringBuilder();
		String color = percent >= 100 ? GREEN : percent > 50 ? CYAN : YELLOW;

		if (isColorEnabled()) sb.append(color);
		sb.append(FILLED.repeat(full));

		if (full < width && remainder > 0.125) {
			int idx = (int) ((1 - remainder) * (PARTIAL.length - 1));
			idx = Math.clamp(idx, 0, PARTIAL.length - 1);
			sb.append(PARTIAL[idx]);
			full++;
		}

		if (isColorEnabled()) sb.append(RESET).append(DIM);
		sb.append(EMPTY.repeat(Math.max(0, width - full)));
		if (isColorEnabled()) sb.append(RESET);

		return sb.toString();
	}

	private static String clearLn() {
		return isColorEnabled() ? CLEAR_LINE : "";
	}

	/**
	 * Picks the highest column count that fits within the detected terminal width.
	 */
	private static int calculateColumns(int generatorCount, int maxLabelLen) {
		int termWidth = AnsiColors.terminalWidth();
		int cellWidth = maxLabelLen + 2 + GEN_BAR_WIDTH + 1 + 4; // label + gap + bar + space + pct
		int separatorWidth = 5; // "  │  "
		int indent = 2;

		for (int cols = 3; cols >= 1; cols--) {
			if (generatorCount < cols) continue;
			int totalWidth = indent + cols * cellWidth + (cols - 1) * separatorWidth;
			if (totalWidth <= termWidth) return cols;
		}
		return 1;
	}

	private static String formatDuration(double seconds) {
		if (seconds < 60) return "%.0fs".formatted(seconds);
		if (seconds < 3600) return "%dm %02ds".formatted((int) (seconds / 60), (int) (seconds % 60));
		return "%dh %02dm".formatted((int) (seconds / 3600), (int) ((seconds % 3600) / 60));
	}
}
