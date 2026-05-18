package com.taapp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    
    public static boolean showConfirm(Component parent, String title, String message, String confirmText, String cancelText) {
        JLabel label = new JLabel("<html><body style='font-family:sans-serif;font-size:14px;width:300px'>"
                + escapeHtml(message).replace("\n", "<br/>") + "</body></html>");
        label.setFont(UI.moFontPlain(14));

        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.add(label, BorderLayout.CENTER);

        JButton confirmBtn = new JButton("OK");
        confirmBtn.setFont(UI.moFontPlain(13));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setPreferredSize(new Dimension(80, 32));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(UI.moFontPlain(13));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setPreferredSize(new Dimension(80, 32));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        final boolean[] result = {false};

        confirmBtn.addActionListener(e -> {
            result[0] = true;
            SwingUtilities.getWindowAncestor(panel).dispose();
        });

        cancelBtn.addActionListener(e -> {
            result[0] = false;
            SwingUtilities.getWindowAncestor(panel).dispose();
        });

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION);
        JDialog dialog = optionPane.createDialog(parent, title);
        dialog.setResizable(false);
        dialog.setVisible(true);

        return result[0];
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
