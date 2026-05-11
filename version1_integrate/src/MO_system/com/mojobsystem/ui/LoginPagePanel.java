package com.mojobsystem.ui;

import com.mojobsystem.auth.AuthController;

import javax.swing.*;
import java.awt.*;

public class LoginPagePanel extends JPanel {
    private AppFrame app;
    private AuthController controller = new AuthController();

    public LoginPagePanel(AppFrame app) {
        this.app = app;
        setLayout(null);
        setBackground(new Color(0xF1F5F9));

        JPanel header = new JPanel(new BorderLayout());
        header.setBounds(0, 0, 1000, 80);
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));
        add(header);

        JLabel title = new JLabel("TA Recruitment System");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(0x0F172A));
        title.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        header.add(title, BorderLayout.WEST);

        JButton backBtn = new JButton("< Back");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.setForeground(new Color(0x64748B));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> app.showPage("HOME"));
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        navButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        navButtons.setOpaque(false);
        navButtons.add(backBtn);
        header.add(navButtons, BorderLayout.EAST);

        JPanel card = new JPanel(null);
        card.setBounds(350, 200, 300, 340);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(new Font("Arial", Font.BOLD, 22));
        loginTitle.setBounds(0, 0, 240, 30);
        loginTitle.setForeground(new Color(0x0F172A));

        JLabel loginDesc = new JLabel("Login to your account");
        loginDesc.setBounds(0, 35, 240, 20);
        loginDesc.setForeground(new Color(0x64748B));

        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(0, 70, 240, 20);
        userLabel.setForeground(new Color(0x374151));

        JTextField userField = new JTextField();
        userField.setBounds(0, 92, 240, 40);
        userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        userField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(0, 145, 240, 20);
        passLabel.setForeground(new Color(0x374151));

        JPasswordField passField = new JPasswordField();
        passField.setBounds(0, 167, 240, 40);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        passField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(0, 230, 240, 45);
        loginBtn.setBackground(new Color(0x111827));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            if (controller.handleLogin(username, password)) {
                String role = controller.getUserRole(username);
                if ("mo".equalsIgnoreCase(role)) {
                    app.showMoDashboard();
                } else if ("ta".equalsIgnoreCase(role)) {
                    app.showPage("TA");
                } else if ("admin".equalsIgnoreCase(role)) {
                    app.showPage("ADMIN");
                } else {
                    app.showPage("HOME");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(loginTitle);
        card.add(loginDesc);
        card.add(userLabel);
        card.add(userField);
        card.add(passLabel);
        card.add(passField);
        card.add(loginBtn);

        add(card);
    }
}
