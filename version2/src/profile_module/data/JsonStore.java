package profile_module.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonStore {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path baseDir;
    private final Path profileFile;

    public JsonStore() {
        this.baseDir = Path.of(System.getProperty("user.home"), ".ta-system-swing");
        this.profileFile = baseDir.resolve("profile.json");
    }

    public Path getProfileFile() {
        return profileFile;
    }

    public ProfileData loadOrCreateDemo() {
        ProfileData loaded = load();
        if (loaded != null) {
            loaded.recomputeCompletion();
            return loaded;
        }
        ProfileData demo = ProfileData.demo();
        demo.recomputeCompletion();
        save(demo);
        return demo;
    }

    public ProfileData load() {
        if (!Files.exists(profileFile)) return null;
        try (Reader r = Files.newBufferedReader(profileFile, StandardCharsets.UTF_8)) {
            ProfileData d = gson.fromJson(r, ProfileData.class);
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public void save(ProfileData data) {
        try {
            Files.createDirectories(baseDir);
            data.recomputeCompletion();
            try (Writer w = Files.newBufferedWriter(profileFile, StandardCharsets.UTF_8)) {
                gson.toJson(data, w);
            }
        } catch (IOException ignored) {
        }
    }

    public ProfileData resetToDemo() {
        ProfileData demo = ProfileData.demo();
        demo.recomputeCompletion();
        save(demo);
        return demo;
    }
}

