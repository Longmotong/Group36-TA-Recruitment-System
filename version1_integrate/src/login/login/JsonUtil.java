package login;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads account credentials from JSON under {@code data/users/{mo,ta,admin}/} (same layout as Job_application_module).
 */
public class JsonUtil {

    private static final JSONParser parser = new JSONParser();
    private static final Pattern MO_FILE_NUM = Pattern.compile("user_mo_(\\d+)\\.json", Pattern.CASE_INSENSITIVE);

    private static Path usersBase() {
        return AppDataRoot.asPath().resolve("users");
    }

    private static File moDir() {
        return usersBase().resolve("mo").toFile();
    }

    private static File taDir() {
        return usersBase().resolve("ta").toFile();
    }

    private static File adminDir() {
        return usersBase().resolve("admin").toFile();
    }

    public static File dataUsersRoot() {
        return usersBase().toFile();
    }

    public static List<User> loadAllUsers() {
        List<User> allUsers = new ArrayList<>();
        collectUsersFromDir(moDir(), allUsers);
        collectUsersFromDir(taDir(), allUsers);
        collectUsersFromDir(adminDir(), allUsers);
        return allUsers;
    }

    private static void collectUsersFromDir(File folder, List<User> out) {
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                User u = parseUserFile(file);
                if (u != null) {
                    out.add(u);
                }
            } catch (Exception e) {
                System.err.println("Error loading user from: " + file.getName());
            }
        }
    }

    private static User parseUserFile(File file) throws Exception {
        JSONObject root = (JSONObject) parser.parse(new FileReader(file, StandardCharsets.UTF_8));

        String username = "";
        String password = "";
        String role = root.containsKey("role") ? String.valueOf(root.get("role")) : "mo";
        String userId = root.containsKey("userId") ? String.valueOf(root.get("userId")) : "";
        String fullName = "";
        String department = "";
        String email = "";

        if (root.containsKey("account")) {
            JSONObject account = (JSONObject) root.get("account");
            username = account.containsKey("username") ? String.valueOf(account.get("username")) : "";
            if (account.containsKey("password")) {
                password = String.valueOf(account.get("password"));
            }
        }

        if (root.containsKey("profile")) {
            JSONObject profile = (JSONObject) root.get("profile");
            fullName = profile.containsKey("fullName") ? String.valueOf(profile.get("fullName")) : "";
            department = profile.containsKey("department") ? String.valueOf(profile.get("department")) : "";
        }

        if (root.containsKey("account") && ((JSONObject) root.get("account")).containsKey("email")) {
            email = String.valueOf(((JSONObject) root.get("account")).get("email"));
        }

        if (username.isEmpty()) {
            return null;
        }

        return new User(username, password, role.toLowerCase(Locale.ROOT), userId, userId, fullName, department, email);
    }

    public static boolean usernameExists(String username) {
        if (username == null || username.isBlank()) {
            return true;
        }
        String target = username.trim().toLowerCase(Locale.ROOT);
        for (User u : loadAllUsers()) {
            if (u.getUsername() != null && u.getUsername().trim().toLowerCase(Locale.ROOT).equals(target)) {
                return true;
            }
        }
        return false;
    }

    public static boolean saveNewTaUser(String username, String password) {
        String studentId = sanitizeStudentId(username);
        File dir = taDir();
        if (!dir.exists() && !dir.mkdirs()) {
            return false;
        }
        File out = new File(dir, "user_ta_" + studentId + ".json");
        if (out.exists()) {
            return false;
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String userId = "u_ta_" + studentId;
        JSONObject root = new JSONObject();
        root.put("userId", userId);
        root.put("loginId", studentId);
        root.put("role", "ta");

        JSONObject account = new JSONObject();
        account.put("username", username.trim());
        account.put("email", username.trim().toLowerCase(Locale.ROOT) + "@student.university.edu");
        account.put("password", password);
        account.put("status", "active");
        account.put("lastLoginAt", now);
        root.put("account", account);

        JSONObject profile = new JSONObject();
        profile.put("fullName", "");
        profile.put("studentId", studentId);
        profile.put("year", "");
        profile.put("programMajor", "");
        profile.put("department", "");
        profile.put("phoneNumber", "");
        profile.put("address", "");
        profile.put("shortBio", "");
        root.put("profile", profile);

        JSONObject academic = new JSONObject();
        academic.put("gpa", 0.0);
        academic.put("completedCourses", new JSONArray());
        root.put("academic", academic);

        JSONObject skills = new JSONObject();
        skills.put("programming", new JSONArray());
        skills.put("teaching", new JSONArray());
        skills.put("communication", new JSONArray());
        skills.put("other", new JSONArray());
        root.put("skills", skills);

        JSONObject cv = new JSONObject();
        cv.put("uploaded", false);
        cv.put("originalFileName", "");
        cv.put("storedFileName", "");
        cv.put("filePath", "");
        cv.put("fileType", "");
        cv.put("fileSizeKB", 0);
        cv.put("uploadedAt", "");
        root.put("cv", cv);

        JSONObject applicationSummary = new JSONObject();
        applicationSummary.put("totalApplications", 0);
        applicationSummary.put("pending", 0);
        applicationSummary.put("underReview", 0);
        applicationSummary.put("accepted", 0);
        applicationSummary.put("rejected", 0);
        root.put("applicationSummary", applicationSummary);

        JSONObject dashboard = new JSONObject();
        dashboard.put("profileCompletion", 0);
        root.put("dashboard", dashboard);

        JSONObject permissions = new JSONObject();
        permissions.put("canEditOwnProfile", true);
        permissions.put("canUploadCV", true);
        permissions.put("canBrowseJobs", true);
        permissions.put("canApplyJob", true);
        permissions.put("canViewOwnApplications", true);
        permissions.put("canReviewApplication", false);
        permissions.put("canManageJob", false);
        permissions.put("canManageUsers", false);
        root.put("permissions", permissions);

        JSONObject meta = new JSONObject();
        meta.put("createdAt", now);
        meta.put("updatedAt", now);
        meta.put("isDeleted", false);
        meta.put("isActive", true);
        root.put("meta", meta);

        return writeJsonFile(out, root);
    }

    public static boolean saveNewMoUser(String username, String password) {
        File dir = moDir();
        if (!dir.exists() && !dir.mkdirs()) {
            return false;
        }
        int nextNum = nextMoFileNumber(dir);
        String moNum = String.format("%03d", nextNum);
        String userId = "u_mo_" + moNum;
        String loginId = "mo" + moNum;
        File out = new File(dir, "user_mo_" + moNum + ".json");
        if (out.exists()) {
            return false;
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        JSONObject root = new JSONObject();
        root.put("userId", userId);
        root.put("loginId", loginId);
        root.put("role", "mo");

        JSONObject account = new JSONObject();
        account.put("username", username.trim());
        account.put("email", username.trim().toLowerCase(Locale.ROOT) + "@university.edu");
        account.put("password", password);
        account.put("status", "active");
        account.put("lastLoginAt", now);
        root.put("account", account);

        JSONObject profile = new JSONObject();
        profile.put("fullName", username.trim());
        profile.put("staffId", "MO" + moNum);
        profile.put("department", "");
        profile.put("phoneNumber", "");
        root.put("profile", profile);

        JSONObject permissions = new JSONObject();
        permissions.put("canEditOwnProfile", true);
        permissions.put("canCreateJob", true);
        permissions.put("canEditManagedJob", true);
        permissions.put("canDeleteManagedJob", true);
        permissions.put("canPublishManagedJob", true);
        permissions.put("canViewManagedJob", true);
        permissions.put("canViewAssignedApplications", true);
        permissions.put("canReviewApplication", true);
        permissions.put("canUpdateApplicationStatus", true);
        permissions.put("canManageUsers", false);
        permissions.put("canViewAllJobs", false);
        permissions.put("canViewAllApplications", false);
        root.put("permissions", permissions);

        JSONObject scope = new JSONObject();
        scope.put("departments", new JSONArray());
        scope.put("managedJobIds", new JSONArray());
        root.put("scope", scope);

        JSONObject dashboard = new JSONObject();
        dashboard.put("managedJobsCount", 0);
        dashboard.put("pendingReviewsCount", 0);
        root.put("dashboard", dashboard);

        JSONObject meta = new JSONObject();
        meta.put("createdAt", now);
        meta.put("updatedAt", now);
        meta.put("isDeleted", false);
        meta.put("isActive", true);
        root.put("meta", meta);

        return writeJsonFile(out, root);
    }

    public static boolean saveNewAdminUser(String username, String password) {
        File dir = adminDir();
        if (!dir.exists() && !dir.mkdirs()) {
            return false;
        }
        int nextNum = nextAdminFileNumber(dir);
        String num = String.format("%03d", nextNum);
        String userId = "u_admin_" + num;
        File out = new File(dir, "user_admin_" + num + ".json");
        if (out.exists()) {
            return false;
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        JSONObject root = new JSONObject();
        root.put("userId", userId);
        root.put("loginId", "admin" + num);
        root.put("role", "admin");

        JSONObject account = new JSONObject();
        account.put("username", username.trim());
        account.put("email", username.trim().toLowerCase(Locale.ROOT) + "@university.edu");
        account.put("password", password);
        account.put("status", "active");
        account.put("lastLoginAt", now);
        root.put("account", account);

        JSONObject profile = new JSONObject();
        profile.put("fullName", username.trim());
        profile.put("staffId", "ADMIN" + num);
        profile.put("phoneNumber", "");
        root.put("profile", profile);

        JSONObject permissions = new JSONObject();
        permissions.put("canEditOwnProfile", true);
        permissions.put("canCreateJob", true);
        permissions.put("canEditAnyJob", true);
        permissions.put("canDeleteAnyJob", true);
        permissions.put("canPublishAnyJob", true);
        permissions.put("canViewAllJobs", true);
        permissions.put("canReviewApplication", true);
        permissions.put("canUpdateApplicationStatus", true);
        permissions.put("canViewAllApplications", true);
        permissions.put("canManageUsers", true);
        permissions.put("canAssignMO", true);
        permissions.put("canViewSystemStatistics", true);
        root.put("permissions", permissions);

        JSONObject dashboard = new JSONObject();
        dashboard.put("systemHealth", "ok");
        root.put("dashboard", dashboard);

        JSONObject meta = new JSONObject();
        meta.put("createdAt", now);
        meta.put("updatedAt", now);
        meta.put("isDeleted", false);
        meta.put("isActive", true);
        root.put("meta", meta);

        return writeJsonFile(out, root);
    }

    private static boolean writeJsonFile(File out, JSONObject root) {
        try (FileWriter fw = new FileWriter(out, StandardCharsets.UTF_8)) {
            fw.write(root.toJSONString());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to write " + out + ": " + e.getMessage());
            return false;
        }
    }

    private static int nextMoFileNumber(File dir) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).startsWith("user_mo_") && name.endsWith(".json"));
        int max = 0;
        if (files != null) {
            for (File f : files) {
                Matcher m = MO_FILE_NUM.matcher(f.getName());
                if (m.matches()) {
                    max = Math.max(max, Integer.parseInt(m.group(1)));
                }
            }
        }
        return max + 1;
    }

    private static int nextAdminFileNumber(File dir) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).startsWith("user_admin_") && name.endsWith(".json"));
        int max = 0;
        if (files != null) {
            for (File f : files) {
                Matcher m = Pattern.compile("user_admin_(\\d+)\\.json", Pattern.CASE_INSENSITIVE).matcher(f.getName());
                if (m.matches()) {
                    max = Math.max(max, Integer.parseInt(m.group(1)));
                }
            }
        }
        return max + 1;
    }

    private static String sanitizeStudentId(String username) {
        String base = username.replaceAll("[^a-zA-Z0-9]", "");
        if (base.isEmpty()) {
            base = "ta" + System.currentTimeMillis();
        }
        return base.toLowerCase(Locale.ROOT);
    }

    public static List<User> loadUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        for (User u : loadAllUsers()) {
            if (role != null && role.equalsIgnoreCase(u.getRole())) {
                list.add(u);
            }
        }
        return list;
    }

    public static void saveUser(User user) {
        // MO profile edits remain module-owned; login only creates accounts via saveNew*User.
    }
}
