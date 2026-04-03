package com.mojobsystem.ui;

import com.mojobsystem.auth.AuthController;
import com.mojobsystem.auth.SessionManager;

import javax.swing.*;
import java.awt.*;

public class HomePagePanel extends JPanel {
    private AppFrame app;

    public HomePagePanel(AppFrame app) {
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

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        navButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        navButtons.setOpaque(false);
        header.add(navButtons, BorderLayout.EAST);

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);
        centerContent.setBounds(250, 200, 500, 350);
        centerContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeTitle = new JLabel("Welcome to TA Recruitment System");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeTitle.setForeground(new Color(0x0F172A));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeSub = new JLabel("<html><div style='text-align:center;width:450px'>A comprehensive platform for managing TA applications, job postings, and recruitment workflows.</div></html>");
        welcomeSub.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeSub.setForeground(new Color(0x64748B));
        welcomeSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.setPreferredSize(new Dimension(200, 50));
        loginBtn.setBackground(new Color(0x111827));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> app.showPage("LOGIN"));

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 16));
        registerBtn.setPreferredSize(new Dimension(200, 50));
        registerBtn.setBackground(Color.WHITE);
        registerBtn.setForeground(new Color(0x111827));
        registerBtn.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB)));
        registerBtn.setFocusPainted(false);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> app.showPage("REGISTER"));

        centerContent.add(Box.createVerticalGlue());
        centerContent.add(welcomeTitle);
        centerContent.add(Box.createVerticalStrut(20));
        centerContent.add(welcomeSub);
        centerContent.add(Box.createVerticalStrut(40));
        centerContent.add(loginBtn);
        centerContent.add(Box.createVerticalStrut(15));
        centerContent.add(registerBtn);
        centerContent.add(Box.createVerticalGlue());

        add(centerContent);
    }
}
