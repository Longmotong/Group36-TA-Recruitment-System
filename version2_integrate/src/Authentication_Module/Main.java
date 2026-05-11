package Authentication_Module;

import Authentication_Module.view.AppFrame;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
        }

        
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        new AppFrame();
    }
}
