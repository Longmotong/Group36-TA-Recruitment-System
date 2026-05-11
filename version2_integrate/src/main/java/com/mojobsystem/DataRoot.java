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
        Path[] candidates = new Path[]{
                Path.of("data"),
                Path.of(System.getProperty("user.dir", ".")).resolve("data"),
                Path.of("..", "data"),
                Path.of("..", "..", "data")
        };
        for (Path base : candidates) {
            try {
                Path norm = base.toAbsolutePath().normalize();
                if (Files.isDirectory(norm.resolve("applications"))) {
                    return norm;
                }
            } catch (Exception ignored) {
            }
        }
        return Path.of("data");
    }
}
