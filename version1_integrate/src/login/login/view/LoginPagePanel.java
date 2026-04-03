package login.view;

import login.AuthController;

import javax.swing.*;
import java.awt.*;

public class LoginPagePanel extends JPanel {

    public LoginPagePanel(AppFrame app) {

        setLayout(null);
        setBackground(Color.WHITE);

        JPanel card = new JPanel(null);
        card.setBounds(350, 200, 300, 340);
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(50, 20, 200, 25);

        JLabel desc = new JLabel("Login to your account", SwingConstants.CENTER);
        desc.setBounds(50, 45, 200, 20);

        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(50, 80, 200, 20);

        JTextField user = new JTextField();
        user.setBounds(50, 100, 200, 25);

        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(50, 135, 200, 20);

        JPasswordField pass = new JPasswordField();
        pass.setBounds(50, 155, 200, 25);

        JButton login = new JButton("Login");
        login.setBounds(50, 200, 200, 30);
        login.setBackground(Color.BLACK);
        login.setForeground(Color.WHITE);

        AuthController controller = new AuthController();

        login.addActionListener(e -> {
            boolean ok = controller.handleLogin(user.getText(), new String(pass.getPassword()));

            if (ok) {
                String role = controller.getUserRole(user.getText());
                if (role == null) {
                    JOptionPane.showMessageDialog(this, "User has no role assigned.");
                    return;
                }
                switch (role.toLowerCase()) {
                    case "ta" -> app.showTaDashboard();
                    case "mo" -> app.showMoDashboard();
                    case "admin" -> app.showAdminDashboard();
                    default -> JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        });

        JButton back = new JButton("Back");
        back.setBounds(50, 240, 200, 25);
        back.addActionListener(e -> app.showPage("HOME"));

        card.add(title);
        card.add(desc);
        card.add(userLabel);
        card.add(user);
        card.add(passLabel);
        card.add(pass);
        card.add(login);
        card.add(back);

        add(card);
    }
}
