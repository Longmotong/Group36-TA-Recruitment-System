package com.mojobsystem;

import com.formdev.flatlaf.FlatLightLaf;
import com.mojobsystem.ui.MoDashboardFrame;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import java.util.Locale;

public class App {
    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        JComponent.setDefaultLocale(Locale.ENGLISH);
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
            // Falls back to default LAF if FlatLaf init fails.
        }
        configureEnglishUiText();

        SwingUtilities.invokeLater(() -> {
            ToolTipManager.sharedInstance().setInitialDelay(150);
            ToolTipManager.sharedInstance().setReshowDelay(0);
            ToolTipManager.sharedInstance().setDismissDelay(8000);
            MoDashboardFrame frame = new MoDashboardFrame();
            frame.setVisible(true);
        });
    }

    private static void configureEnglishUiText() {
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        UIManager.put("FileChooser.openButtonText", "Open");
        UIManager.put("FileChooser.saveButtonText", "Save");
        UIManager.put("FileChooser.cancelButtonText", "Cancel");
        UIManager.put("FileChooser.lookInLabelText", "Look in");
        UIManager.put("FileChooser.saveInLabelText", "Save in");
        UIManager.put("FileChooser.fileNameLabelText", "File name");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Files of type");
        UIManager.put("FileChooser.upFolderToolTipText", "Up One Level");
        UIManager.put("FileChooser.homeFolderToolTipText", "Home");
        UIManager.put("FileChooser.newFolderToolTipText", "Create New Folder");
    }
}
