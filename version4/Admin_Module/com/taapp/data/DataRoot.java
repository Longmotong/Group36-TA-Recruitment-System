package Admin_Module.com.taapp.data;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DataRoot {
    private DataRoot() {}

    public static Path resolve() {
        Path[] candidates = new Path[]{
                Path.of("data"),
                Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize().resolve("data"),
                Path.of("MO_System", "data"),
                Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize().resolve("MO_System").resolve("data"),
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
        return Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize().resolve("data");
    }
}
