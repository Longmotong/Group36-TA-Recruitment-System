package TA_Job_Application_Module.pages.applications;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import TA_Job_Application_Module.ui.UI_Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

/**
 * Card-based UI for My Applications / Drafts (purple portal reference layout).
 */
public final class ApplicationsCards {

    private static final Color LABEL_GREY = new Color(107, 114, 128);
    private static final Color VALUE_BLACK = new Color(17, 24, 39);

    private ApplicationsCards() {
    }

    public static JPanel wrapSearchFilterStrip(JComponent searchField, JComponent statusCombo) {
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.VIOLET_200),
                new EmptyBorder(8, 12, 8, 12)));

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        left.add(new JLabel(JobsPortalUi.searchIcon(JobsPortalUi.PURPLE_600, 22)), BorderLayout.WEST);
        left.add(searchField, BorderLayout.CENTER);

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.add(left, BorderLayout.CENTER);
        row.add(statusCombo, BorderLayout.EAST);

        JPanel pad = new JPanel(new BorderLayout());
        pad.setOpaque(false);
        pad.setBorder(new EmptyBorder(4, 4, 4, 4));
        pad.add(row, BorderLayout.CENTER);

        JobsPortalUi.RoundedSurface shell = new JobsPortalUi.RoundedSurface(
                14, Color.WHITE, JobsPortalUi.VIOLET_200, 1f, true, new BorderLayout());
        shell.add(pad, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(shell, BorderLayout.CENTER);
        return wrap;
    }

    /**
     * Summary stat card: single horizontal band — label + icon on the left, large value on the right.
     * Keeps the row short vertically while the parent uses equal-width columns to span the full page width.
     */
    public static JPanel summaryStatTile(Icon glyph, Color cardFill, Color borderColor,
                                         String labelCaps, JLabel valueLabel,
                                         Color labelColor, Color valueColor) {
        JPanel inner = new JPanel(new BorderLayout(14, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel meta = new JPanel(new BorderLayout(8, 0));
        meta.setOpaque(false);
        JLabel lab = new JLabel(labelCaps);
        lab.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lab.setForeground(labelColor);
        JLabel iconLbl = new JLabel(glyph);
        iconLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        meta.add(lab, BorderLayout.WEST);
        meta.add(iconLbl, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        inner.add(meta, BorderLayout.CENTER);
        inner.add(valueLabel, BorderLayout.EAST);

        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                14, cardFill, borderColor, 1f, false, new BorderLayout());
        card.add(inner, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    public static Icon documentStackIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                g2.drawRoundRect(x + 4, y + 5, s - 10, s - 9, 2, 2);
                g2.drawRoundRect(x + 6, y + 3, s - 10, s - 9, 2, 2);
                g2.drawLine(x + 8, y + 10, x + s - 8, y + 10);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static Icon checkCircleIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                g2.drawOval(x + 3, y + 3, s - 6, s - 6);
                g2.drawLine(x + s / 2 - 4, y + s / 2, x + s / 2 - 1, y + s / 2 + 3);
                g2.drawLine(x + s / 2 - 1, y + s / 2 + 3, x + s - 6, y + s / 2 - 4);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static Icon xCircleIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                g2.drawOval(x + 3, y + 3, s - 6, s - 6);
                g2.drawLine(x + 7, y + 7, x + s - 7, y + s - 7);
                g2.drawLine(x + s - 7, y + 7, x + 7, y + s - 7);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static JPanel applicationCard(Application app, String applied, String lastUp,
                                         Runnable onViewDetails, Runnable onWithdrawOrNull) {
        String title = app.getJobSnapshot() != null ? nz(app.getJobSnapshot().getTitle()) : "";
        String course = app.getJobSnapshot() != null ? nz(app.getJobSnapshot().getCourseCode()) : "";
        String dept = app.getJobSnapshot() != null ? nz(app.getJobSnapshot().getDepartment()) : "";
        String statusLabel = app.getStatus() != null ? nz(app.getStatus().getLabel()) : "";

        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                14, Color.WHITE, JobsPortalUi.VIOLET_200, 1f, true, new BorderLayout());
        JPanel pad = new JPanel();
        pad.setLayout(new BoxLayout(pad, BoxLayout.Y_AXIS));
        pad.setOpaque(false);
        pad.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleL = new JLabel(title);
        titleL.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleL.setForeground(JobsPortalUi.PURPLE_600);
        header.add(titleL, BorderLayout.WEST);

        JPanel actionCol = new JPanel();
        actionCol.setLayout(new BoxLayout(actionCol, BoxLayout.Y_AXIS));
        actionCol.setOpaque(false);

        JobsPortalUi.PurpleGradientButton viewBtn =
                JobsPortalUi.gradientButton("View Details", new Font("Segoe UI", Font.BOLD, 13),
                        JobsPortalUi.eyeGlyph(Color.WHITE, 16));
        viewBtn.addActionListener(e -> onViewDetails.run());
        viewBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        actionCol.add(viewBtn);

        if (onWithdrawOrNull != null) {
            actionCol.add(Box.createVerticalStrut(8));
            JobsPortalUi.RedGradientButton wBtn =
                    JobsPortalUi.dangerGradientButton("Withdraw", new Font("Segoe UI", Font.BOLD, 13), null);
            wBtn.addActionListener(e -> onWithdrawOrNull.run());
            wBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
            actionCol.add(wBtn);
            int mw = Math.max(viewBtn.getPreferredSize().width, wBtn.getPreferredSize().width);
            Dimension vd = viewBtn.getPreferredSize();
            Dimension wd = wBtn.getPreferredSize();
            viewBtn.setPreferredSize(new Dimension(mw, vd.height));
            viewBtn.setMaximumSize(new Dimension(mw, vd.height));
            wBtn.setPreferredSize(new Dimension(mw, wd.height));
            wBtn.setMaximumSize(new Dimension(mw, wd.height));
        }

        header.add(actionCol, BorderLayout.EAST);
        pad.add(header);

        pad.add(Box.createVerticalStrut(10));

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.add(metaCellCompact("COURSE", course));
        infoRow.add(columnSep());
        infoRow.add(metaCellCompact("DEPARTMENT", dept));
        infoRow.add(columnSep());
        infoRow.add(metaCellCompact("APPLIED DATE", applied));
        infoRow.add(columnSep());
        infoRow.add(metaCellCompact("LAST UPDATED", lastUp));
        pad.add(infoRow);

        pad.add(Box.createVerticalStrut(10));

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        foot.setOpaque(false);
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.add(statusPill(statusLabel));
        pad.add(foot);

        card.add(pad, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.putClientProperty("application.card", app);
        return wrap;
    }

    public static JPanel draftCard(Application draft, String saved, String lastUp,
                                   Runnable onEdit, Runnable onDelete) {
        String title = draft.getJobSnapshot() != null ? nz(draft.getJobSnapshot().getTitle()) : "";
        String course = draft.getJobSnapshot() != null ? nz(draft.getJobSnapshot().getCourseCode()) : "";
        String dept = draft.getJobSnapshot() != null ? nz(draft.getJobSnapshot().getDepartment()) : "";

        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                14, Color.WHITE, JobsPortalUi.VIOLET_200, 1f, true, new BorderLayout());
        JPanel pad = new JPanel();
        pad.setLayout(new BoxLayout(pad, BoxLayout.Y_AXIS));
        pad.setOpaque(false);
        pad.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JLabel titleL = new JLabel(title);
        titleL.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleL.setForeground(JobsPortalUi.PURPLE_600);
        header.add(titleL, BorderLayout.WEST);

        JPanel actionCol = new JPanel();
        actionCol.setLayout(new BoxLayout(actionCol, BoxLayout.Y_AXIS));
        actionCol.setOpaque(false);
        JobsPortalUi.PurpleGradientButton editBtn =
                JobsPortalUi.gradientButton("Continue", new Font("Segoe UI", Font.BOLD, 13), null);
        editBtn.addActionListener(e -> onEdit.run());
        editBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        actionCol.add(editBtn);
        actionCol.add(Box.createVerticalStrut(8));
        JobsPortalUi.RedGradientButton delBtn =
                JobsPortalUi.dangerGradientButton("Delete", new Font("Segoe UI", Font.BOLD, 13), null);
        delBtn.addActionListener(e -> onDelete.run());
        delBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        actionCol.add(delBtn);
        int mw = Math.max(editBtn.getPreferredSize().width, delBtn.getPreferredSize().width);
        Dimension ed = editBtn.getPreferredSize();
        Dimension dd = delBtn.getPreferredSize();
        editBtn.setPreferredSize(new Dimension(mw, ed.height));
        editBtn.setMaximumSize(new Dimension(mw, ed.height));
        delBtn.setPreferredSize(new Dimension(mw, dd.height));
        delBtn.setMaximumSize(new Dimension(mw, dd.height));
        header.add(actionCol, BorderLayout.EAST);
        pad.add(header);

        pad.add(Box.createVerticalStrut(10));

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoRow.setOpaque(false);
        infoRow.add(metaCellCompact("COURSE", course));
        infoRow.add(columnSep());
        infoRow.add(metaCellCompact("DEPARTMENT", dept));
        infoRow.add(columnSep());
        infoRow.add(metaCellCompact("SAVED DATE", saved));
        infoRow.add(columnSep());
        infoRow.add(metaCellCompact("LAST UPDATED", lastUp));
        pad.add(infoRow);

        pad.add(Box.createVerticalStrut(10));

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        foot.setOpaque(false);
        foot.add(statusPill("Draft"));
        pad.add(foot);

        card.add(pad, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.putClientProperty("draft.card", draft);
        return wrap;
    }

    private static JPanel metaCell(String caps, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(caps);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(LABEL_GREY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel(value.isEmpty() ? "\u2014" : value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));
        v.setForeground(VALUE_BLACK);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(v);
        return p;
    }

    /** Compact meta block for single-row card layout. */
    private static JPanel metaCellCompact(String caps, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(caps);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(LABEL_GREY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel(value.isEmpty() ? "\u2014" : value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(VALUE_BLACK);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(v);
        return p;
    }

    private static JLabel columnSep() {
        JLabel s = new JLabel(" | ");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(new Color(209, 213, 219));
        return s;
    }

    private static JPanel statusPill(String label) {
        String key = label.toLowerCase(Locale.ROOT);
        Color bg;
        Color fg;
        Icon lead = null;
        int iconSz = 18;
        if (key.contains("offer") && key.contains("pending")) {
            bg = UI_Constants.OFFER_PENDING_BADGE_BG;
            fg = UI_Constants.OFFER_PENDING_BADGE_FG;
            lead = checkCircleIcon(fg, iconSz);
        } else if (key.contains("pending")) {
            bg = new Color(254, 249, 195);
            fg = new Color(161, 98, 7);
            lead = plainClockIcon(fg, iconSz);
        } else if (key.contains("under review")) {
            bg = new Color(219, 234, 254);
            fg = new Color(29, 78, 216);
            lead = waveIcon(fg, iconSz);
        } else if (key.contains("accepted")) {
            bg = new Color(209, 250, 229);
            fg = new Color(5, 122, 85);
            lead = checkCircleIcon(fg, iconSz);
        } else if (key.contains("rejected")) {
            bg = new Color(254, 226, 226);
            fg = new Color(185, 28, 28);
            lead = xCircleIcon(fg, iconSz);
        } else if (key.contains("draft")) {
            bg = new Color(237, 233, 254);
            fg = new Color(91, 33, 182);
            lead = documentStackIcon(fg, iconSz);
        } else {
            bg = new Color(243, 244, 246);
            fg = LABEL_GREY;
        }

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(true);
        row.setBackground(bg);
        Color borderCol = new Color(
                Math.max(0, fg.getRed() - 25),
                Math.max(0, fg.getGreen() - 25),
                Math.max(0, fg.getBlue() - 25));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(borderCol.getRed(), borderCol.getGreen(), borderCol.getBlue(), 200), 1),
                new EmptyBorder(8, 14, 8, 14)));
        if (lead != null) {
            row.add(new JLabel(lead));
        }
        JLabel t = new JLabel(label);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(fg);
        row.add(t);
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(row);
        return wrap;
    }

    /** Clock strokes only (no violet circle — matches colored status pill). */
    private static Icon plainClockIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                int cx = x + s / 2;
                int cy = y + s / 2;
                g2.drawOval(x + 2, y + 2, s - 4, s - 4);
                g2.drawLine(cx, cy, cx, y + 5);
                g2.drawLine(cx, cy, x + s - 5, cy);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static Icon waveIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int s = size;
                int mid = y + s / 2;
                g2.drawLine(x + 2, mid, x + s / 3, mid - 3);
                g2.drawLine(x + s / 3, mid - 3, x + 2 * s / 3, mid + 3);
                g2.drawLine(x + 2 * s / 3, mid + 3, x + s - 2, mid);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }
}
