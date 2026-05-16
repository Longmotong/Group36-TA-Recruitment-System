package Authentication_Module.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {

    private String username;
    private String password;
    
    private String passwordHash;
    private String role; // TA / MO / Admin
   
    private String systemUserId;

    public User(String username, String password, String role) {
        this(username, password, null, role, null);
    }

    public User(String username, String password, String passwordHash, String role) {
        this(username, password, passwordHash, role, null);
    }

    public User(String username, String password, String passwordHash, String role, String systemUserId) {
        this.username = username;
        this.password = password != null ? password : "";
        this.passwordHash = passwordHash;
        this.role = role;
        this.systemUserId = systemUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getSystemUserId() {
        return systemUserId;
    }

    public void setSystemUserId(String systemUserId) {
        this.systemUserId = systemUserId;
    }

    
    public boolean passwordMatches(String plain) {
        if (plain == null) {
            return false;
        }
        if (password != null && !password.isBlank() && plain.equals(password)) {
            return true;
        }
        if (passwordHash != null && !passwordHash.isBlank()) {
            String h = passwordHash.trim();
            if (h.matches("^[a-fA-F0-9]{64}$")) {
                return sha256Hex(plain).equalsIgnoreCase(h);
            }
            return plain.equals(h);
        }
        return plain.equals(password);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
