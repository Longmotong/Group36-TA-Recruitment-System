package ui;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Centers page content with horizontal gutters and max width. */
public final class AppLayout {
    public static final int CONTENT_MAX_W = 1040;
    public static final int GUTTER = 40;

    private AppLayout() {}

    public static JPanel wrapCentered(JPanel content) {
        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, GUTTER, 0, GUTTER);

        content.setMaximumSize(new Dimension(CONTENT_MAX_W, Integer.MAX_VALUE));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        shell.add(content, c);
        return shell;
    }
}
