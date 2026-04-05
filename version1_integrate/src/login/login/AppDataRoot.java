package login;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves the shared {@code data} directory: sibling {@code ../data} to the code folder (e.g. {@code version1_integrate}),
 * or inline {@code ./data}, with legacy fallbacks for older layouts.
 */
public final class AppDataRoot {

    public static final String PROPERTY = "app.data.root";

    private AppDataRoot() {}

    public static Path asPath() {
        String override = System.getProperty(PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override.trim()).toAbsolutePath().normalize();
        }
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path sibling = cwd.getParent() != null ? cwd.getParent().resolve("data").normalize() : null;
        // Prefer sibling ../data only when it already holds app data (avoid empty folder masking legacy trees).
        if (sibling != null && isDataRoot(sibling)) {
            return sibling;
        }
        Path inline = cwd.resolve("data").normalize();
        if (isDataRoot(inline)) {
            return inline;
        }
        Path leg1 = cwd.resolve("integrated").resolve("data").normalize();
        if (Files.isDirectory(leg1)) {
            return leg1;
        }
        Path leg2 = cwd.getParent() != null
                ? cwd.getParent().resolve("integrated").resolve("data").normalize()
                : null;
        if (leg2 != null && Files.isDirectory(leg2)) {
            return leg2;
        }
        // New layout: sibling ../data (create on first save if missing).
        if (sibling != null) {
            return sibling;
        }
        return inline;
    }

    private static boolean isDataRoot(Path p) {
        return Files.isDirectory(p) && (Files.isDirectory(p.resolve("users")) || Files.isDirectory(p.resolve("jobs")));
    }

    public static File asFile() {
        return asPath().toFile();
    }
}
