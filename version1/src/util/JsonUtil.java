import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.User;

public class JsonUtil {

    private static final String BASE_PATH = "data/users/";

    // 根据角色获取文件路径
    private static String getFilePath(String role) {
        return BASE_PATH + role.toLowerCase() + ".json";
    }

    // 保存用户（按角色分类）
    public static void saveUser(User user) {
        List<User> users = loadUsersByRole(user.getRole());
        users.add(user);

        JSONArray jsonArray = new JSONArray();

        for (User u : users) {
            JSONObject obj = new JSONObject();
            obj.put("username", u.getUsername());
            obj.put("password", u.getPassword());
            obj.put("role", u.getRole());
            jsonArray.add(obj);
        }

        try {
            String filePath = getFilePath(user.getRole());

            // 确保目录存在
            new File(BASE_PATH).mkdirs();

            FileWriter file = new FileWriter(filePath);
            file.write(jsonArray.toJSONString());
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 加载某个角色的用户
    public static List<User> loadUsersByRole(String role) {
        List<User> users = new ArrayList<>();

        try {
            String filePath = getFilePath(role);
            JSONParser parser = new JSONParser();
            File file = new File(filePath);

            if (!file.exists()) {
                return users;
            }

            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(filePath));

            for (Object obj : jsonArray) {
                JSONObject json = (JSONObject) obj;

                String username = (String) json.get("username");
                String password = (String) json.get("password");
                String userRole = (String) json.get("role");

                users.add(new User(username, password, userRole));
            }

        } catch (Exception e) {
            // ignore
        }

        return users;
    }

    // 加载所有用户（登录用）
    public static List<User> loadAllUsers() {
        List<User> allUsers = new ArrayList<>();

        allUsers.addAll(loadUsersByRole("TA"));
        allUsers.addAll(loadUsersByRole("MO"));
        allUsers.addAll(loadUsersByRole("Admin"));

        return allUsers;
    }
}
