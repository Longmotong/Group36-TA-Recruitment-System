package TA_Job_Application_Module.pages.jobs;

import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.service.DataService;
import TA_Job_Application_Module.ui.UI_Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * Position details view — purple portal layout matching the TA portal reference (two-column + summary rail).
 */
public class Page_JobDetail {

    public interface JobDetailCallback {
        void onBackToJobs();

        void onApply(Job job);
    }

    private static final int CARD_ARC = 16;
    /** Left edge of card content: smaller for flush-left “顶格” layout. */
    private static final int CARD_PAD_LEFT = 14;
    private static final int CARD_PAD_TOP = 18;
    private static final int CARD_PAD_RIGHT = 18;
    private static final int CARD_PAD_BOTTOM = 18;
    private static final int CARD_GAP = 18;
    private static final int SUMMARY_WIDTH = 312;
    /** Max width for Apply / Applied pill in sidebar (narrower than full rail). */
    private static final int SUMMARY_APPLY_BTN_MAX_W = 228;
    private static final int MAIN_SUMMARY_GAP = 28;
    private static final int PURPLE_BAR = 4;
    private static final Color TITLE_BLACK = new Color(17, 16, 51);
    private static final Color BODY_GRAY = new Color(55, 65, 81);
    private static final Color SOFT_CARD_BG = Color.WHITE;
    private static final Color SOFT_BORDER = JobsPortalUi.LIGHT_PURPLE_BORDER;
    private static final Color PRIMARY_PURPLE = JobsPortalUi.PRIMARY_PURPLE;
    private static final Color DEEP_PURPLE = JobsPortalUi.DEEP_PURPLE;
    private static final Color MUTED_TEXT = JobsPortalUi.MUTED_TEXT;

    private JPanel panel;
    private final JobDetailCallback callback;
    private final DataService dataService;

