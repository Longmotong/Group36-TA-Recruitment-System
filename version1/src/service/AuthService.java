import java.util.ArrayList;
import java.util.List;

import model.User;
import util.JsonUtil;

public class AuthService {

    // 从JSON加载所有用户
    private List<User> users = JsonUtil.loadAllUsers();

    // Register
    public boolean register(String username, String password, String role) {

        // 检查用户名是否已存在（跨所有角色）
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false; // Username already exists
            }
        }

        // 创建新用户
        User newUser = new User(username, password, role);

        // 添加到内存
        users.add(newUser);

        // 保存到对应角色的JSON文件
        JsonUtil.saveUser(newUser);

        return true;
    }

    // Login
    public User login(String username, String password) {

        // 每次登录重新加载（防止数据不同步 ⭐加分点）
        users = JsonUtil.loadAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {
                return user;
            }
        }

        return null;
    }
}
