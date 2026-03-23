package com.ebuko.moapp.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPaths {
  // Note: the folder name in your repository is literally "data（1）" (full-width parentheses).
  private static final String DATA_FOLDER_NAME = "data（1）";

  public static Path resolveDataRoot() {
    Path cursor = Paths.get("").toAbsolutePath();
    for (int i = 0; i < 8 && cursor != null; i++) {
      Path candidate = cursor.resolve(DATA_FOLDER_NAME).resolve("data");
      if (Files.isDirectory(candidate) && Files.exists(candidate.resolve("system").resolve("application_status_config.json"))) {
        return candidate;
      }
      cursor = cursor.getParent();
    }
    // Fallback candidates in case the user runs from the project root.
    Path fallback1 = Paths.get("data（1）", "data");
    if (Files.isDirectory(fallback1)) return fallback1;
    Path fallback2 = Paths.get("..", "data（1）", "data");
    if (Files.isDirectory(fallback2)) return fallback2;

    throw new IllegalStateException(
        "Cannot locate data root. Looked for folder: " + DATA_FOLDER_NAME + "/data relative to current working directory.");
  }
}