    public Page_JobDetail(DataService dataService, JobDetailCallback callback) {
        this.dataService = dataService;
        this.callback = callback;
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void showJob(Job job) {
        panel.removeAll();
        buildContent(job);
        panel.revalidate();
        panel.repaint();
    }

    private void initPanel() {
        panel = new JobDetailRootPanel();
    }

    private void buildContent(Job job) {
        boolean hasApplied = dataService != null && dataService.hasAppliedToJob(job.getJobId());

        JButton backBtn = new JButton("\u2190  Back to Job Listings");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(PRIMARY_PURPLE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setBorder(new EmptyBorder(0, 0, 12, 0));
        backBtn.addActionListener(e -> callback.onBackToJobs());

        JLabel pageTitle = new JLabel("Position Details");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        pageTitle.setForeground(TITLE_BLACK);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageTitle.setBorder(new EmptyBorder(4, 0, 6, 0));

        JPanel titleUnderline = new JPanel();
        titleUnderline.setOpaque(true);
        titleUnderline.setBackground(PRIMARY_PURPLE);
        Dimension underlineSize = new Dimension(70, 4);
        titleUnderline.setPreferredSize(underlineSize);
        titleUnderline.setMinimumSize(underlineSize);
        titleUnderline.setMaximumSize(underlineSize);
        titleUnderline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        northStack.add(backBtn);
        northStack.add(pageTitle);
        northStack.add(titleUnderline);
        northStack.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCol.setMinimumSize(new Dimension(0, 0));

        leftCol.add(wrapHeaderWithAppliedStrip(hasApplied, buildHeaderCard(job)));
        leftCol.add(Box.createVerticalStrut(CARD_GAP));
        leftCol.add(buildDescriptionCard(job));
        leftCol.add(Box.createVerticalStrut(CARD_GAP));
        leftCol.add(buildBulletSectionCardPortal("Key Responsibilities", job.getResponsibilities()));
        if (job.getRequirements() != null && !job.getRequirements().isEmpty()) {
            leftCol.add(Box.createVerticalStrut(CARD_GAP));
            leftCol.add(buildBulletSectionCardPortal("Requirements", job.getRequirements()));
        }
        if (job.getPreferredSkills() != null && !job.getPreferredSkills().isEmpty()) {
            leftCol.add(Box.createVerticalStrut(CARD_GAP));
            leftCol.add(buildSkillsCardPortal(job.getPreferredSkills()));
        }

        JPanel summaryCard = buildSummaryRail(job, hasApplied);

        JPanel contentRow = new JPanel(new BorderLayout(MAIN_SUMMARY_GAP, 0));
        contentRow.setOpaque(false);
        contentRow.add(leftCol, BorderLayout.CENTER);
        contentRow.add(summaryCard, BorderLayout.EAST);

        panel.setLayout(new BorderLayout(0, 0));
        panel.add(northStack, BorderLayout.NORTH);
        panel.add(contentRow, BorderLayout.CENTER);
    }

    /** Optional green strip + header card (applied indicator). */
    private JPanel wrapHeaderWithAppliedStrip(boolean hasApplied, JPanel headerCard) {
        if (!hasApplied) {
            return headerCard;
        }
        JPanel shell = new JPanel(new BorderLayout(0, 0));
        shell.setOpaque(false);
        JPanel strip = new JPanel();
        strip.setOpaque(true);
        strip.setBackground(new Color(34, 197, 94));
        strip.setPreferredSize(new Dimension(6, 0));
        shell.add(strip, BorderLayout.WEST);
        shell.add(headerCard, BorderLayout.CENTER);
        shell.setAlignmentX(Component.LEFT_ALIGNMENT);
        return shell;
    }

    private JPanel buildHeaderCard(Job job) {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        /** Default JPanel alignmentX is 0.5 — would center every row in the card (title looks “偏右”). */
        inner.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.setBorder(new EmptyBorder(CARD_PAD_TOP + 4, CARD_PAD_LEFT + 8, CARD_PAD_BOTTOM + 4, CARD_PAD_RIGHT + 8));

        boolean hasApplied = dataService != null && dataService.hasAppliedToJob(job.getJobId());
        if (hasApplied) {
            JLabel appliedBadge = new JLabel("Applied");
            appliedBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            appliedBadge.setForeground(new Color(22, 163, 74));
            appliedBadge.setOpaque(true);
            appliedBadge.setBackground(new Color(220, 252, 231));
            appliedBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
            appliedBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
            inner.add(appliedBadge);
            inner.add(Box.createVerticalStrut(12));
        }

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent statusBadge = createOpenStatusBadge(job.getStatus());
        Dimension bdPref = statusBadge.getPreferredSize();
        statusBadge.setMinimumSize(new Dimension(Math.max(72, bdPref.width), bdPref.height));
        int badgeGap = 14;
        JLabel title = new JLabel();
        title.setVerticalAlignment(SwingConstants.TOP);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel titleHost = new JPanel(new BorderLayout(0, 0));
        titleHost.setOpaque(false);
        titleHost.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleHost.add(title, BorderLayout.WEST);
        Runnable refreshTitle = () -> refreshJobTitleHtml(title, job, top, statusBadge, badgeGap);
        top.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshTitle.run();
            }
        });

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.FIRST_LINE_START;
        gc.insets = new Insets(0, 0, 0, 0);
        top.add(titleHost, gc);
        gc.gridx = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.FIRST_LINE_END;
        gc.insets = new Insets(0, badgeGap, 0, 0);
        top.add(statusBadge, gc);

