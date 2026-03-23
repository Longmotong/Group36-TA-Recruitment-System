package com.ebuko.moapp;

import com.ebuko.moapp.ui.MoAppFrame;
import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        // Use Nimbus if available for a slightly nicer default look.
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      } catch (Exception ignored) {
        // Fallback to platform default look and feel.
      }
      MoAppFrame frame = new MoAppFrame();
      frame.setVisible(true);
    });
  }
}

