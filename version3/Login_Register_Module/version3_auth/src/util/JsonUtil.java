package util;

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

    // 获取角色目录
    private static String getRoleDir(String role) {
        return BASE_PATH + role + "/";
    }

    // 生成文件名（自动编号）
    private static String generateFileName(String role) {
        String dirPath = getRoleDir(role);
        File dir = new File(dirPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        int count = (files == null) ? 0 : files.length;

        // 例如：user_mo_001.json
        return String.format("user_%s_%03d.json", role.toLowerCase(), count + 1);
    }

    // 保存用户（一个用户一个文件）
    public static void saveUser(User user) {
        try {
            String dirPath = getRoleDir(user.getRole());
            File dir = new File(dirPath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            // ❗防止用户名重复（跨角色）
            List<User> allUsers = loadAllUsers();
            for (User u : allUsers) {
                if (u.getUsername().equals(user.getUsername())) {
                    System.out.println("用户名已存在！");
                    return;
                }
            }

            String fileName = generateFileName(user.getRole());
            String filePath = dirPath + fileName;

            JSONObject obj = new JSONObject();
            obj.put("username", user.getUsername());
            obj.put("password", user.getPassword());

            FileWriter writer = new FileWriter(filePath);
            writer.write(obj.toJSONString());
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 加载某个角色的所有用户
    public static List<User> loadUsersByRole(String role) {
        List<User> users = new ArrayList<>();

        try {
            String dirPath = getRoleDir(role);
            File dir = new File(dirPath);

            if (!dir.exists()) return users;

            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

            if (files == null) return users;

            JSONParser parser = new JSONParser();

            for (File file : files) {
                JSONObject json = (JSONObject) parser.parse(new FileReader(file));

                String username = (String) json.get("username");
                String password = (String) json.get("password");

                users.add(new User(username, password, role));
            }

        } catch (Exception e) {
            e.printStackTrace();
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
    //MO判断是否首次登录
    public static boolean isFirstLogin(User user) {
        try {
            String dirPath = BASE_PATH + user.getRole() + "/";
            File dir = new File(dirPath);

            if (!dir.exists()) return true;

            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

            if (files == null) return true;

            JSONParser parser = new JSONParser();

            for (File file : files) {
                JSONObject json = (JSONObject) parser.parse(new FileReader(file));

                String username = (String) json.get("username");

                if (username != null && username.equals(user.getUsername())) {

                    // 👉 取 profile
                    JSONObject profile = (JSONObject) json.get("profile");

                    if (profile == null) return true;

                    String fullName = (String) profile.get("fullName");

                    return (fullName == null || fullName.trim().isEmpty());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void updateMOProfile(User user,
                                       String fullName,
                                       String staffId,
                                       String department,
                                       String phone,
                                       String school,
                                       String email,
                                       String campus,
                                       String courseTypes) {

        try {
            String dirPath = BASE_PATH + user.getRole() + "/";
            File dir = new File(dirPath);

            if (!dir.exists()) return;

            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files == null) return;

            JSONParser parser = new JSONParser();

            for (File file : files) {
                JSONObject json = (JSONObject) parser.parse(new FileReader(file));

                String username = (String) json.get("username");

                if (username != null && username.equals(user.getUsername())) {

                    // ===== profile =====
                    JSONObject profile = (JSONObject) json.get("profile");
                    if (profile == null) profile = new JSONObject();

                    profile.put("fullName", fullName);
                    profile.put("staffId", staffId);
                    profile.put("department", department);
                    profile.put("phoneNumber", phone);

                    // ===== identityInformationRequired =====
                    JSONObject identity = new JSONObject();
                    identity.put("school", school);
                    identity.put("campusEmail", email);
                    profile.put("identityInformationRequired", identity);

                    // ===== courseInformationRequired =====
                    JSONObject course = new JSONObject();

                    // 处理 courseTypes（逗号分隔）
                    org.json.simple.JSONArray courseArray = new org.json.simple.JSONArray();
                    if (courseTypes != null && !courseTypes.isEmpty()) {
                        String[] parts = courseTypes.split(",");
                        for (String p : parts) {
                            courseArray.add(p.trim());
                        }
                    }

                    course.put("courseTypes", courseArray);
                    course.put("teachingCampus", campus);

                    profile.put("courseInformationRequired", course);

                    // 写回 profile
                    json.put("profile", profile);

                    // ===== 写回文件 =====
                    FileWriter writer = new FileWriter(file);
                    writer.write(json.toJSONString());
                    writer.close();

                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}