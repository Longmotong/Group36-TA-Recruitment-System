package com.mojobsystem.ui;

import com.mojobsystem.MoContext;
import com.mojobsystem.auth.SessionManager;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {
    private CardLayout layout = new CardLayout();
    private JPanel container = new JPanel(layout);
    private IntegratedDashboardFrame moDashboard;

    public AppFrame() {
        setTitle("TA Recruitment System");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        container.add(new HomePagePanel(this), "HOME");
        container.add(new LoginPagePanel(this), "LOGIN");
        container.add(new RegisterPagePanel(this), "REGISTER");

        add(container);
        layout.show(container, "HOME");

        setVisible(true);
    }

    public void showPage(String name) {
        layout.show(container, name);
    }

    public void showMoDashboard() {
        dispose();
        MoContext.initFromSession();
        moDashboard = new IntegratedDashboardFrame();
        moDashboard.setVisible(true);
    }
}
