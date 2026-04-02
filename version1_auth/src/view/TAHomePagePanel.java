package view;

import javax.swing.*;
import java.awt.*;

public class TAHomePagePanel extends JPanel {
    public TAHomePagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        add(new JLabel("TA Dashboard", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}