        inner.add(top);
        SwingUtilities.invokeLater(refreshTitle);
        SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(refreshTitle));

        inner.add(Box.createVerticalStrut(20));

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 28, 0));
        twoCol.setOpaque(false);
        twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCol.add(buildMetaBlock(
                circleGlyphIcon(new Color(237, 233, 254), JobsPortalUi.PURPLE_600, 28, Page_JobDetail::paintBuildingGlyph),
                "COURSE · DEPARTMENT",
                courseDisplayLine(job)));
        twoCol.add(buildMetaBlock(
                circleGlyphIcon(new Color(219, 234, 254), new Color(37, 99, 235), 28, Page_JobDetail::paintPersonGlyph),
                "INSTRUCTOR",
                nz(job.getInstructorName())));
        inner.add(twoCol);

        return wrapDetailCard(inner);
    }

    private static void paintBuildingGlyph(Graphics2D g2, int ix, int iy, int s) {
        int w = s - 14;
        int x = ix + 7;
        int y = iy + 8;
        g2.fillRect(x, y + 4, w, s - 14);
        Polygon roof = new Polygon();
        roof.addPoint(ix + s / 2, iy + 5);
        roof.addPoint(ix + s - 5, iy + 11);
        roof.addPoint(ix + 5, iy + 11);
        g2.fillPolygon(roof);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.fillRect(x + 3, y + 8, 4, 4);
        g2.fillRect(x + w - 7, y + 8, 4, 4);
    }

    /** Head + shoulders centered in the s×s icon cell (avoids clipping at bottom of circle). */
    private static void paintPersonGlyph(Graphics2D g2, int ix, int iy, int s) {
        float cx = ix + s / 2f;
        float cy = iy + s / 2f;
        int head = Math.max(4, Math.round(s * 0.32f));
        int hx = Math.round(cx - head / 2f);
        int hy = Math.round(cy - s * 0.26f - head / 2f);
        g2.drawOval(hx, hy, head, head);
        int bodyW = Math.round(s * 0.55f);
        int bodyH = Math.round(s * 0.30f);
        int bx = Math.round(cx - bodyW / 2f);
        int by = Math.round(cy - s * 0.04f);
        g2.drawArc(bx, by, bodyW, bodyH, 200, 140);
    }

    /**
     * Circle backdrop + ink strokes via {@code strokePainter} receiving (g2, ix, iy, size).
     */
    private static Icon circleGlyphIcon(Color circleBg, Color ink, int size,
                                        QuadGlyphPainter strokePainter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(circleBg);
                g2.fillOval(x + 1, y + 1, size - 2, size - 2);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.45f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                strokePainter.paint(g2, x, y, size);
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

    @FunctionalInterface
    private interface QuadGlyphPainter {
        void paint(Graphics2D g2, int ix, int iy, int size);
    }

    private JPanel buildMetaBlock(Icon icon, String capsLabel, String value) {
        JPanel block = new JPanel(new BorderLayout(14, 0));
        block.setOpaque(false);

        JPanel iconTile = JobsPortalUi.wrapRoundedInner(new JLabel(icon), 14, JobsPortalUi.LAVENDER,
                null, 0f, false, new Insets(8, 8, 8, 8));
        iconTile.setPreferredSize(new Dimension(48, 48));
        iconTile.setMinimumSize(new Dimension(48, 48));
        iconTile.setMaximumSize(new Dimension(48, 48));
        block.add(iconTile, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel l = new JLabel(capsLabel);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea v = createWrappingTextArea(value == null ? "" : value,
                new Font("Segoe UI", Font.BOLD, 15), TITLE_BLACK);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(l);
        text.add(Box.createVerticalStrut(3));
        text.add(v);
        block.add(text, BorderLayout.CENTER);
        return block;
    }

    private static void refreshJobTitleHtml(JLabel title, Job job, JPanel topRow, JComponent badge, int badgeGap) {
        int rowW = topRow.getWidth();
        if (rowW <= 0) {
            return;
        }
        int badgeW = Math.max(badge.getPreferredSize().width, badge.getMinimumSize().width);
        int textW = rowW - badgeW - badgeGap;
        if (textW < 48) {
            textW = 48;
        }
        String esc = escapeHtmlLite(job.getTitle() != null ? job.getTitle() : "");
        title.setText(String.format(
                "<html><body style='margin:0;padding:0'>"
                        + "<div style='width:%dpx;text-align:left;font-family:Segoe UI;font-size:22px;font-weight:bold;color:rgb(%d,%d,%d)'>%s</div>"
                        + "</body></html>",
                textW, TITLE_BLACK.getRed(), TITLE_BLACK.getGreen(), TITLE_BLACK.getBlue(), esc));
    }

    private static String escapeHtmlLite(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JComponent createOpenStatusBadge(String rawStatus) {
        String s = rawStatus == null || rawStatus.isEmpty() ? "open" : rawStatus.trim();
        String label = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        boolean open = "open".equalsIgnoreCase(s);
        Color bg = open ? new Color(220, 252, 231) : new Color(243, 244, 246);
        Color fg = open ? new Color(21, 128, 61) : UI_Constants.TEXT_SECONDARY;
        Color border = open ? new Color(167, 243, 208) : UI_Constants.BORDER_COLOR;

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        if (open) {
            row.add(new JLabel(openCheckIcon(fg, 16)));
        }
        JLabel t = new JLabel(label);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(fg);
        row.add(t);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(row, BorderLayout.WEST);
        return JobsPortalUi.wrapRoundedInner(wrap, 10, bg, border, 1f, false, new Insets(6, 12, 6, 12));
    }

    private static Icon openCheckIcon(Color ink, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 3, y + size / 2, x + size / 2 - 1, y + size - 4);
                g2.drawLine(x + size / 2 - 1, y + size - 4, x + size - 3, y + 3);
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

    private JPanel buildDescriptionCard(Job job) {
        String text = job.getDescription() == null ? "" : job.getDescription();
        JTextArea desc = createWrappingTextArea(text.isEmpty() ? "\u2014" : text,
                new Font("Segoe UI", Font.PLAIN, 14), BODY_GRAY);
        JPanel padded = new JPanel(new BorderLayout(0, 12));
        padded.setOpaque(false);
        padded.setBorder(new EmptyBorder(CARD_PAD_TOP, CARD_PAD_LEFT, CARD_PAD_BOTTOM, CARD_PAD_RIGHT));
        padded.add(portalSectionTitle("Position Description"), BorderLayout.NORTH);
        padded.add(desc, BorderLayout.CENTER);
        return wrapDetailCard(padded);
    }

    /** Purple icon tile + accent bar + title on one row, left-aligned. */
    private JPanel portalSectionTitle(String title) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel iconTile = JobsPortalUi.wrapRoundedInner(new JLabel(sectionIcon(title, PRIMARY_PURPLE, 20)),
                12, JobsPortalUi.LAVENDER, null, 0f, false, new Insets(7, 7, 7, 7));
        iconTile.setPreferredSize(new Dimension(38, 38));
        iconTile.setMinimumSize(new Dimension(38, 38));
        iconTile.setMaximumSize(new Dimension(38, 38));
        iconTile.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel bar = new JPanel();
        bar.setOpaque(true);
        bar.setBackground(PRIMARY_PURPLE);
        Dimension barSz = new Dimension(PURPLE_BAR, 26);
        bar.setPreferredSize(barSz);
        bar.setMinimumSize(barSz);
        bar.setMaximumSize(new Dimension(PURPLE_BAR, 28));
        bar.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel lab = new JLabel(title);
        lab.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lab.setForeground(TITLE_BLACK);
        lab.setAlignmentY(Component.CENTER_ALIGNMENT);

        row.add(iconTile);
        row.add(Box.createHorizontalStrut(12));
        row.add(bar);
        row.add(Box.createHorizontalStrut(10));
        row.add(lab);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private static Icon sectionIcon(String title, Color ink, int size) {
        String t = title == null ? "" : title.toLowerCase();
        if (t.contains("respons")) {
            return simpleLineIcon(ink, size, (g2, s) -> {
                g2.drawRoundRect(4, 4, s - 8, s - 7, 3, 3);
                g2.drawLine(7, 9, 10, 12);
                g2.drawLine(10, 12, s - 7, 7);
                g2.drawLine(7, s - 7, s - 7, s - 7);
            });
        }
        if (t.contains("require")) {
            return simpleLineIcon(ink, size, (g2, s) -> {
                g2.drawRoundRect(5, 3, s - 10, s - 5, 3, 3);
                g2.drawLine(8, 8, s - 8, 8);
                g2.drawLine(8, 12, s - 8, 12);
                g2.drawLine(8, 16, s - 11, 16);
            });
        }
        if (t.contains("skill")) {
            return simpleLineIcon(ink, size, (g2, s) -> {
                Polygon star = new Polygon();
                star.addPoint(s / 2, 3);
                star.addPoint(s / 2 + 3, s / 2 - 2);
                star.addPoint(s - 3, s / 2 - 2);
                star.addPoint(s / 2 + 4, s / 2 + 2);
                star.addPoint(s / 2 + 6, s - 3);
                star.addPoint(s / 2, s / 2 + 5);
                star.addPoint(s / 2 - 6, s - 3);
                star.addPoint(s / 2 - 4, s / 2 + 2);
                star.addPoint(3, s / 2 - 2);
                star.addPoint(s / 2 - 3, s / 2 - 2);
                g2.drawPolygon(star);
            });
        }
        return simpleLineIcon(ink, size, (g2, s) -> {
            g2.drawRoundRect(5, 3, s - 10, s - 5, 3, 3);
            g2.drawLine(9, 8, s - 8, 8);
            g2.drawLine(9, 12, s - 8, 12);
            g2.drawLine(9, 16, s - 11, 16);
        });
    }

    private interface SimpleIconPainter {
        void paint(Graphics2D g2, int size);
    }

    private static Icon simpleLineIcon(Color ink, int size, SimpleIconPainter painter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ink);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                painter.paint(g2, size);
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

    private JPanel buildBulletSectionCardPortal(String heading, List<String> items) {
        JPanel padded = new JPanel();
        padded.setLayout(new BoxLayout(padded, BoxLayout.Y_AXIS));
        padded.setOpaque(false);
        padded.setBorder(new EmptyBorder(CARD_PAD_TOP, CARD_PAD_LEFT, CARD_PAD_BOTTOM, CARD_PAD_RIGHT));
        padded.add(portalSectionTitle(heading));
        padded.add(Box.createVerticalStrut(12));
        if (items == null || items.isEmpty()) {
            JLabel empty = new JLabel("None specified.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(JobsPortalUi.TEXT_GRAY_LIGHT);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            padded.add(empty);
        } else {
            for (String line : items) {
                padded.add(purpleBulletRow(line));
                padded.add(Box.createVerticalStrut(8));
            }
        }
        return wrapDetailCard(padded);
    }

    private JPanel purpleBulletRow(String line) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel bullet = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d = 10;
                g2.setColor(JobsPortalUi.PURPLE_600);
                g2.fillOval(0, (getHeight() - d) / 2, d, d);
                g2.setColor(Color.WHITE);
                g2.fillOval(3, (getHeight() - d) / 2 + 3, 4, 4);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(14, 22);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        row.add(bullet, BorderLayout.WEST);
        JTextArea text = createWrappingTextArea(line, new Font("Segoe UI", Font.PLAIN, 14), BODY_GRAY);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildSkillsCardPortal(List<String> skills) {
        JPanel padded = new JPanel();
        padded.setLayout(new BoxLayout(padded, BoxLayout.Y_AXIS));
        padded.setOpaque(false);
        padded.setBorder(new EmptyBorder(CARD_PAD_TOP, CARD_PAD_LEFT, CARD_PAD_BOTTOM, CARD_PAD_RIGHT));
        padded.add(portalSectionTitle("Preferred Skills"));
        padded.add(Box.createVerticalStrut(12));
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        flow.setOpaque(false);
        flow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String skill : skills) {
            flow.add(skillPill(skill));
        }
        padded.add(flow);
        return wrapDetailCard(padded);
    }

    private JComponent skillPill(String skill) {
        JLabel pill = new JLabel(skill);
        pill.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pill.setForeground(JobsPortalUi.PURPLE_600);
        return JobsPortalUi.wrapRoundedInnerHug(pill, 9,
                new Color(253, 252, 255), SOFT_BORDER, 1f, false,
                new Insets(7, 14, 7, 14));
    }

    private JPanel buildSummaryRail(Job job, boolean hasApplied) {
        JPanel padded = new JPanel();
        padded.setLayout(new BoxLayout(padded, BoxLayout.Y_AXIS));
        padded.setOpaque(false);
        padded.setBorder(new EmptyBorder(CARD_PAD_TOP, CARD_PAD_LEFT, CARD_PAD_BOTTOM, CARD_PAD_RIGHT));

        int railInner = SUMMARY_WIDTH - CARD_PAD_LEFT - CARD_PAD_RIGHT;
        int blockW = Math.min(SUMMARY_APPLY_BTN_MAX_W, Math.max(200, railInner));

        JPanel titleInner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleInner.setOpaque(false);
        titleInner.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleInner.add(new JLabel(JobsPortalUi.sparkleIcon(PRIMARY_PURPLE, 22)));
        JLabel summaryTitle = new JLabel("Position Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        summaryTitle.setForeground(TITLE_BLACK);
        titleInner.add(summaryTitle);
        padded.add(titleInner);
        padded.add(Box.createVerticalStrut(18));

        padded.add(centerSummaryRailRow(summaryTintRow(new Color(245, 243, 255),
                circleGlyphIcon(JobsPortalUi.VIOLET_100, JobsPortalUi.PURPLE_600, 26,
                        Page_JobDetail::paintBriefcaseGlyph),
                JobsPortalUi.PURPLE_600,
                "Employment Type",
                nz(job.getEmploymentType())), blockW, 90));

        padded.add(Box.createVerticalStrut(12));
        padded.add(centerSummaryRailRow(summaryTintRow(new Color(239, 246, 255),
                circleGlyphIcon(new Color(219, 234, 254), new Color(37, 99, 235), 26,
                        (g2, ix, iy, s) -> {
                            int cx = ix + s / 2;
                            int cy = iy + s / 2;
                            g2.drawOval(ix + 5, iy + 5, s - 10, s - 10);
                            g2.drawLine(cx, cy, cx, iy + 7);
                            g2.drawLine(cx, cy, ix + s - 7, cy);
                        }),
                new Color(37, 99, 235),
                "Weekly Hours",
                nz(job.getWeeklyHoursDisplay())), blockW, 90));

        padded.add(Box.createVerticalStrut(12));
        padded.add(centerSummaryRailRow(summaryTintRow(new Color(254, 242, 242),
                circleGlyphIcon(new Color(254, 226, 226), new Color(220, 38, 38), 26,
                        (g2, ix, iy, s) -> {
                            g2.drawRoundRect(ix + 5, iy + 6, s - 10, s - 11, 3, 3);
                            g2.drawLine(ix + 5, iy + 10, ix + s - 5, iy + 10);
                            g2.drawLine(ix + 8, iy + 4, ix + 8, iy + 8);
                            g2.drawLine(ix + s - 8, iy + 4, ix + s - 8, iy + 8);
                        }),
                new Color(220, 38, 38),
                "Application Deadline",
                nz(formatDeadline(job.getDeadlineDisplay()))), blockW, 90));

        padded.add(Box.createVerticalStrut(12));
        padded.add(centerSummaryRailRow(summaryTintRow(new Color(240, 253, 244),
                circleGlyphIcon(new Color(209, 250, 229), new Color(22, 163, 74), 26,
                        (g2, ix, iy, s) -> {
                            int cx = ix + s / 2;
                            Polygon p = new Polygon();
                            p.addPoint(cx, iy + 6);
                            p.addPoint(ix + s - 6, iy + s / 2);
                            p.addPoint(cx, iy + s - 6);
                            p.addPoint(ix + 6, iy + s / 2);
                            g2.drawPolygon(p);
                            g2.fillOval(cx - 2, iy + s / 2 - 2, 5, 5);
                        }),
                new Color(22, 163, 74),
                "Location / Mode",
                nz(buildLocationSummary(job))), blockW, 90));

        padded.add(Box.createVerticalStrut(20));
        padded.add(Box.createVerticalGlue());

        JButton applyBtn;
        if (hasApplied) {
            applyBtn = JobsPortalUi.appliedOutlineButton("Applied", new Font("Segoe UI", Font.BOLD, 15));
            applyBtn.setEnabled(false);
        } else {
            applyBtn = JobsPortalUi.gradientButton("Apply Now   \u2192", new Font("Segoe UI", Font.BOLD, 15), null);
            applyBtn.addActionListener(e -> callback.onApply(job));
        }
        int btnW = blockW;
        int btnH = applyBtn.getPreferredSize().height;
        applyBtn.setPreferredSize(new Dimension(btnW, btnH));
        applyBtn.setMinimumSize(new Dimension(btnW, btnH));
        applyBtn.setMaximumSize(new Dimension(btnW, btnH));
        JPanel applyStrip = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        applyStrip.setOpaque(false);
        applyStrip.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyStrip.add(applyBtn);
        padded.add(applyStrip);

        JPanel card = wrapDetailCard(padded);
        Dimension nat = card.getPreferredSize();
        card.setPreferredSize(new Dimension(SUMMARY_WIDTH, Math.max(nat.height, 420)));
        card.setMaximumSize(new Dimension(SUMMARY_WIDTH, Integer.MAX_VALUE));
        return card;
    }

    /** 在侧栏内水平居中：固定宽度色块 / 按钮与标题对齐 */
    private JPanel centerSummaryRailRow(JComponent inner, int width, int height) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setOpaque(false);
        holder.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.setPreferredSize(new Dimension(width, height));
        inner.setMaximumSize(new Dimension(width, height));
        inner.setMinimumSize(new Dimension(Math.min(width, 180), Math.min(height, 72)));
        holder.add(inner);
        return holder;
    }

    private static void paintBriefcaseGlyph(Graphics2D g2, int ix, int iy, int s) {
        int w = s - 10;
        int x = ix + 5;
        int y = iy + 8;
        g2.drawRoundRect(x, y + 3, w, s - 14, 3, 3);
        g2.drawLine(ix + s / 2 - 4, iy + 8, ix + s / 2 + 4, iy + 8);
        g2.drawArc(ix + s / 2 - 5, iy + 5, 10, 8, 0, 180);
    }

    private JPanel summaryTintRow(Color bg, Icon icon, Color labelColor, String label, String value) {
        JPanel row = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(new EmptyBorder(14, 16, 14, 16));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(42, 42));
        iconWrap.add(new JLabel(icon));
        row.add(iconWrap, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel lab = new JLabel(label);
        lab.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lab.setForeground(labelColor);
        lab.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        val.setForeground(TITLE_BLACK);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(Box.createVerticalGlue());
        text.add(lab);
        text.add(Box.createVerticalStrut(5));
        text.add(val);
        text.add(Box.createVerticalGlue());
        row.add(text, BorderLayout.CENTER);
        return row;
    }


    private JPanel wrapDetailCard(JComponent inner) {
        JobsPortalUi.RoundedSurface rs = new JobsPortalUi.RoundedSurface(
                CARD_ARC + 2, SOFT_CARD_BG, SOFT_BORDER, 1f, true, new BorderLayout());
        rs.add(inner, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(rs, BorderLayout.CENTER);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrap;
    }

    /** Course code and department only (no course title in the middle). */
    private static String courseDisplayLine(Job job) {
        String code = job.getCourseCode() != null ? job.getCourseCode().trim() : "";
        String dept = job.getDepartment() != null ? job.getDepartment().trim() : "";
        if (code.isEmpty() && dept.isEmpty()) {
            return "\u2014";
        }
        if (code.isEmpty()) {
            return dept;
        }
        if (dept.isEmpty()) {
            return code;
        }
        return code + "  \u2022  " + dept;
    }

    private static String nz(String s) {
        if (s == null || s.isBlank()) {
            return "\u2014";
        }
        return s;
    }

    private String buildLocationSummary(Job job) {
        String mode = job.getLocationMode();
        if (mode == null) {
            mode = "";
        }
        if (job.getEmployment() != null) {
            String d = job.getEmployment().getLocationDetail();
            if (d != null && !d.isBlank()) {
                return mode.isEmpty() ? d : mode + " (" + d + ")";
            }
        }
        return mode.isEmpty() ? "\u2014" : mode;
    }

    private String formatDeadline(String raw) {
        if (raw == null || raw.length() < 10) {
            return raw != null ? raw : "\u2014";
        }
        String ymd = raw.substring(0, 10);
        String[] p = ymd.split("-");
        if (p.length != 3) {
            return raw;
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        try {
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            if (m >= 1 && m <= 12) {
                return months[m - 1] + " " + d + ", " + p[0];
            }
        } catch (NumberFormatException ignored) {
        }
        return raw;
    }

    private static JTextArea createWrappingTextArea(String text, Font font, Color fg) {
        JTextArea ta = new JTextArea(text == null ? "" : text);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setOpaque(false);
        ta.setFont(font);
        ta.setForeground(fg);
        ta.setBorder(null);
        ta.setMargin(new Insets(0, 0, 0, 0));
        ta.setColumns(1);
        return ta;
    }

    private static final class JobDetailRootPanel extends JPanel implements Scrollable {
        JobDetailRootPanel() {
            super();
            setLayout(new BorderLayout(0, 0));
            setOpaque(true);
            setBackground(JobsPortalUi.PAGE_BG);
            setBorder(new EmptyBorder(28, 48, 40, 48));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bg = new GradientPaint(
                    0, 0, new Color(253, 252, 255),
                    0, getHeight(), new Color(248, 246, 255));
            g2.setPaint(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(109, 77, 235, 18));
            int startX = Math.max(0, getWidth() - 230);
            for (int x = startX; x < getWidth() - 18; x += 10) {
                for (int y = 0; y < 150; y += 10) {
                    g2.fillOval(x, y, 2, 2);
                }
            }
            g2.dispose();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? 16 : 10;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            if (orientation == SwingConstants.VERTICAL) {
                return Math.max(visibleRect.height - 10, 10);
            }
            return Math.max(visibleRect.width - 10, 10);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
