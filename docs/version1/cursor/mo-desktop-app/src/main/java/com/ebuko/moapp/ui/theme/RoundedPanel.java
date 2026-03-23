package com.ebuko.moapp.ui.theme;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
  private final int radius;
  private Color borderColor = Theme.CARD_BORDER;
  private int borderWidth = 1;

  public RoundedPanel() {
    this(12);
  }

  public RoundedPanel(int radius) {
    this.radius = radius;
    setOpaque(false);
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
    repaint();
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int w = getWidth();
    int h = getHeight();

    g2.setColor(Theme.CARD_BG);
    g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

    g2.setColor(borderColor);
    g2.setStroke(new BasicStroke(borderWidth));
    g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

    g2.dispose();
    super.paintComponent(g);
  }
}

