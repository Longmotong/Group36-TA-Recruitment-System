package TA_Job_Application_Module.pages.status;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.ApplicationStatusCodes;
import TA_Job_Application_Module.pages.jobs.JobsPortalUi;
import TA_Job_Application_Module.service.DataService;
import TA_Job_Application_Module.ui.UI_Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Page_ApplicationStatus {

    private static final int CARD_PAD = 20;
    private static final int CARD_GAP = 16;

    /**
     * In {@link BoxLayout#Y_AXIS}, narrow children stay at preferred width and are placed using
     * {@link JComponent#getAlignmentX()} — default 0.5 centers them. Force full track width so
     * {@link SwingConstants#LEFT} text truly hugs the left edge.
     */
    private static void stretchRowInVerticalBox(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (c instanceof JTextArea ta) {
            ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            return;
        }
        Dimension p = c.getPreferredSize();
        int h = Math.max(1, p.height);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    private static final Color PRIMARY_PURPLE = new Color(0x6D4DEB);
    private static final Color DEEP_PURPLE = new Color(0x4F35D9);
    private static final Color LAVENDER_BG = new Color(0xF3EEFF);
    private static final Color LIGHT_PURPLE_BORDER = new Color(0xDED4FF);
    private static final Color PAGE_TOP = new Color(0xFDFCFF);
    private static final Color PAGE_BOTTOM = new Color(0xF8F6FF);
    private static final Color DARK_TEXT = new Color(0x111033);
    private static final Color MUTED_TEXT = new Color(0x667085);
    private static final Color SUCCESS = new Color(0x059669);
    private static final Color SUCCESS_DARK = new Color(0x047857);
    private static final Color DANGER = new Color(0xEF4444);
    private static final Color DANGER_DARK = new Color(0xDC2626);
    private static final Color BLUE = new Color(0x2F80ED);
    private static final Color AMBER = new Color(0xD49300);
    private static final Color TEAL = new Color(0x12B3A8);

    private static final Color TIMELINE_LINE = new Color(0xD9D3F5);
    private static final Color TIMELINE_PENDING_LINE = new Color(0xD6D9E6);

    public interface StatusCallback {
        void onBackToApplications();
        void onCancelled();
        void onAcceptOffer(String applicationId);
        void onDeclineOffer(String applicationId);
    }

    private JPanel panel;
    private StatusCallback callback;
    private Application currentApp;
    private DataService dataService;

    public Page_ApplicationStatus(StatusCallback callback, DataService dataService) {
        this.callback = callback;
        this.dataService = dataService;
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    /** 模态框相对顶层窗口居中；若传内层 {@link #panel}，在滚动布局下易相对视区底部居中。 */
    private Component dialogOwnerForModals() {
        Window w = SwingUtilities.getWindowAncestor(panel);
        return w != null ? w : panel;
    }

    public String getCurrentApplicationId() {
        return currentApp != null ? currentApp.getApplicationId() : null;
    }

    public void showApplication(Application app) {
        this.currentApp = app;
        panel.removeAll();
        buildContent(app);
        panel.revalidate();
        panel.repaint();
    }

    private void initPanel() {
        panel = new ApplicationStatusRootPanel();
    }

    private void buildContent(Application app) {
        JButton backBtn = new JButton("\u2190  Back to My Applications");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(PRIMARY_PURPLE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setBorder(new EmptyBorder(0, 0, 8, 0));
        backBtn.addActionListener(e -> callback.onBackToApplications());
        panel.add(backBtn);
        panel.add(Box.createVerticalStrut(8));

        JLabel pageTitle = new JLabel("Application Status");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 34));
        pageTitle.setForeground(DARK_TEXT);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(pageTitle);

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(48, 4));
        underline.setMaximumSize(new Dimension(48, 4));
        underline.setMinimumSize(new Dimension(48, 4));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(8));
        panel.add(underline);
        panel.add(Box.createVerticalStrut(18));

        panel.add(buildSummaryCard(app));

        JPanel offerCard = buildOfferCard(app);
        if (offerCard != null) {
            panel.add(Box.createVerticalStrut(CARD_GAP));
            panel.add(offerCard);
        }

        JPanel cancelCard = buildCancelCard(app);
        if (cancelCard != null) {
            panel.add(Box.createVerticalStrut(CARD_GAP));
            panel.add(cancelCard);
        }

        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildTimelineCard(app));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildStatusInfoCard(app));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildTextCard("Reviewer Notes", defaultReviewerNotes(app), resolveStatusColorKey(app), starIcon(PRIMARY_PURPLE, 28), PRIMARY_PURPLE));
        panel.add(Box.createVerticalStrut(CARD_GAP));
        panel.add(buildTextCard("Next Steps", defaultNextSteps(app), resolveStatusColorKey(app), arrowRightIcon(BLUE, 28), BLUE));
    }

    private JPanel buildSummaryCard(Application app) {
        Application.JobSnapshot job = app.getJobSnapshot();
        Application.ApplicantSnapshot who = app.getApplicantSnapshot();
        Application.Meta meta = app.getMeta();

        String titleText = job != null && job.getTitle() != null ? job.getTitle() : "Application";
        String courseDept = "";
        if (job != null) {
            String cc = job.getCourseCode() != null ? job.getCourseCode() : "";
            String dept = job.getDepartment() != null ? job.getDepartment() : "";
            courseDept = cc + "  \u2022  " + dept;
        }
        String applicant = who != null && who.getFullName() != null ? who.getFullName() : "";
        String applied = "";
        if (meta != null && meta.getSubmittedAt() != null) {
            applied = "Applied " + formatDateTime(meta.getSubmittedAt());
        }

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 16, 18, 18));

        JLabel icon = new JLabel(briefcaseIcon(PRIMARY_PURPLE, 28));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(icon, 18, LAVENDER_BG, null, 0f, false, new Insets(10, 10, 10, 10));
        iconTile.setPreferredSize(new Dimension(64, 64));
        iconTile.setMinimumSize(new Dimension(64, 64));
        iconTile.setMaximumSize(new Dimension(64, 64));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(DARK_TEXT);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        stretchRowInVerticalBox(title);
        textCol.add(title);

        textCol.add(Box.createVerticalStrut(8));
        String metaLine = joinMeta(courseDept, applicant, applied);
        JLabel metaLbl = new JLabel("<html><body style='text-align:left;margin:0;padding:0'>" + escapeHtml(metaLine) + "</body></html>");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLbl.setForeground(MUTED_TEXT);
        metaLbl.setHorizontalAlignment(SwingConstants.LEFT);
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        stretchRowInVerticalBox(metaLbl);
        textCol.add(metaLbl);

        textCol.add(Box.createVerticalStrut(18));
        JPanel statusStrip = buildCurrentStatusBar(app);
        stretchRowInVerticalBox(statusStrip);
        textCol.add(statusStrip);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 0, 10);
        content.add(iconTile, g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 0);
        content.add(textCol, g);

        JPanel card = wrapInModernCard(content);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private static String joinMeta(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("  \u2022  ");
            }
            sb.append(p.trim());
        }
        return sb.toString();
    }

    private JPanel buildCurrentStatusBar(Application app) {
        Application.Status st = app.getStatus();
        String label = st != null && st.getLabel() != null ? st.getLabel() : "Unknown";
        StatusPalette p = statusPalette(resolveStatusColorKey(app));

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 8, 10, 12));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel icon = new JLabel(checkCircleIcon(p.ink, 23));
        icon.setVerticalAlignment(SwingConstants.CENTER);
        bar.add(icon, BorderLayout.WEST);

        JLabel text = new JLabel("<html><body style='text-align:left;margin:0;padding:0'><b>Current Status:</b> <span style='color:rgb(" + p.ink.getRed() + "," + p.ink.getGreen() + "," + p.ink.getBlue() + ")'>" + escapeHtml(label) + "</span></body></html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        text.setForeground(MUTED_TEXT);
        text.setVerticalAlignment(SwingConstants.CENTER);
        text.setHorizontalAlignment(SwingConstants.LEFT);
        bar.add(text, BorderLayout.CENTER);

        return JobsPortalUi.wrapRoundedInner(bar, 12, p.softBg, p.border, 1f, false, new Insets(0, 0, 0, 0));
    }

    private String resolveStatusColorKey(Application app) {
        Application.Status st = app.getStatus();
        if (st == null) {
            return "yellow";
        }
        String cur = st.getCurrent();
        if (cur != null) {
            switch (cur.toLowerCase()) {
                case "offer_pending":
                    return "teal";
                case "accepted":
                    return "green";
                case "rejected":
                    return "red";
                case "under_review":
                    return "blue";
                case "pending":
                    return "yellow";
                default:
                    break;
            }
        }
        return "yellow";
    }

    private static String normalizeColorKey(String c) {
        if (c == null) {
            return "yellow";
        }
        return switch (c.trim().toLowerCase()) {
            case "green" -> "green";
            case "red" -> "red";
            case "blue" -> "blue";
            case "teal" -> "teal";
            case "yellow" -> "yellow";
            default -> "yellow";
        };
    }

    private JPanel buildTimelineCard(Application app) {
        String ck = resolveStatusColorKey(app);
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));

        JLabel icon = new JLabel(progressIcon(PRIMARY_PURPLE, 28));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(icon, 18, LAVENDER_BG, null, 0f, false, new Insets(13, 13, 13, 13));
        iconTile.setPreferredSize(new Dimension(62, 62));
        iconTile.setMinimumSize(new Dimension(62, 62));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionHeading("Application Progress"));
        content.add(Box.createVerticalStrut(16));

        List<Application.TimelineEvent> events = orderedTimeline(app);
        if (events.isEmpty()) {
            JLabel empty = new JLabel("No timeline events yet.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(MUTED_TEXT);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            stretchRowInVerticalBox(empty);
            content.add(empty);
        } else {
            TimelineProgressPanel progressPanel = new TimelineProgressPanel(events, statusPalette(ck).ink);
            progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            stretchRowInVerticalBox(progressPanel);
            content.add(progressPanel);
        }

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 0, 12);
        inner.add(iconTile, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(content, g);
        JPanel card = wrapInModernCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private List<Application.TimelineEvent> orderedTimeline(Application app) {
        List<Application.TimelineEvent> raw = app.getTimeline();
        List<Application.TimelineEvent> list = new ArrayList<>();
        if (raw != null) {
            list.addAll(raw);
        }
        list.sort(Comparator.comparing(e -> e.getTimestamp() != null ? e.getTimestamp() : ""));
        return list;
    }

    private JPanel buildTimelineRow(Application.TimelineEvent ev, boolean drawLineBelow, String colorKey) {
        // Kept for compatibility with the previous vertical implementation; the current UI uses TimelineProgressPanel.
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JPanel buildStatusInfoCard(Application app) {
        Application.Review review = app.getReview();
        String msg = review != null && review.getStatusMessage() != null && !review.getStatusMessage().isEmpty()
            ? review.getStatusMessage()
            : "Your application is being processed.";
        StatusPalette p = statusPalette(resolveStatusColorKey(app));

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));

        JLabel icon = new JLabel(infoIcon(p.ink, 28));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(icon, 18, p.iconBg, null, 0f, false, new Insets(13, 13, 13, 13));
        iconTile.setPreferredSize(new Dimension(62, 62));
        iconTile.setMinimumSize(new Dimension(62, 62));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionHeading("Status Information"));
        content.add(Box.createVerticalStrut(12));

        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextArea ta = modernTextArea(msg, new Font("Segoe UI", Font.PLAIN, 14), p.ink);
        box.add(ta, BorderLayout.CENTER);
        JPanel tinted = JobsPortalUi.wrapRoundedInner(box, 12, p.softBg, p.border, 1f, false, new Insets(0, 0, 0, 0));
        tinted.setAlignmentX(Component.LEFT_ALIGNMENT);
        stretchRowInVerticalBox(tinted);
        content.add(tinted);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 0, 12);
        inner.add(iconTile, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(content, g);
        JPanel card = wrapInModernCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private static final int TEXT_CARD_ACCENT_WIDTH = 4;
    private static final int TEXT_CARD_GAP_AFTER_ACCENT = 14;

    private JPanel buildTextCard(String heading, String body, String colorKey) {
        StatusPalette p = statusPalette(colorKey);
        return buildTextCard(heading, body, colorKey, starIcon(p.ink, 28), p.ink);
    }

    private JPanel buildTextCard(String heading, String body, String colorKey, Icon icon, Color accentColor) {
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));

        Color iconBg = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 28);
        JLabel iconLabel = new JLabel(icon);
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(iconLabel, 18, iconBg, null, 0f, false, new Insets(13, 13, 13, 13));
        iconTile.setPreferredSize(new Dimension(62, 62));
        iconTile.setMinimumSize(new Dimension(62, 62));

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel accent = new JPanel();
        accent.setOpaque(true);
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(TEXT_CARD_ACCENT_WIDTH, 0));
        accent.setMinimumSize(new Dimension(TEXT_CARD_ACCENT_WIDTH, 0));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.setBorder(new EmptyBorder(0, TEXT_CARD_GAP_AFTER_ACCENT, 0, 0));
        JTextArea bodyTa = modernTextArea(body == null ? "" : body, new Font("Segoe UI", Font.PLAIN, 14), new Color(0x30364F));
        text.add(sectionHeading(heading));
        text.add(Box.createVerticalStrut(10));
        text.add(bodyTa);
        stretchRowInVerticalBox(bodyTa);

        content.add(accent, BorderLayout.WEST);
        content.add(text, BorderLayout.CENTER);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 0, 12);
        inner.add(iconTile, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 0);
        inner.add(content, g);

        JPanel card = wrapInModernCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private String defaultReviewerNotes(Application app) {
        Application.Review r = app.getReview();
        if (r != null && r.getReviewerNotes() != null && !r.getReviewerNotes().isEmpty()) {
            return r.getReviewerNotes();
        }
        return "No reviewer notes yet.";
    }

    private String defaultNextSteps(Application app) {
        Application.Review r = app.getReview();
        if (r != null && r.getNextSteps() != null && !r.getNextSteps().isEmpty()) {
            return r.getNextSteps();
        }
        return "Please wait for further updates from the hiring team.";
    }

    private JLabel sectionHeading(String text) {
        JLabel h = new JLabel(text);
        h.setFont(new Font("Segoe UI", Font.BOLD, 17));
        h.setForeground(DARK_TEXT);
        h.setHorizontalAlignment(SwingConstants.LEFT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        stretchRowInVerticalBox(h);
        return h;
    }

    private JPanel buildOfferCard(Application app) {
        String cur = app.getStatus() != null ? app.getStatus().getCurrent() : "";
        if (!ApplicationStatusCodes.isOfferPending(cur)) {
            return null;
        }

        // 文案与按钮不要同一行 BorderLayout（EAST 会挤占 CENTER，HTML 固定宽度时岗位名易被裁断）
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel upper = new JPanel(new BorderLayout(18, 0));
        upper.setOpaque(false);
        upper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel offerIcon = new JLabel(JobsPortalUi.trophyIcon(TEAL, 38));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(offerIcon, 34, new Color(0xDDFBF2), null, 0f, false, new Insets(14, 14, 14, 14));
        iconTile.setPreferredSize(new Dimension(78, 78));
        iconTile.setMinimumSize(new Dimension(78, 78));
        upper.add(iconTile, BorderLayout.WEST);

        JPanel textSide = new JPanel();
        textSide.setLayout(new BoxLayout(textSide, BoxLayout.Y_AXIS));
        textSide.setOpaque(false);

        // 不用 Unicode 警告符（Swing HTML 在部分环境下会显示成方块）
        JPanel headingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headingRow.setOpaque(false);
        headingRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headingRow.add(new JLabel(JobsPortalUi.exclamationCircleIcon(new Color(0xDC2626), 22)));
        JLabel heading = new JLabel("<html><body><span style='color:#DC2626;'>ACTION REQUIRED</span>"
                + " <span style='color:#111827;'>— Offer Received!</span></body></html>");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(DARK_TEXT);
        headingRow.add(heading);
        textSide.add(headingRow);

        textSide.add(Box.createVerticalStrut(8));
        Application.JobSnapshot job = app.getJobSnapshot();
        String jobTitle = job != null ? job.getTitle() : "this position";
        JComponent hint = createOfferCongratulationsHint(escapeHtml(jobTitle));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        textSide.add(hint);
        upper.add(textSide, BorderLayout.CENTER);
        content.add(upper);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton declineBtn = new RoundedActionButton("Decline Offer", new Color(0xFF4D4F), new Color(0xDC2626), Color.WHITE);
        declineBtn.setPreferredSize(new Dimension(152, 48));
        declineBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                dialogOwnerForModals(),
                "Are you sure you want to decline this offer?\nThis action cannot be undone.",
                "Confirm Decline",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                callback.onDeclineOffer(app.getApplicationId());
            }
        });

        JButton acceptBtn = new RoundedActionButton("Accept Offer", new Color(0x10B981), new Color(0x059669), Color.WHITE);
        acceptBtn.setPreferredSize(new Dimension(152, 48));
        acceptBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                dialogOwnerForModals(),
                "Do you confirm your acceptance of this TA position?",
                "Confirm Acceptance",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                callback.onAcceptOffer(app.getApplicationId());
            }
        });

        buttonPanel.add(declineBtn);
        buttonPanel.add(acceptBtn);
        content.add(Box.createVerticalStrut(14));
        content.add(buttonPanel);

        JobsPortalUi.RoundedSurface surface = new JobsPortalUi.RoundedSurface(18, Color.WHITE, new Color(0x7CE6CA), 1.2f, true, new BorderLayout());
        surface.add(content, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(surface, BorderLayout.CENTER);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrap;
    }

    private JPanel buildCancelCard(Application app) {
        String cur = app.getStatus() != null ? app.getStatus().getCurrent() : "";
        // Only show cancel option for pending status, not for offer_pending (offer has its own decline button)
        if (!"pending".equalsIgnoreCase(cur)) {
            return null;
        }

        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(exclamationIcon(DANGER, 28));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(icon, 18, new Color(0xFEE2E2), null, 0f, false, new Insets(13, 13, 13, 13));
        iconTile.setPreferredSize(new Dimension(62, 62));
        content.add(iconTile, BorderLayout.WEST);

        JPanel textSide = new JPanel();
        textSide.setLayout(new BoxLayout(textSide, BoxLayout.Y_AXIS));
        textSide.setOpaque(false);

        JLabel heading = sectionHeading("Withdraw Application");
        textSide.add(heading);
        textSide.add(Box.createVerticalStrut(8));
        JLabel hint = new JLabel("<html><body style='width:620px;color:#667085;'>If you no longer wish to pursue this position, you can withdraw your application. This action cannot be undone.</body></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textSide.add(hint);

        content.add(textSide, BorderLayout.CENTER);

        JButton cancelBtn = new RoundedActionButton("Withdraw Application", new Color(0xFF4D4F), new Color(0xDC2626), Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(190, 48));
        cancelBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                dialogOwnerForModals(),
                "Are you sure you want to withdraw this application?\nThis action cannot be undone.",
                "Confirm Withdrawal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                callback.onCancelled();
            }
        });
        content.add(cancelBtn, BorderLayout.EAST);

        JPanel card = wrapInModernCard(content);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createCardShell() {
        JPanel c = new JPanel();
        c.setBackground(UI_Constants.CARD_BG);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD)
        ));
        return c;
    }

    private JPanel wrapInCard(JComponent inner) {
        JPanel shell = createCardShell();
        shell.setLayout(new BorderLayout());
        shell.add(inner, BorderLayout.CENTER);
        return shell;
    }

    private JPanel wrapInModernCard(JComponent inner) {
        JobsPortalUi.RoundedSurface surface = new JobsPortalUi.RoundedSurface(18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        surface.add(inner, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(surface, BorderLayout.CENTER);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return wrap;
    }

    private String formatDateTime(String iso) {
        if (iso == null || iso.length() < 10) {
            return iso != null ? iso : "";
        }
        String ymd = iso.substring(0, 10);
        String nice = formatDateYmd(ymd);
        if (iso.length() >= 16) {
            String hm = iso.substring(11, 16);
            return nice + " at " + hm;
        }
        return nice;
    }

    private String formatDateYmd(String ymd) {
        String[] p = ymd.split("-");
        if (p.length != 3) {
            return ymd;
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        try {
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            if (m >= 1 && m <= 12) {
                return months[m - 1] + " " + d + ", " + p[0];
            }
        } catch (NumberFormatException ignored) { }
        return ymd;
    }

    /**
     * Offer 祝贺语：用 HTML {@link JTextPane} 放在无滚动条的 {@link JScrollPane} 中，
     * {@link JTextPane#getScrollableTracksViewportWidth()} 为 true 时按视口宽度换行，避免 JLabel 固定像素宽度被裁切。
     */
    private static JComponent createOfferCongratulationsHint(String escapedJobTitle) {
        String html = "<html><head><style type='text/css'>"
                + "body { margin:0; font-family:Segoe UI; font-size:14px; color:#374151; line-height:1.45; }"
                + "b { color:#111827; font-weight:bold; }"
                + ".urgent { color:#DC2626; font-weight:bold; }"
                + "</style></head><body>"
                + "Congratulations! You have been offered the position of <b>" + escapedJobTitle + "</b>. "
                + "<span class='urgent'>Please respond promptly</span> to confirm your acceptance."
                + "</body></html>";

        JTextPane pane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        pane.setContentType("text/html");
        pane.setText(html);
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pane.setBorder(null);
        pane.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(pane);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        return sp;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JTextArea modernTextArea(String text, Font font, Color color) {
        JTextArea ta = new JTextArea(text == null ? "" : text);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setOpaque(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(font);
        ta.setForeground(color);
        ta.setBorder(BorderFactory.createEmptyBorder());
        ta.setMargin(new Insets(0, 0, 0, 0));
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ta;
    }

    private static StatusPalette statusPalette(String colorKey) {
        return switch (normalizeColorKey(colorKey)) {
            case "green" -> new StatusPalette(new Color(0xECFDF5), new Color(0xD1FAE5), new Color(0xA7F3D0), SUCCESS_DARK);
            case "teal" -> new StatusPalette(new Color(0xECFEF8), new Color(0xDDFBF2), new Color(0x99F6E4), new Color(0x0F8E83));
            case "red" -> new StatusPalette(new Color(0xFEF2F2), new Color(0xFEE2E2), new Color(0xFCA5A5), DANGER_DARK);
            case "blue" -> new StatusPalette(new Color(0xEFF6FF), new Color(0xDBEAFE), new Color(0x93C5FD), BLUE);
            default -> new StatusPalette(new Color(0xFFFBEB), new Color(0xFEF3C7), new Color(0xFDE68A), AMBER);
        };
    }

    private record StatusPalette(Color softBg, Color iconBg, Color border, Color ink) { }

    private static final class ApplicationStatusRootPanel extends JPanel implements Scrollable {
        ApplicationStatusRootPanel() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(JobsPortalUi.PAGE_BG);
            setBorder(new EmptyBorder(28, 48, 40, 48));
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, PAGE_TOP, 0, getHeight(), PAGE_BOTTOM));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(109, 77, 235, 16));
            int startX = Math.max(0, getWidth() - 250);
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

    private final class TimelineProgressPanel extends JComponent {
        private final List<Application.TimelineEvent> events;
        private final Color accent;

        TimelineProgressPanel(List<Application.TimelineEvent> events, Color accent) {
            this.events = events;
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(760, 120));
            setMinimumSize(new Dimension(300, 120));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int n = Math.max(1, events.size());
            int left = 70;
            int right = 70;
            int y = 34;
            int w = Math.max(1, getWidth() - left - right);
            int step = n == 1 ? 0 : w / (n - 1);

            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                Application.TimelineEvent a = events.get(i);
                Application.TimelineEvent b = events.get(i + 1);
                int x1 = left + i * step;
                int x2 = left + (i + 1) * step;
                boolean doneA = isDone(a);
                boolean doneB = isDone(b);
                if (doneA && doneB) {
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x1 + 15, y, x2 - 15, y);
                } else {
                    g2.setColor(TIMELINE_PENDING_LINE);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 5f}, 0f));
                    g2.drawLine(x1 + 15, y, x2 - 15, y);
                }
            }

            Font titleFont = new Font("Segoe UI", Font.BOLD, 12);
            Font timeFont = new Font("Segoe UI", Font.PLAIN, 12);
            for (int i = 0; i < n; i++) {
                Application.TimelineEvent ev = events.get(i);
                int x = left + i * step;
                boolean done = isDone(ev);

                if (done) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 36));
                    g2.fillOval(x - 18, y - 18, 36, 36);
                    g2.setColor(accent);
                    g2.fillOval(x - 14, y - 14, 28, 28);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x - 6, y, x - 2, y + 5);
                    g2.drawLine(x - 2, y + 5, x + 7, y - 6);
                } else {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                    g2.fillOval(x - 18, y - 18, 36, 36);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 13, y - 13, 26, 26);
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawOval(x - 13, y - 13, 26, 26);
                    g2.fillOval(x - 5, y - 5, 10, 10);
                }

                String stepTitle = ev.getStepLabel() != null && !ev.getStepLabel().isEmpty()
                        ? ev.getStepLabel()
                        : ev.getStepKey();
                String ts = formatDateTime(ev.getTimestamp());

                int textW = Math.min(160, Math.max(90, step == 0 ? 160 : step - 14));
                int tx = x - textW / 2;
                drawCenteredText(g2, stepTitle, titleFont, done ? accent : MUTED_TEXT, tx, y + 48, textW);
                if (ts != null && !ts.isEmpty()) {
                    drawCenteredText(g2, ts, timeFont, MUTED_TEXT, tx, y + 72, textW);
                }
            }
            g2.dispose();
        }

        private boolean isDone(Application.TimelineEvent ev) {
            return ev == null || ev.getStatus() == null || "completed".equalsIgnoreCase(ev.getStatus());
        }

        private void drawCenteredText(Graphics2D g2, String text, Font font, Color color, int x, int baseline, int width) {
            if (text == null) {
                return;
            }
            g2.setFont(font);
            g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics(font);
            String t = ellipsize(text, fm, width);
            int tx = x + Math.max(0, (width - fm.stringWidth(t)) / 2);
            g2.drawString(t, tx, baseline);
        }

        private String ellipsize(String text, FontMetrics fm, int width) {
            if (fm.stringWidth(text) <= width) {
                return text;
            }
            String ell = "...";
            int max = Math.max(0, width - fm.stringWidth(ell));
            int len = text.length();
            while (len > 0 && fm.stringWidth(text.substring(0, len)) > max) {
                len--;
            }
            return len <= 0 ? ell : text.substring(0, len) + ell;
        }
    }

    private static final class RoundedActionButton extends JButton {
        private final Color left;
        private final Color right;
        private final Color text;
        private boolean hover;

        RoundedActionButton(String label, Color left, Color right, Color text) {
            super(label);
            this.left = left;
            this.right = right;
            this.text = text;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color l = hover ? right : left;
            Color r = hover ? left.darker() : right;
            g2.setColor(new Color(17, 16, 51, hover ? 36 : 20));
            g2.fillRoundRect(0, 3, w, h - 3, 12, 12);
            g2.setPaint(new GradientPaint(0, 0, l, w, 0, r));
            g2.fillRoundRect(0, 0, w, h - 2, 12, 12);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String s = getText();
            int tx = (w - fm.stringWidth(s)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent() - 1;
            g2.setColor(text);
            g2.drawString(s, tx, ty);
            g2.dispose();
        }
    }

    private static Icon briefcaseIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = 5;
            int y = 9;
            g2.drawRoundRect(x, y, s - 10, s - 12, 5, 5);
            g2.drawLine(s / 2 - 5, y, s / 2 - 5, y - 4);
            g2.drawLine(s / 2 + 5, y, s / 2 + 5, y - 4);
            g2.drawLine(s / 2 - 5, y - 4, s / 2 + 5, y - 4);
        });
    }

    private static Icon progressIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[] xs = {5, 12, 19, 27};
            int[] ys = {20, 12, 18, 8};
            for (int i = 0; i < xs.length; i++) {
                g2.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
                if (i < xs.length - 1) {
                    g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
                }
            }
        });
    }

    private static Icon checkCircleIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int pad = Math.max(3, Math.round(s * 0.18f));
            g2.drawOval(pad, pad, s - 2 * pad, s - 2 * pad);
            int cx = s / 2;
            int cy = s / 2;
            g2.drawLine(cx - 5, cy + 1, cx - 1, cy + 5);
            g2.drawLine(cx - 1, cy + 5, cx + 5, cy - 3);
        });
    }

    private static Icon infoIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(4, 4, s - 8, s - 8);
            g2.fillOval(s / 2 - 2, 8, 4, 4);
            g2.drawLine(s / 2, s / 2 - 1, s / 2, s - 9);
        });
    }

    private static Icon starIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            Path2D p = new Path2D.Double();
            double cx = s / 2.0;
            double cy = s / 2.0;
            for (int i = 0; i < 10; i++) {
                double a = -Math.PI / 2 + i * Math.PI / 5;
                double r = (i % 2 == 0) ? s * 0.40 : s * 0.18;
                double x = cx + Math.cos(a) * r;
                double y = cy + Math.sin(a) * r;
                if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
            }
            p.closePath();
            g2.fill(p);
        });
    }

    private static Icon arrowRightIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(5, s / 2, s - 6, s / 2);
            g2.drawLine(s - 6, s / 2, s - 13, s / 2 - 7);
            g2.drawLine(s - 6, s / 2, s - 13, s / 2 + 7);
            g2.drawLine(5, s / 2 + 7, s / 2, s / 2 + 7);
        });
    }

    private static Icon exclamationIcon(Color ink, int size) {
        return glyph(size, (g2, s) -> {
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(4, 4, s - 8, s - 8);
            g2.drawLine(s / 2, 8, s / 2, s - 13);
            g2.fillOval(s / 2 - 2, s - 9, 4, 4);
        });
    }

    private interface GlyphPainter {
        void paint(Graphics2D g2, int size);
    }

    private static Icon glyph(int size, GlyphPainter painter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
}
