package com.taapp.ui.components;

import com.taapp.ui.UI;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;

public class Card extends JPanel {
    public Card() {
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.palette().border(), 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
    }
}

