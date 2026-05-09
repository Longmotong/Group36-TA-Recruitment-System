package Authentication_Module.service;

import Authentication_Module.model.User;
import Authentication_Module.util.JsonUtil;

import java.util.List;

public class AuthService {

    private List<User> users;

    public AuthService() {
        
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

        
        users = JsonUtil.loadAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username) && user.passwordMatches(password)) {
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
}