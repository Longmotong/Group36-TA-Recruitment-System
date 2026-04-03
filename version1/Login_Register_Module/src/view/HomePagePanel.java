package view;

import javax.swing.*;
import java.awt.*;

public class HomePagePanel extends JPanel {

    public HomePagePanel(AppFrame app) {

        setLayout(null);
        setBackground(Color.WHITE);

        // ===== Title =====
        JLabel title = new JLabel("Multi-Role Management System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 34));
        title.setBounds(150, 40, 700, 40);
        add(title);

        JLabel sub = new JLabel(
                "Unified platform for TA, MO and Admin management",
                SwingConstants.CENTER
        );
        sub.setBounds(150, 80, 700, 20);
        add(sub);

        // ===== Buttons =====
        JButton login = blackBtn("Login");
        login.setBounds(420, 120, 90, 35);
        login.addActionListener(e -> app.showPage("LOGIN"));

        JButton register = whiteBtn("Register");
        register.setBounds(520, 120, 110, 35);
        register.addActionListener(e -> app.showPage("REGISTER"));

        add(login);
        add(register);

        // ===== Section =====
        JLabel sec = new JLabel("Three Roles, One Platform", SwingConstants.CENTER);
        sec.setFont(new Font("Arial", Font.BOLD, 18));
        sec.setBounds(300, 180, 400, 30);
        add(sec);

        // ===== Cards =====
        add(card("TA Portal", "Teaching Assistant Workspace", 150, false));
        add(card("MO Portal", "Management Officer Center", 400, true));
        add(card("Admin Portal", "System Administration Center", 650, false));

        // ===== Bottom =====
        JPanel bottom = new JPanel(null);
        bottom.setBackground(Color.BLACK);
        bottom.setBounds(150, 480, 700, 120);

        JLabel txt = new JLabel("Ready to Get Started?", SwingConstants.CENTER);
        txt.setForeground(Color.WHITE);
        txt.setFont(new Font("Arial", Font.BOLD, 16));
        txt.setBounds(200, 20, 300, 30);

        JButton btn = whiteBtn("Register for Free");
        btn.setBounds(250, 60, 200, 30);
        btn.addActionListener(e -> app.showPage("REGISTER"));

        bottom.add(txt);
        bottom.add(btn);

        add(bottom);
    }

    private JPanel card(String title, String desc, int x, boolean dark) {
        JPanel p = new JPanel(null);
        p.setBounds(x, 230, 200, 180);

        if (dark) {
            p.setBackground(Color.BLACK);
        } else {
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setBounds(20, 40, 160, 20);
        t.setForeground(dark ? Color.WHITE : Color.BLACK);
        t.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel d = new JLabel(desc, SwingConstants.CENTER);
        d.setBounds(10, 80, 180, 20);
        d.setForeground(dark ? Color.WHITE : Color.BLACK);

        p.add(t);
        p.add(d);

        return p;
    }

    private JButton blackBtn(String t) {
        JButton b = new JButton(t);
        b.setBackground(Color.BLACK);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private JButton whiteBtn(String t) {
        JButton b = new JButton(t);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return b;
    }
}