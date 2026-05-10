package com.taapp.data;

import com.taapp.util.SimpleJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TaUserRepository {
    public Map<String, Object> findByKey(String key) {
        Path dir = DataRoot.resolve().resolve("users").resolve("ta");
        if (!Files.isDirectory(dir) || key == null || key.isBlank()) {
            return Map.of();
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path file : stream) {
                try {
                    Map<String, Object> root = SimpleJson.parseObject(Files.readString(file, StandardCharsets.UTF_8));
                    if (matches(root, key)) {
                        return root;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return Map.of();
    }

    public List<Map<String, Object>> findApplicationsForKey(String key) {
        Path dir = DataRoot.resolve().resolve("applications");
        if (!Files.isDirectory(dir) || key == null || key.isBlank()) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path file : stream) {
                try {
                    Map<String, Object> root = SimpleJson.parseObject(Files.readString(file, StandardCharsets.UTF_8));
                    if (matchesApplication(root, key)) {
                        out.add(root);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object v) { return v instanceof Map ? (Map<String, Object>) v : Map.of(); }

    private boolean matches(Map<String, Object> root, String key) {
        Map<String, Object> profile = map(root.get("profile"));
        Map<String, Object> account = map(root.get("account"));
        return key.equals(text(root, "userId", ""))
                || key.equals(text(root, "loginId", ""))
                || key.equals(text(profile, "studentId", ""))
                || key.equals(text(account, "username", ""));
    }

    private boolean matchesApplication(Map<String, Object> root, String key) {
        Map<String, Object> app = map(root.get("applicantSnapshot"));
        Map<String, Object> account = map(root.get("account"));
        Map<String, Object> workflow = map(root.get("workflow"));
        Map<String, Object> review = map(root.get("review"));
        return key.equals(text(root, "userId", ""))
                || key.equals(text(root, "studentId", ""))
                || key.equals(text(app, "studentId", ""))
                || key.equals(text(app, "fullName", ""))
                || key.equals(text(app, "email", ""))
                || key.equals(text(account, "username", ""))
                || key.equals(text(workflow, "assignedMO", ""))
                || key.equals(text(review, "reviewedBy", ""))
                || key.equals(text(review, "updatedBy", ""));
    }

    private static String text(Map<String, Object> obj, String key, String def) { Object v = obj == null ? null : obj.get(key); return v == null ? def : String.valueOf(v); }
}
