package service;

import model.User;
import util.JsonUtil;

import java.util.List;

public class AuthService {

    private List<User> users;

    public AuthService() {
        // 初始化时加载所有用户
        users = JsonUtil.loadAllUsers();
    }

    // ========================
    // Register
    // ========================
    public boolean register(String username, String password, String role) {

        //
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("Password invalid");
        }

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }

        User newUser = new User(username, password, role);
        users.add(newUser);
        JsonUtil.saveUser(newUser);

        return true;
    }

    // ========================
    // Login
    // ========================
    public User login(String username, String password) {

        // 每次登录重新加载（防止不同步 ⭐）
        users = JsonUtil.loadAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {
                return user;
            }
        }

        return null;
    }

    // ========================
    // 获取角色（用于页面跳转）
    // ========================
    public String getRoleByUsername(String username) {

        users = JsonUtil.loadAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.getRole();
            }
        }

        return null;
    }
}