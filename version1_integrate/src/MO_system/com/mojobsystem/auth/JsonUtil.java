package com.mojobsystem.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import login.AppDataRoot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper prettyMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static File getRoleFile(String role) {
        return AppDataRoot.asPath().resolve("users").resolve(role.toLowerCase() + ".json").toFile();
    }

    public static void saveUser(User user) {
        List<User> users = loadUsersByRole(user.getRole());
        users.add(user);

        ArrayNode jsonArray = prettyMapper.createArrayNode();

        for (User u : users) {
            ObjectNode obj = prettyMapper.createObjectNode();
            obj.put("username", u.getUsername());
            obj.put("password", u.getPassword());
            obj.put("role", u.getRole());
            if (u.getMoId() != null) {
                obj.put("moId", u.getMoId());
            }
            jsonArray.add(obj);
        }

        try {
            File file = getRoleFile(user.getRole());
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            prettyMapper.writeValue(file, jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> loadUsersByRole(String role) {
        List<User> users = new ArrayList<>();

        try {
            File file = getRoleFile(role);

            if (!file.exists()) {
                return users;
            }

            ArrayNode jsonArray = (ArrayNode) mapper.readTree(file);

            for (int i = 0; i < jsonArray.size(); i++) {
                ObjectNode json = (ObjectNode) jsonArray.get(i);

                String username = json.get("username").asText();
                String password = json.get("password").asText();
                String userRole = json.get("role").asText();
                String moId = json.has("moId") ? json.get("moId").asText() : null;

                users.add(new User(username, password, userRole, moId));
            }

        } catch (IOException e) {
        }

        return users;
    }

    public static List<User> loadAllUsers() {
        List<User> allUsers = new ArrayList<>();

        allUsers.addAll(loadUsersByRole("mo"));
        allUsers.addAll(loadUsersByRole("ta"));
        allUsers.addAll(loadUsersByRole("admin"));

        return allUsers;
    }
}
