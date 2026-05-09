package Authentication_Module.util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.Writer;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Authentication_Module.model.User;

public class JsonUtil {

    private static final String BASE_PATH = "data/users/";
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<String> PROFICIENCY_LEVELS = List.of("Beginner", "Intermediate", "Advanced");
    private static final List<String> SKILL_PROGRAMMING = List.of(
            "Java", "Python", "C/C++", "SQL", "Algorithms & Data Structures", "Object-Oriented Programming (OOP)");
    private static final List<String> SKILL_HARDWARE = List.of(
            "VHDL", "Verilog", "Digital Logic Design", "FPGA Development & Debugging");
    private static final List<String> SKILL_EMBEDDED = List.of(
            "STM32 Development", "FreeRTOS", "Embedded C", "Hardware Driver Development");
    private static final List<String> SKILL_TOOLS = List.of(
            "Quartus Prime", "Keil5", "STM32CubeIDE", "STM32CubeMX", "CST Studio Suite", "Matlab / Simulink", "Cisco Packet Tracer");
    private static final List<String> SKILL_LANGUAGE = List.of("English");

    
    private static String getFilePath(String role) {
        return BASE_PATH + role.toLowerCase() + ".json";
    }

   
    public static void saveUser(User user) {
        String role = user.getRole();

       
        if ("MO".equalsIgnoreCase(role)) {
            saveMoUserToIndividualFile(user);
            return;
        }

        
        if ("TA".equalsIgnoreCase(role)) {
            saveTaUserToIndividualFile(user);
            return;
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            saveAdminUserToIndividualFile(user);
            return;
        }

        
        List<User> users = loadUsersByRole(role);
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
            String filePath = getFilePath(role);

           
            new File(BASE_PATH).mkdirs();

            FileWriter file = new FileWriter(filePath);
            file.write(jsonArray.toJSONString());
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private static void saveMoUserToIndividualFile(User user) {
        try {
            String roleLower = "mo";
            String dirPath = BASE_PATH + roleLower + File.separator;
            new File(dirPath).mkdirs();

            
            int nextNumber = getNextMoFileNumber(dirPath);

            
            String fileName = "user_mo_" + String.format("%03d", nextNumber) + ".json";
            String filePath = dirPath + fileName;

            
            String now = java.time.LocalDateTime.now().toString();

            
            String numberStr = String.format("%03d", nextNumber);

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("userId", "u_mo_" + numberStr);
            root.put("loginId", "mo" + numberStr);
            root.put("role", roleLower);

            Map<String, Object> account = new LinkedHashMap<>();
            account.put("username", user.getUsername());
            account.put("email", user.getUsername() + "@university.edu");
            account.put("passwordHash", "hashed_" + user.getUsername());
            account.put("password", user.getPassword());
            account.put("status", "active");
            account.put("lastLoginAt", now);
            root.put("account", account);

            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("fullName", user.getUsername());
            profile.put("staffId", "");
            profile.put("department", "");
            profile.put("phoneNumber", "");
            Map<String, Object> identity = new LinkedHashMap<>();
            identity.put("school", "");
            identity.put("campusEmail", user.getUsername() + "@university.edu");
            profile.put("identityInformationRequired", identity);
            Map<String, Object> course = new LinkedHashMap<>();
            course.put("courseTypes", List.of());
            course.put("teachingCampus", "");
            profile.put("courseInformationRequired", course);
            profile.put("taSkillPoolCatalog", createMoSkillCatalog());
            profile.put("jobPostingSkillTemplate", createMoJobPostingSkillTemplate());
            root.put("profile", profile);

            Map<String, Object> permissions = new LinkedHashMap<>();
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

            Map<String, Object> scope = new LinkedHashMap<>();
            scope.put("departments", List.of());
            scope.put("managedJobIds", List.of());
            root.put("scope", scope);

            Map<String, Object> dashboard = new LinkedHashMap<>();
            dashboard.put("managedJobsCount", 0);
            dashboard.put("pendingReviewsCount", 0);
            root.put("dashboard", dashboard);

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("createdAt", now);
            meta.put("updatedAt", now);
            meta.put("isDeleted", false);
            meta.put("isActive", true);
            root.put("meta", meta);

            writePrettyJsonToFile(filePath, root);

            System.out.println("[JsonUtil] MO user saved to: " + filePath);

            ensureMoJobsIndexHasUser((String) root.get("userId"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   
    private static void ensureMoJobsIndexHasUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try {
            Path indexPath = Path.of("data", "indexes", "mo_jobs_index.json");
            Files.createDirectories(indexPath.getParent());
            JSONParser parser = new JSONParser();
            JSONObject root;
            if (Files.exists(indexPath) && Files.size(indexPath) > 0) {
                try (FileReader r = new FileReader(indexPath.toFile())) {
                    Object o = parser.parse(r);
                    root = o instanceof JSONObject ? (JSONObject) o : new JSONObject();
                }
            } else {
                root = new JSONObject();
            }
            Object mo = root.get("moJobs");
            JSONObject moJobs;
            if (mo instanceof JSONObject) {
                moJobs = (JSONObject) mo;
            } else {
                moJobs = new JSONObject();
                root.put("moJobs", moJobs);
            }
            if (!moJobs.containsKey(userId)) {
                moJobs.put(userId, new JSONArray());
            }
            try (FileWriter w = new FileWriter(indexPath.toFile())) {
                w.write(toFormattedJson(root));
            }
        } catch (Exception e) {
            System.err.println("[JsonUtil] Could not update mo_jobs_index for " + userId + ": " + e.getMessage());
        }
    }

    
    private static void saveTaUserToIndividualFile(User user) {
        try {
            String dirPath = BASE_PATH + "ta" + File.separator;
            new File(dirPath).mkdirs();

           
            int nextNumber = getNextTaFileNumber(dirPath);

           
            String studentId = String.format("%08d", nextNumber);

            
            String fileName = "user_ta_" + studentId + ".json";
            String filePath = dirPath + fileName;

          
            String now = java.time.LocalDateTime.now().toString();

            
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("userId", "u_ta_" + studentId);
            root.put("loginId", studentId);
            root.put("role", "ta");

            Map<String, Object> account = new LinkedHashMap<>();
            account.put("username", user.getUsername());
            account.put("email", user.getUsername() + "@university.edu");
            account.put("passwordHash", "hashed_" + user.getUsername());
            account.put("password", user.getPassword());
            account.put("status", "active");
            account.put("lastLoginAt", now);
            root.put("account", account);

            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("fullName", user.getUsername());
            profile.put("studentId", studentId);
            profile.put("year", "");
            profile.put("programMajor", "");
            profile.put("department", "");
            profile.put("phoneNumber", "");
            profile.put("address", "");
            profile.put("shortBio", "");
            root.put("profile", profile);

            Map<String, Object> academic = new LinkedHashMap<>();
            academic.put("gpa", 0.0);
            academic.put("completedCourses", List.of());
            root.put("academic", academic);

            root.put("skills", createTaSkillsTemplate());

            Map<String, Object> cv = new LinkedHashMap<>();
            cv.put("uploaded", false);
            cv.put("originalFileName", "");
            cv.put("storedFileName", "");
            cv.put("filePath", "");
            cv.put("fileType", "");
            cv.put("fileSizeKB", 0);
            cv.put("uploadedAt", "");
            root.put("cv", cv);

            Map<String, Object> applicationSummary = new LinkedHashMap<>();
            applicationSummary.put("totalApplications", 0);
            applicationSummary.put("pending", 0);
            applicationSummary.put("underReview", 0);
            applicationSummary.put("accepted", 0);
            applicationSummary.put("rejected", 0);
            root.put("applicationSummary", applicationSummary);

            Map<String, Object> dashboard = new LinkedHashMap<>();
            dashboard.put("profileCompletion", 0);
            dashboard.put("onboardingCompleted", false);
            root.put("dashboard", dashboard);

            Map<String, Object> permissions = new LinkedHashMap<>();
            permissions.put("canEditOwnProfile", true);
            permissions.put("canUploadCV", true);
            permissions.put("canBrowseJobs", true);
            permissions.put("canApplyJob", true);
            permissions.put("canViewOwnApplications", true);
            permissions.put("canReviewApplication", false);
            permissions.put("canManageJob", false);
            permissions.put("canManageUsers", false);
            root.put("permissions", permissions);

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("createdAt", now);
            meta.put("updatedAt", now);
            meta.put("isDeleted", false);
            meta.put("isActive", true);
            root.put("meta", meta);

            writePrettyJsonToFile(filePath, root);

            System.out.println("[JsonUtil] TA user saved to: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveAdminUserToIndividualFile(User user) {
        try {
            String dirPath = BASE_PATH + "admin" + File.separator;
            new File(dirPath).mkdirs();
            int nextNumber = getNextAdminFileNumber(dirPath);
            String numberStr = String.format("%03d", nextNumber);
            String fileName = "user_admin_" + numberStr + ".json";
            String filePath = dirPath + fileName;
            String now = java.time.LocalDateTime.now().toString();

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("userId", "u_admin_" + numberStr);
            root.put("loginId", "admin" + numberStr);
            root.put("role", "admin");

            Map<String, Object> account = new LinkedHashMap<>();
            account.put("username", user.getUsername());
            account.put("email", user.getUsername() + "@university.edu");
            account.put("passwordHash", "hashed_" + user.getUsername());
            account.put("password", user.getPassword());
            account.put("status", "active");
            account.put("lastLoginAt", now);
            root.put("account", account);

            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("fullName", user.getUsername());
            profile.put("staffId", "ADMIN" + numberStr);
            profile.put("phoneNumber", "");
            root.put("profile", profile);

            Map<String, Object> permissions = new LinkedHashMap<>();
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

            Map<String, Object> dashboard = new LinkedHashMap<>();
            dashboard.put("totalUsers", 0);
            dashboard.put("totalJobs", 0);
            dashboard.put("totalApplications", 0);
            root.put("dashboard", dashboard);

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("createdAt", now);
            meta.put("updatedAt", now);
            meta.put("isDeleted", false);
            meta.put("isActive", true);
            root.put("meta", meta);

            writePrettyJsonToFile(filePath, root);
            System.out.println("[JsonUtil] Admin user saved to: " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private static int getNextTaFileNumber(String dirPath) {
        File dir = new File(dirPath);
        int maxNumber = 0;

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.matches("user_ta_\\d+\\.json"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    String numStr = name.replace("user_ta_", "").replace(".json", "");
                    try {
                        int num = Integer.parseInt(numStr);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return maxNumber + 1;
    }
    private static int getNextMoFileNumber(String dirPath) {
        File dir = new File(dirPath);
        int maxNumber = 0;

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.matches("user_mo_\\d+\\.json"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    String numStr = name.replace("user_mo_", "").replace(".json", "");
                    try {
                        int num = Integer.parseInt(numStr);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return maxNumber + 1;
    }

    private static int getNextAdminFileNumber(String dirPath) {
        File dir = new File(dirPath);
        int maxNumber = 0;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.matches("user_admin_\\d+\\.json"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    String numStr = name.replace("user_admin_", "").replace(".json", "");
                    try {
                        int num = Integer.parseInt(numStr);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return maxNumber + 1;
    }

    private static void writePrettyJsonToFile(String filePath, Map<String, Object> root) throws Exception {
        String json = PRETTY_GSON.toJson(root);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(json);
        }
    }

    private static Map<String, Object> createTaSkillsTemplate() {
        Map<String, Object> skills = new LinkedHashMap<>();
        skills.put("proficiencyLevels", PROFICIENCY_LEVELS);
        Map<String, Object> taSkillPool = new LinkedHashMap<>();
        taSkillPool.put("technicalSkills", createTaTechnicalSkills());
        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("professionalDevelopmentAndSimulationTools", createTaSkillItems(SKILL_TOOLS));
        taSkillPool.put("engineeringAndTools", tools);
        Map<String, Object> language = new LinkedHashMap<>();
        language.put("crossCulturalCommunication", createTaSkillItems(SKILL_LANGUAGE));
        taSkillPool.put("languageAndCommunication", language);
        skills.put("taSkillPool", taSkillPool);
        return skills;
    }

    private static Map<String, Object> createTaTechnicalSkills() {
        Map<String, Object> technical = new LinkedHashMap<>();
        technical.put("programmingAndSoftwareFundamentals", createTaSkillItems(SKILL_PROGRAMMING));
        technical.put("hardwareAndLogicDesign", createTaSkillItems(SKILL_HARDWARE));
        technical.put("embeddedSystemsAndLowLevelDevelopment", createTaSkillItems(SKILL_EMBEDDED));
        return technical;
    }

    private static List<Map<String, Object>> createTaSkillItems(List<String> names) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String name : names) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("selected", false);
            item.put("proficiency", null);
            out.add(item);
        }
        return out;
    }

    private static Map<String, Object> createMoSkillCatalog() {
        Map<String, Object> catalog = new LinkedHashMap<>();
        catalog.put("proficiencyLevels", PROFICIENCY_LEVELS);
        Map<String, Object> skillCatalog = new LinkedHashMap<>();
        Map<String, Object> technical = new LinkedHashMap<>();
        technical.put("programmingAndSoftwareFundamentals", SKILL_PROGRAMMING);
        technical.put("hardwareAndLogicDesign", SKILL_HARDWARE);
        technical.put("embeddedSystemsAndLowLevelDevelopment", SKILL_EMBEDDED);
        skillCatalog.put("technicalSkills", technical);
        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("professionalDevelopmentAndSimulationTools", SKILL_TOOLS);
        skillCatalog.put("engineeringAndTools", tools);
        Map<String, Object> language = new LinkedHashMap<>();
        language.put("crossCulturalCommunication", SKILL_LANGUAGE);
        skillCatalog.put("languageAndCommunication", language);
        catalog.put("skillCatalog", skillCatalog);
        return catalog;
    }

    private static Map<String, Object> createMoJobPostingSkillTemplate() {
        Map<String, Object> template = new LinkedHashMap<>();
        template.put("proficiencyLevels", PROFICIENCY_LEVELS);
        Map<String, Object> taSkillPool = new LinkedHashMap<>();
        taSkillPool.put("technicalSkills", createMoJobSkillTechnical());
        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("professionalDevelopmentAndSimulationTools", createMoJobSkillItems(SKILL_TOOLS));
        taSkillPool.put("engineeringAndTools", tools);
        Map<String, Object> language = new LinkedHashMap<>();
        language.put("crossCulturalCommunication", createMoJobSkillItems(SKILL_LANGUAGE));
        taSkillPool.put("languageAndCommunication", language);
        template.put("taSkillPool", taSkillPool);
        template.put("customRequiredSkills", List.of());
        return template;
    }

    private static Map<String, Object> createMoJobSkillTechnical() {
        Map<String, Object> technical = new LinkedHashMap<>();
        technical.put("programmingAndSoftwareFundamentals", createMoJobSkillItems(SKILL_PROGRAMMING));
        technical.put("hardwareAndLogicDesign", createMoJobSkillItems(SKILL_HARDWARE));
        technical.put("embeddedSystemsAndLowLevelDevelopment", createMoJobSkillItems(SKILL_EMBEDDED));
        return technical;
    }

    private static List<Map<String, Object>> createMoJobSkillItems(List<String> names) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String name : names) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("selected", false);
            item.put("minimumProficiency", null);
            out.add(item);
        }
        return out;
    }

   
    private static String toFormattedJson(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        appendValue(sb, obj, 0);
        return sb.toString();
    }

    private static void appendValue(StringBuilder sb, Object value, int indent) {
        if (value instanceof JSONObject) {
            appendJsonObject(sb, (JSONObject) value, indent);
        } else if (value instanceof JSONArray) {
            appendJsonArray(sb, (JSONArray) value, indent);
        } else if (value instanceof String) {
            sb.append("\"").append(escapeString((String) value)).append("\"");
        } else if (value instanceof Number) {
            sb.append(value.toString());
        } else if (value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value == null) {
            sb.append("null");
        }
    }

    private static void appendJsonObject(StringBuilder sb, JSONObject obj, int indent) {
        String indentStr = "  ".repeat(indent);
        String childIndent = "  ".repeat(indent + 1);
        sb.append("{\n");

        int size = obj.size();
        int i = 0;
        for (Object key : obj.keySet()) {
            sb.append(childIndent).append("\"").append(escapeString(key.toString())).append("\": ");
            appendValue(sb, obj.get(key), indent + 1);
            if (++i < size) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append(indentStr).append("}");
    }

    private static void appendJsonArray(StringBuilder sb, JSONArray arr, int indent) {
        String childIndent = "  ".repeat(indent + 1);
        sb.append("[\n");

        int size = arr.size();
        for (int i = 0; i < size; i++) {
            sb.append(childIndent);
            appendValue(sb, arr.get(i), indent + 1);
            if (i < size - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ".repeat(indent)).append("]");
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\")
               .replace("\"", "\\\"")
               .replace("\n", "\\n")
               .replace("\r", "\\r")
               .replace("\t", "\\t");
    }

    
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
                String hash = (String) json.get("passwordHash");

                if (hash != null && !hash.isBlank()) {
                    users.add(new User(username, password != null ? password : "", hash, userRole));
                } else {
                    users.add(new User(username, password != null ? password : "", userRole));
                }
            }

        } catch (Exception e) {
            // ignore
        }

        return users;
    }

    
    public static List<User> loadUsersFromRoleSubdir(String roleFolder) {
        List<User> users = new ArrayList<>();
        String dirPath = BASE_PATH + roleFolder.toLowerCase() + File.separator;
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return users;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return users;
        }
        JSONParser parser = new JSONParser();
        for (File f : files) {
            try {
                Object parsed = parser.parse(new FileReader(f));
                if (!(parsed instanceof JSONObject)) {
                    continue;
                }
                JSONObject root = (JSONObject) parsed;
                Object accObj = root.get("account");
                if (!(accObj instanceof JSONObject)) {
                    continue;
                }
                JSONObject account = (JSONObject) accObj;
                String username = (String) account.get("username");
                if (username == null || username.isBlank()) {
                    continue;
                }
                String plain = (String) account.get("password");
                String hash = (String) account.get("passwordHash");
                String roleRaw = (String) root.get("role");
                if (roleRaw == null || roleRaw.isBlank()) {
                    roleRaw = roleFolder;
                }
                String userRole = normalizeRole(roleRaw);
                String systemUserId = (String) root.get("userId");
                if (hash != null && !hash.isBlank()) {
                    users.add(new User(username, plain != null ? plain : "", hash, userRole, systemUserId));
                } else {
                    users.add(new User(username, plain != null ? plain : "", null, userRole, systemUserId));
                }
            } catch (Exception ignored) {
                // skip broken file
            }
        }
        return users;
    }

    private static String normalizeRole(String roleRaw) {
        String r = roleRaw.trim();
        if (r.equalsIgnoreCase("ta")) {
            return "TA";
        }
        if (r.equalsIgnoreCase("mo")) {
            return "MO";
        }
        if (r.equalsIgnoreCase("admin")) {
            return "Admin";
        }
        return r.toUpperCase();
    }

    private static void putUserByUsername(Map<String, User> map, User u) {
        if (u != null && u.getUsername() != null) {
            map.put(u.getUsername(), u);
        }
    }

    
    public static List<User> loadAllUsers() {
        Map<String, User> byName = new LinkedHashMap<>();

        for (User u : loadUsersByRole("TA")) {
            putUserByUsername(byName, u);
        }
        for (User u : loadUsersByRole("MO")) {
            putUserByUsername(byName, u);
        }
        for (User u : loadUsersByRole("Admin")) {
            putUserByUsername(byName, u);
        }
        for (User u : loadUsersFromRoleSubdir("TA")) {
            putUserByUsername(byName, u);
        }
        for (User u : loadUsersFromRoleSubdir("MO")) {
            putUserByUsername(byName, u);
        }
        for (User u : loadUsersFromRoleSubdir("Admin")) {
            putUserByUsername(byName, u);
        }

        return new ArrayList<>(byName.values());
    }

   
    public static String findMoUserIdByAccountUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String dirPath = BASE_PATH + "mo" + File.separator;
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return null;
        }
        JSONParser parser = new JSONParser();
        for (File f : files) {
            try {
                Object parsed = parser.parse(new FileReader(f));
                if (!(parsed instanceof JSONObject)) {
                    continue;
                }
                JSONObject root = (JSONObject) parsed;
                Object accObj = root.get("account");
                if (!(accObj instanceof JSONObject)) {
                    continue;
                }
                JSONObject account = (JSONObject) accObj;
                String u = (String) account.get("username");
                if (username.equals(u)) {
                    String id = (String) root.get("userId");
                    return id != null && !id.isBlank() ? id : null;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // MO 是否首次登录：沿用当前 data/users/mo/*.json 的 profile 判断
    public static boolean isFirstLogin(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return true;
        }
        try {
            String dirPath = BASE_PATH + "mo" + File.separator;
            File dir = new File(dirPath);
            if (!dir.isDirectory()) {
                return true;
            }

            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
            if (files == null || files.length == 0) {
                return true;
            }

            JSONParser parser = new JSONParser();
            for (File file : files) {
                Object parsed = parser.parse(new FileReader(file));
                if (!(parsed instanceof JSONObject)) {
                    continue;
                }
                JSONObject root = (JSONObject) parsed;
                Object accObj = root.get("account");
                if (!(accObj instanceof JSONObject)) {
                    continue;
                }
                JSONObject account = (JSONObject) accObj;
                String username = (String) account.get("username");
                if (!user.getUsername().equals(username)) {
                    continue;
                }

                Object profileObj = root.get("profile");
                if (!(profileObj instanceof JSONObject)) {
                    return true;
                }
                JSONObject profile = (JSONObject) profileObj;
                String fullName = safeString(profile.get("fullName"));
                String staffId = safeString(profile.get("staffId"));
                String department = safeString(profile.get("department"));
                String phone = safeString(profile.get("phoneNumber"));

                // 采用现有结构判断：关键资料为空即视为首次登录
                return fullName.isBlank() || staffId.isBlank() || department.isBlank() || phone.isBlank();
            }
        } catch (Exception ignored) {
            return true;
        }
        return true;
    }

    // MO 首登补全资料：直接更新 data/users/mo/*.json 现有节点
    public static void updateMOProfile(
            User user,
            String fullName,
            String staffId,
            String department,
            String phone,
            String school,
            String email,
            String campus,
            String courseTypes
    ) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return;
        }
        try {
            String dirPath = BASE_PATH + "mo" + File.separator;
            File dir = new File(dirPath);
            if (!dir.isDirectory()) {
                return;
            }

            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
            if (files == null) {
                return;
            }

            JSONParser parser = new JSONParser();
            for (File file : files) {
                Object parsed = parser.parse(new FileReader(file));
                if (!(parsed instanceof JSONObject)) {
                    continue;
                }
                JSONObject root = (JSONObject) parsed;
                Object accObj = root.get("account");
                if (!(accObj instanceof JSONObject)) {
                    continue;
                }
                JSONObject account = (JSONObject) accObj;
                String username = (String) account.get("username");
                if (!user.getUsername().equals(username)) {
                    continue;
                }

                JSONObject profile = root.get("profile") instanceof JSONObject
                        ? (JSONObject) root.get("profile")
                        : new JSONObject();
                profile.put("fullName", safeString(fullName));
                profile.put("staffId", safeString(staffId));
                profile.put("department", safeString(department));
                profile.put("phoneNumber", safeString(phone));

                JSONObject identity = profile.get("identityInformationRequired") instanceof JSONObject
                        ? (JSONObject) profile.get("identityInformationRequired")
                        : new JSONObject();
                identity.put("school", safeString(school));
                identity.put("campusEmail", safeString(email));
                profile.put("identityInformationRequired", identity);

                JSONObject course = profile.get("courseInformationRequired") instanceof JSONObject
                        ? (JSONObject) profile.get("courseInformationRequired")
                        : new JSONObject();
                course.put("teachingCampus", safeString(campus));

                JSONArray courseArray = new JSONArray();
                if (courseTypes != null && !courseTypes.isBlank()) {
                    String[] parts = courseTypes.split(",");
                    for (String p : parts) {
                        String v = p == null ? "" : p.trim();
                        if (!v.isEmpty()) {
                            courseArray.add(v);
                        }
                    }
                }
                course.put("courseTypes", courseArray);
                profile.put("courseInformationRequired", course);

                root.put("profile", profile);

                JSONObject meta = root.get("meta") instanceof JSONObject
                        ? (JSONObject) root.get("meta")
                        : new JSONObject();
                meta.put("updatedAt", java.time.LocalDateTime.now().toString());
                root.put("meta", meta);

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(toFormattedJson(root));
                }
                return;
            }
        } catch (Exception ignored) {
            // keep silent to avoid breaking login flow
        }
    }

    private static String safeString(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
