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
        return authService.register(username, password, role);
    }

    // 获取角色（用于跳转）
    public String getUserRole(String username) {
        return authService.getRoleByUsername(username);
    }
}