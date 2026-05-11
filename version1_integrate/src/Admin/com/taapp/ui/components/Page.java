package com.taapp.ui.components;

import com.taapp.ui.UI;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

public class Page extends JPanel {
    private final JPanel content = new JPanel();

    public Page() {
        setLayout(new BorderLayout());
        setBackground(UI.palette().appBg());
        setOpaque(true);

        content.setOpaque(false);
        content.setLayout(new BorderLayout());
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        add(content, BorderLayout.CENTER);
    }

    public JPanel content() {
        return content;
    }
}

