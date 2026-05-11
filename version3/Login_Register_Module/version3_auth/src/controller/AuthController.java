package controller;

import model.User;
import service.AuthService;

public class AuthController {

    private AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    // 登录
    public User handleLogin(String username, String password) {
        return authService.login(username, password);
    }

    // 注册
    public boolean handleRegister(String username, String password, String role) {

        System.out.println(">>> register called: " + password);

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include letters and numbers"
            );
        }

        return authService.register(username, password, role);
    }
}