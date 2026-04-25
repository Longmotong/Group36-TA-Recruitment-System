package com.mojobsystem;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves the {@code data/} directory when the process working directory is not the project root
 * (e.g. some IDE run configurations).
 */
public final class DataRoot {

    private DataRoot() {
    }

    public static Path resolve() {
        Path userDir = Path.of(System.getProperty("user.dir", "."));
        Path[] candidates = new Path[]{
                Path.of("data"),
                userDir.resolve("data"),
                Path.of("MO_System", "data"),
                userDir.resolve("MO_System").resolve("data"),
                Path.of("..", "data"),
                Path.of("..", "..", "data"),
                Path.of("..", "MO_System", "data")
        };
        for (Path base : candidates) {
            try {
                Path norm = base.toAbsolutePath().normalize();
                if (Files.isDirectory(norm.resolve("applications"))
                        || Files.isRegularFile(norm.resolve("jobs.json"))) {
                    return norm;
                }
            } catch (Exception ignored) {
            }
        }
        return userDir.resolve("data").toAbsolutePath().normalize();
    }
}
