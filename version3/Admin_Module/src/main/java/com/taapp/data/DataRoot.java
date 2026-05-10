package com.taapp.data;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DataRoot {
    private DataRoot() {}

    public static Path resolve() {
        Path[] candidates = new Path[]{
                Path.of("D:/Users/32812/Desktop/data"),
                Path.of("D:/Users/32812/Desktop/data").toAbsolutePath(),
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
        return Path.of("D:/Users/32812/Desktop/data").toAbsolutePath().normalize();
    }
}
