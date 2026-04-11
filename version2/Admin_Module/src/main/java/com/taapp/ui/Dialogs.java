package com.taapp.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;

public final class Dialogs {
    private Dialogs() {}

    public static void showMessage(Component parent, String title, String message, int horizontalAlignment) {
        JLabel label = new JLabel("<html><body style='font-family:sans-serif;font-size:14px'>"
                + escapeHtml(message).replace("\n", "<br/>") + "</body></html>");
        label.setFont(UI.moFontPlain(14));
        label.setHorizontalAlignment(horizontalAlignment);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showScrollable(Component parent, String title, JComponent content, int width, int height) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setPreferredSize(new java.awt.Dimension(width, height));
        JOptionPane.showMessageDialog(parent, sp, title, JOptionPane.PLAIN_MESSAGE);
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
