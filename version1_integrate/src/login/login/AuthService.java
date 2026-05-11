package login;

import java.util.List;

public class AuthService {

    private List<User> users;

    public AuthService() {
        users = JsonUtil.loadAllUsers();
    }

    public User login(String username, String password) {
        users = JsonUtil.loadAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword() != null
                    && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public String getRoleByUsername(String username) {
        users = JsonUtil.loadAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.getRole();
            }
        }
        return null;
    }

    public boolean register(String username, String password, String role) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        if (JsonUtil.usernameExists(username.trim())) {
            return false;
        }
        String r = role == null ? "ta" : role.trim().toLowerCase();
        return switch (r) {
            case "ta" -> JsonUtil.saveNewTaUser(username.trim(), password);
            case "mo" -> JsonUtil.saveNewMoUser(username.trim(), password);
            case "admin" -> JsonUtil.saveNewAdminUser(username.trim(), password);
            default -> false;
        };
    }
}
