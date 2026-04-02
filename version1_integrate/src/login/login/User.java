package login;

public class User {

    private String username;
    private String password;
    private String role;
    private String moId;
    private String userId;
    private String fullName;
    private String department;
    private String email;

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

    public User(String username, String password, String role, String moId, String userId, String fullName, String department, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.moId = moId;
        this.userId = userId;
        this.fullName = fullName;
        this.department = department;
        this.email = email;
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

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }
}
