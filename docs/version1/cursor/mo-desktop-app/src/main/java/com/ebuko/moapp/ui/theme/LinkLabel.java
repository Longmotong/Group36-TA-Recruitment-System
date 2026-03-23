package com.ebuko.moapp.ui.theme;

import javax.swing.*;
import java.awt.*;

public class LinkLabel extends JLabel {
  public LinkLabel(String text) {
    super(text);
    setForeground(Theme.LINK);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }
}

