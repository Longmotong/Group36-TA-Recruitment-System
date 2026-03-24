package appreview.util;

/**
 * ANSI color helper for console output.
 */
public final class ConsolePrinter {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    private ConsolePrinter() {
    }

    /**
     * Wrap text with color.
     *
     * @param color ansi color code
     * @param text text
     * @return colored text
     */
    public static String color(String color, String text) {
        return color + text + RESET;
    }
}
