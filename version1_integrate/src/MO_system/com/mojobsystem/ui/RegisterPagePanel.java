package com.mojobsystem.ui;

import com.mojobsystem.auth.AuthController;

import javax.swing.*;
import java.awt.*;

public class RegisterPagePanel extends JPanel {
    private AppFrame app;
    private AuthController controller = new AuthController();
    private JComboBox<String> roleCombo;

    public RegisterPagePanel(AppFrame app) {
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
        card.setBounds(350, 150, 300, 450);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        JLabel registerTitle = new JLabel("Create Account");
        registerTitle.setFont(new Font("Arial", Font.BOLD, 22));
        registerTitle.setBounds(0, 0, 240, 30);
        registerTitle.setForeground(new Color(0x0F172A));

        JLabel registerDesc = new JLabel("Join the TA Recruitment System");
        registerDesc.setBounds(0, 35, 240, 20);
        registerDesc.setForeground(new Color(0x64748B));

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

        JLabel confirmPassLabel = new JLabel("Confirm Password");
        confirmPassLabel.setBounds(0, 220, 240, 20);
        confirmPassLabel.setForeground(new Color(0x374151));

        JPasswordField confirmPassField = new JPasswordField();
        confirmPassField.setBounds(0, 242, 240, 40);
        confirmPassField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        confirmPassField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel roleLabel = new JLabel("Role");
        roleLabel.setBounds(0, 295, 240, 20);
        roleLabel.setForeground(new Color(0x374151));

        String[] roles = {"mo", "ta", "admin"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setBounds(0, 317, 240, 40);
        roleCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(0, 380, 240, 45);
        registerBtn.setBackground(new Color(0x111827));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Arial", Font.BOLD, 16));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (controller.handleRegister(username, password, role)) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                app.showPage("LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(registerTitle);
        card.add(registerDesc);
        card.add(userLabel);
        card.add(userField);
        card.add(passLabel);
        card.add(passField);
        card.add(confirmPassLabel);
        card.add(confirmPassField);
        card.add(roleLabel);
        card.add(roleCombo);
        card.add(registerBtn);

        add(card);
    }
}
