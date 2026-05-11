package controller;

import model.User;
import service.AuthService;

public class AuthController {

    private AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    // 登录
    public boolean handleLogin(String username, String password) {
        User user = authService.login(username, password);
        return user != null;
    }

    // 注册
    public boolean handleRegister(String username, String password, String role) {

        System.out.println(">>> register called: " + password);

        // 一行强校验（推荐）
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include letters and numbers"
            );
        }

        return authService.register(username, password, role);
    }

    // 获取角色（用于跳转）
    public String getUserRole(String username) {
        return authService.getRoleByUsername(username);
    }
}