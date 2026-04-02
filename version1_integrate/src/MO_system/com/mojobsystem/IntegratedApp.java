package com.mojobsystem;

import com.formdev.flatlaf.FlatLightLaf;
import login.view.AppFrame;

import javax.swing.*;

public class IntegratedApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
        }

        java.awt.EventQueue.invokeLater(() -> {
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}
