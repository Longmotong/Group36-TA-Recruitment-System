package com.taapp.ui.components;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class Page extends JPanel {
    private final JPanel content = new JPanel(new BorderLayout());

    public Page() {
        setOpaque(false);
        setLayout(new BorderLayout());
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    protected JPanel content() {
        return content;
    }
}
