package view;

import javax.swing.*;
import java.awt.*;

public class AdminHomePagePanel extends JPanel {
    public AdminHomePagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        add(new JLabel("Admin Dashboard", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}