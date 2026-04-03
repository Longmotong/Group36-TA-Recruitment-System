package com.taapp.model;

public class CurrentUser {
    private final String userId;
    private final String loginId;
    private final String role;
    private final String fullName;
    private final String email;

    public CurrentUser(String userId, String loginId, String role, String fullName, String email) {
        this.userId = userId;
        this.loginId = loginId;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getLoginId() { return loginId; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}

