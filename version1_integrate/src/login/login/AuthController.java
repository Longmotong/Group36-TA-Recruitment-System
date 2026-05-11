package login;

public class AuthController {

    private AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public boolean handleLogin(String username, String password) {
        User user = authService.login(username, password);
        if (user != null) {
            SessionManager.login(user);
            return true;
        }
        return false;
    }

    public boolean handleRegister(String username, String password, String role) {
        return authService.register(username, password, role);
    }

    public String getUserRole(String username) {
        return authService.getRoleByUsername(username);
    }
}
