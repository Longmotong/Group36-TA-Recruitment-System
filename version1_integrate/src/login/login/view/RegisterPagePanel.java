package login.view;

import login.AuthController;

import javax.swing.*;
import java.awt.*;

public class RegisterPagePanel extends JPanel {

    public RegisterPagePanel(AppFrame app) {

        setLayout(null);
        setBackground(Color.WHITE);

        JPanel card = new JPanel(null);
        card.setBounds(350, 160, 300, 420);
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(50, 20, 200, 25);

        JLabel desc = new JLabel("Register to get started", SwingConstants.CENTER);
        desc.setBounds(50, 45, 200, 20);

        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(50, 80, 200, 20);

        JTextField user = new JTextField();
        user.setBounds(50, 100, 200, 25);

        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(50, 135, 200, 20);

        JPasswordField pass = new JPasswordField();
        pass.setBounds(50, 155, 200, 25);

        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setBounds(50, 190, 200, 20);

        JPasswordField confirm = new JPasswordField();
        confirm.setBounds(50, 210, 200, 25);

        JLabel roleLabel = new JLabel("Role (TA / MO / Admin)");
        roleLabel.setBounds(50, 245, 200, 20);

        JComboBox<String> role = new JComboBox<>(new String[]{"ta", "mo", "admin"});
        role.setBounds(50, 265, 200, 25);

        JButton reg = new JButton("Register");
        reg.setBounds(50, 310, 200, 30);
        reg.setBackground(Color.BLACK);
        reg.setForeground(Color.WHITE);

        AuthController controller = new AuthController();

        reg.addActionListener(e -> {
            String username = user.getText().trim();
            String password = new String(pass.getPassword());
            String confirmPassword = new String(confirm.getPassword());
            String roleStr = (String) role.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in username and password.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            if (controller.handleRegister(username, password, roleStr)) {
                JOptionPane.showMessageDialog(this, "Registration successful. Signing you in…");
                if (controller.handleLogin(username, password)) {
                    String r = controller.getUserRole(username);
                    if (r == null) {
                        app.showPage("LOGIN");
                        return;
                    }
                    switch (r.toLowerCase()) {
                        case "ta" -> app.showTaDashboard();
                        case "mo" -> app.showMoDashboard();
                        case "admin" -> app.showAdminDashboard();
                        default -> app.showPage("LOGIN");
                    }
                } else {
                    app.showPage("LOGIN");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed (username may already exist).");
            }
        });

        JButton back = new JButton("Back");
        back.setBounds(50, 350, 200, 25);
        back.addActionListener(e -> app.showPage("HOME"));

        card.add(title);
        card.add(desc);
        card.add(userLabel);
        card.add(user);
        card.add(passLabel);
        card.add(pass);
        card.add(confirmLabel);
        card.add(confirm);
        card.add(roleLabel);
        card.add(role);
        card.add(reg);
        card.add(back);

        add(card);
    }
}
