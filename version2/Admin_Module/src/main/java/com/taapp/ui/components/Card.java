package com.taapp.ui.components;

import com.taapp.ui.UI;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;

public class Card extends JPanel {
    public Card() {
        setOpaque(true);
        setBackground(UI.palette().cardBg());
        setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1));
    }
}
