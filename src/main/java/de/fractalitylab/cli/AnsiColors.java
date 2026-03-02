package de.fractalitylab.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * ANSI escape code constants for terminal coloring.
 * Respects the {@code NO_COLOR} environment variable and non-interactive terminals.
 * Supports Linux, macOS, and Windows (Windows Terminal, ConEmu, cmd.exe on Windows 10+).
 */
public final class AnsiColors {

	private AnsiColors() {
	}

	public static final String RESET = "\u001B[0m";
	public static final String BOLD = "\u001B[1m";
	public static final String DIM = "\u001B[2m";

	public static final String RED = "\u001B[31m";
	public static final String GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[33m";
	public static final String BLUE = "\u001B[34m";
	public static final String MAGENTA = "\u001B[35m";
	public static final String CYAN = "\u001B[36m";
	public static final String WHITE = "\u001B[37m";

	private static final boolean IS_WINDOWS = System.getProperty("os.name", "")
			.toLowerCase().startsWith("windows");

	private static final boolean COLOR_ENABLED = detectColorSupport();
	private static final int TERMINAL_WIDTH = detectTerminalWidth();

	private static boolean detectColorSupport() {
		if (System.getenv("NO_COLOR") != null) {
			return false;
		}
		if (System.console() != null) {
			return true;
		}
		// System.console() returns null on many JVMs even in interactive terminals.
		// On Linux, check if stdout points to a terminal device.
		return isStdoutTerminal();
	}

	private static boolean isStdoutTerminal() {
		// Cross-platform: check terminal-related environment variables
		String term = System.getenv("TERM");
		if (term != null && !"dumb".equals(term)) {
			return true;
		}
		// Windows Terminal
		if (System.getenv("WT_SESSION") != null) {
			return true;
		}
		// ConEmu / Cmder
		if ("ON".equalsIgnoreCase(System.getenv("ConEmuANSI"))) {
			return true;
		}

		// Linux: check /proc/self/fd/1 symlink target
		try {
			Path link = Path.of("/proc/self/fd/1");
			if (Files.isSymbolicLink(link)) {
				String target = Files.readSymbolicLink(link).toString();
				return target.startsWith("/dev/pts/") || target.startsWith("/dev/tty");
			}
		} catch (Exception ignored) {
		}

		// macOS / Unix fallback: invoke `test -t 1` with inherited stdout
		if (!IS_WINDOWS) {
			try {
				var proc = new ProcessBuilder("test", "-t", "1")
						.redirectOutput(ProcessBuilder.Redirect.INHERIT)
						.start();
				return proc.waitFor() == 0;
			} catch (Exception ignored) {
			}
		}

		return false;
	}

	private static int detectTerminalWidth() {
		String cols = System.getenv("COLUMNS");
		if (cols != null) {
			try {
				int parsed = Integer.parseInt(cols.trim());
				if (parsed > 0) return parsed;
			} catch (NumberFormatException ignored) {
			}
		}
		if (!IS_WINDOWS) {
			// Linux / macOS: use tput cols
			try {
				var proc = new ProcessBuilder("tput", "cols")
						.redirectInput(ProcessBuilder.Redirect.from(new File("/dev/tty")))
						.redirectErrorStream(true)
						.start();
				String output = new String(proc.getInputStream().readAllBytes()).trim();
				if (proc.waitFor() == 0) {
					return Integer.parseInt(output);
				}
			} catch (Exception ignored) {
			}
		} else {
			// Windows: parse `mode con` output (language-independent)
			try {
				var proc = new ProcessBuilder("cmd.exe", "/c", "mode", "con")
						.redirectErrorStream(true)
						.start();
				String output = new String(proc.getInputStream().readAllBytes());
				if (proc.waitFor() == 0) {
					var matcher = Pattern.compile(":\\s*(\\d+)").matcher(output);
					// First match = lines/rows, second match = columns
					if (matcher.find() && matcher.find()) {
						return Integer.parseInt(matcher.group(1));
					}
				}
			} catch (Exception ignored) {
			}
		}
		return 120;
	}

	/**
	 * @return true if the terminal supports ANSI colors and cursor control
	 */
	public static boolean isColorEnabled() {
		return COLOR_ENABLED;
	}

	/**
	 * @return detected terminal width in columns, defaults to 120
	 */
	public static int terminalWidth() {
		return TERMINAL_WIDTH;
	}

	/**
	 * Wraps text with ANSI codes if color is enabled, otherwise returns plain text.
	 */
	public static String colorize(String text, String... codes) {
		if (!COLOR_ENABLED) {
			return text;
		}
		var sb = new StringBuilder();
		for (String code : codes) {
			sb.append(code);
		}
		sb.append(text).append(RESET);
		return sb.toString();
	}
}
