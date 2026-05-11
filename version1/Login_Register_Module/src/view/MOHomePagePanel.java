package view;

import javax.swing.*;
import java.awt.*;

public class MOHomePagePanel extends JPanel {
    public MOHomePagePanel(AppFrame app) {
        setLayout(new BorderLayout());
        add(new JLabel("MO Dashboard", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}