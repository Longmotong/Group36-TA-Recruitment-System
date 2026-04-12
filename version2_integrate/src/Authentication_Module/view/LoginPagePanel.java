package Authentication_Module.view;

import Authentication_Module.session.SessionManager;
import Authentication_Module.controller.AuthController;
import Authentication_Module.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPagePanel extends JPanel {

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginPagePanel(AppFrame app) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);

        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(30, 40, 30, 40)
        ));
        card.setMaximumSize(new Dimension(AuthUiDimensions.LOGIN_CARD_W, 480));
        card.setPreferredSize(new Dimension(AuthUiDimensions.LOGIN_CARD_W, AuthUiDimensions.LOGIN_CARD_H));

        
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_LOGIN_TITLE));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Login to your account");
        desc.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        desc.setForeground(new Color(120, 120, 120));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.LOGIN_FIELD_H));
        usernameField.setPreferredSize(new Dimension(AuthUiDimensions.LOGIN_FIELD_W, AuthUiDimensions.LOGIN_FIELD_H));
        usernameField.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.LOGIN_FIELD_H));
        passwordField.setPreferredSize(new Dimension(AuthUiDimensions.LOGIN_FIELD_W, AuthUiDimensions.LOGIN_FIELD_H));
        passwordField.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        
        JButton login = new JButton("Login");
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        login.setOpaque(true);
        login.setBackground(Color.BLACK);
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        login.setContentAreaFilled(true);
        login.setBorderPainted(false);
        login.setFont(new Font("Arial", Font.BOLD, AuthUiDimensions.FONT_LOGIN_BTN));
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, AuthUiDimensions.LOGIN_BTN_H));
        login.setPreferredSize(new Dimension(AuthUiDimensions.LOGIN_FIELD_W, AuthUiDimensions.LOGIN_BTN_H));

        AuthController controller = new AuthController();

        login.addActionListener(e -> {
            String username = usernameField.getText().trim();
            char[] pw = passwordField.getPassword();
            String password = new String(pw);
            java.util.Arrays.fill(pw, '\0');
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password");
                return;
            }
            User loggedInUser = controller.handleLogin(username, password);

            if (loggedInUser != null) {
                SessionManager.login(loggedInUser);
                String role = loggedInUser.getRole().toLowerCase();

                if ("admin".equals(role)) {
                    
                    app.showPage("ADMIN");
                } else if ("ta".equals(role)) {
                    app.showPage("TA");
                } else if ("mo".equals(role)) {
                    app.showPage("MO");
                } else {
                    JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        });

        
        JButton back = new JButton("← Back to Home");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setForeground(new Color(100, 100, 100));
        back.setFont(new Font("Arial", Font.PLAIN, AuthUiDimensions.FONT_LOGIN_BODY));
        back.addActionListener(e -> app.showPage("HOME"));

        
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);

        card.add(Box.createVerticalStrut(25));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));
        card.add(passwordField);

        card.add(Box.createVerticalStrut(25));
        card.add(login);

        card.add(Box.createVerticalStrut(15));
        card.add(back);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                if (parent != null) {
                    parent.revalidate();
                    parent.repaint();
                }
            });
        }
    }

    public void clearCredentials() {
        usernameField.setText("");
        char[] pw = passwordField.getPassword();
        java.util.Arrays.fill(pw, '\0');
        passwordField.setText("");
    }
}