package com.taapp.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (Exception ignored) {
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
