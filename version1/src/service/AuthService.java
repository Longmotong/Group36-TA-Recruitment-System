import java.util.ArrayList;
import java.util.List;
import model.User;
public class AuthService {

    private List<User> users = new ArrayList<>();

    // Register
    public boolean register(String username, String password, String role) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false; // Username already exists
            }
        }
        users.add(new User(username, password, role));
        return true;
    }

    // Login
    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
