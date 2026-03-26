import model.User;
import service.AuthService;
import session.SessionManager;

public class AuthController {

    private AuthService authService = new AuthService();

    // Register
    public void handleRegister(String username, String password, String role) {
        boolean success = authService.register(username, password, role);

        if (success) {
            System.out.println("Register successful.");
        } else {
            System.out.println("Username already exists.");
        }
    }

    // Login
    public void handleLogin(String username, String password) {
        User user = authService.login(username, password);

        if (user == null) {
            System.out.println("Login failed.");
        } else {
            SessionManager.login(user);
            System.out.println("Login successful.");
            redirectByRole(user);
        }
    }

    // Role-based navigation (先用占位)
    private void redirectByRole(User user) {
        String role = user.getRole();

        switch (role) {
            case "TA":
                System.out.println("[Placeholder] Go to TA Home Page");
                break;
            case "MO":
                System.out.println("[Placeholder] Go to MO Home Page");
                break;
            case "Admin":
                System.out.println("[Placeholder] Go to Admin Home Page");
                break;
            default:
                System.out.println("Unknown role.");
        }
    }

    // Logout
    public void handleLogout() {
        SessionManager.logout();
        System.out.println("Logged out.");
    }
}
