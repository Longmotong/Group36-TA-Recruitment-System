package com.taapp.data;

import com.taapp.model.CurrentUser;
import com.taapp.model.Position;
import com.taapp.model.Statistics;
import com.taapp.model.TA;
import com.taapp.util.SimpleJson;
import Authentication_Module.session.SessionManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private DataStore() {
    }

    public static DataStore defaultStore() { return INSTANCE; }
    public CurrentUser getCurrentUser() {
        Authentication_Module.model.User authUser = SessionManager.getCurrentUser();
        if (authUser != null) {
            String loginId = authUser.getSystemUserId();
            String username = authUser.getUsername();
            String role = authUser.getRole();
            if (loginId == null || loginId.isBlank()) {
                loginId = username;
            }
            if (role == null || role.isBlank()) {
                role = "admin";
            }
            return new CurrentUser(
                    loginId,
                    loginId,
                    role.toLowerCase(),
                    username == null || username.isBlank() ? loginId : username,
                    (username == null || username.isBlank() ? "admin" : username) + "@university.edu"
            );
        }
        String login = System.getProperty("user.name", "user");
        return loadCurrentUser(login);
    }
    public Statistics getStatistics() { return new JsonDataRepository().loadStatistics(); }
    public List<Position> getPositions() { return new JsonDataRepository().loadPositions(); }
    public List<TA> getTAs() { return new JsonDataRepository().loadTAs(); }

    private CurrentUser loadCurrentUser(String login) {
        Path dataRoot = DataRoot.resolve();
        Path userFile = dataRoot.resolve("users").resolve("ta").resolve("user_ta_" + login + ".json");
        if (!Files.isRegularFile(userFile)) {
            Path fallback = dataRoot.resolve("users").resolve("ta").resolve("user_ta_20230016.json");
            if (Files.isRegularFile(fallback)) {
                userFile = fallback;
            }
        }
        if (Files.isRegularFile(userFile)) {
            try {
                Map<String, Object> root = SimpleJson.parseObject(Files.readString(userFile, StandardCharsets.UTF_8));
                Map<String, Object> account = map(root.get("account"));
                Map<String, Object> profile = map(root.get("profile"));
                return new CurrentUser(
                        text(root, "userId", login),
                        text(root, "loginId", login),
                        text(root, "role", "ta"),
                        text(profile, "fullName", login),
                        text(account, "email", login + "@localhost")
                );
            } catch (Exception ignored) {
            }
        }
        return new CurrentUser(login, login, "ta", login, login + "@localhost");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object v) { return v instanceof Map ? (Map<String, Object>) v : Map.of(); }
    private static String text(Map<String, Object> obj, String key, String def) { Object v = obj == null ? null : obj.get(key); return v == null ? def : String.valueOf(v); }
}
