package com.mojobsystem.auth;

public class User {

    private String username;
    private String password;
    private String role; // TA / MO / Admin
    private String moId;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String username, String password, String role, String moId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.moId = moId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }
}
