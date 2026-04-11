

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 仪表盘：主内容区纵向铺满，卡片与状态区随窗口放大
 * - 两个主模块丰富说明
 * - Quick Status 实时从 DataService 读取
 */
public class Page_Dashboard {

    public interface NavigationCallback {
        void goToProfile();
        void goToJobs();
        void goToApplications();
    }

    private static final int CARD_ARC = 12;
    private static final int OUTER_PAD_V = 28;
    private static final int OUTER_PAD_H = 22;
    private static final int CARD_GAP = 16;
    private static final int SECTION_GAP = 18;

    /** 固定英文区域，避免默认区域在月份处输出不可见/缺字字符，JTextArea 显示为方块 */
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    /** Overview 更新时间：与 DTF 一致使用英文区域，避免 “Apr” 等月份在部分字体下显示为 □ */
    private static final DateTimeFormatter UPDATE_TIME_FMT =
        DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH);

    private JPanel panel;
    /** 中间区域（含 Overview），用于可靠替换底部状态卡片 */
    private JPanel bodyPanel;
    private TAUser currentUser;
    private NavigationCallback callback;
    private DataService dataService;

    public Page_Dashboard(TAUser currentUser, NavigationCallback callback) {
        this.currentUser = currentUser;
        this.callback = callback;
        this.dataService = DataService.getInstance();
        initPanel();
    }

    public JPanel getPanel() {
        return panel;
    }

    /** 从 DataService 重新拉取申请与职位数据并重建 Overview（提交成功或回到 Home 时调用） */
    public void refreshOverview() {
        if (bodyPanel == null) {
            return;
        }
        BorderLayout bl = (BorderLayout) bodyPanel.getLayout();
        Component old = bl.getLayoutComponent(BorderLayout.SOUTH);
        if (old != null) {
            bodyPanel.remove(old);
        }
        bodyPanel.add(buildQuickStatusCard(), BorderLayout.SOUTH);
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    /** 与 JSON / 代码中可能出现的写法统一（如 under_review vs "under review"） */
    private static String normalizeStatusBucket(Application a) {
        if (a == null || a.getStatus() == null) {
            return "other";
        }
        String raw = a.getStatus().getCurrent();
        if (raw == null || raw.isEmpty()) {
            return "other";
        }
        String c = raw.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (c) {
            case "pending" -> "pending";
            case "under_review", "underreview" -> "under_review";
            case "accepted" -> "accepted";
            case "rejected" -> "rejected";
            case "cancelled" -> "cancelled";
            default -> "other";
        };
    }

    private void initPanel() {
        panel = new JPanel(new BorderLayout(0, SECTION_GAP));
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(OUTER_PAD_V, OUTER_PAD_H, OUTER_PAD_V, OUTER_PAD_H));

        panel.add(buildHeader(), BorderLayout.NORTH);

        // 中间：两卡片仅占用内容高度，剩余纵向空间由透明填充区吸收（避免 GridLayout 把卡片拉满整屏）
        bodyPanel = new JPanel(new BorderLayout(0, SECTION_GAP));
        bodyPanel.setOpaque(false);

        JPanel cardsArea = new JPanel(new GridBagLayout());
        cardsArea.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 0;
        gc.anchor = GridBagConstraints.NORTH;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, CARD_GAP / 2);
        gc.gridx = 0;
        gc.weightx = 0.5;
        cardsArea.add(buildProfileModuleCard(), gc);
        gc.gridx = 1;
        gc.insets = new Insets(0, CARD_GAP / 2, 0, 0);
        cardsArea.add(buildJobModuleCard(), gc);
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel verticalFiller = new JPanel();
        verticalFiller.setOpaque(false);
        cardsArea.add(verticalFiller, gc);

        bodyPanel.add(cardsArea, BorderLayout.CENTER);

        bodyPanel.add(buildQuickStatusCard(), BorderLayout.SOUTH);

        panel.add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleMain = new JLabel("TA Dashboard");
        titleMain.setFont(new Font("Segoe UI", Font.BOLD, 46));
        titleMain.setForeground(UI_Constants.TEXT_PRIMARY);
        top.add(titleMain, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Welcome! Please select a function module to get started.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitle.setForeground(UI_Constants.TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(12, 0, 0, 0));
        top.add(subtitle, BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildProfileModuleCard() {
        RoundedPanel card = new RoundedPanel(CARD_ARC, UI_Constants.BORDER_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        JPanel header = buildModuleHeader("\uD83D\uDC64", "Profile Module",
            new Color(219, 234, 254), UI_Constants.INFO_COLOR);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(header);
        stack.add(Box.createVerticalStrut(10));

        String profileBody = ""
            + "Keep your profile current: name, contact, and academic background help departments match you to the right TA role.\n\n"
            + "Upload your CV and tag relevant skills so reviewers see your strengths at a glance.\n\n"
            + "A complete profile speeds up applications and helps you stand out.";
        JScrollPane descSp = wrapCardDescription(profileBody, 6);
        descSp.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(descSp);

        stack.add(Box.createVerticalStrut(14));

        JButton goProfile = UI_Helper.createDarkButtonLarge("Go to Profile");
        goProfile.setPreferredSize(new Dimension(0, 46));
        goProfile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        goProfile.addActionListener(e -> callback.goToProfile());
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnWrap.add(goProfile, BorderLayout.CENTER);
        stack.add(btnWrap);

        card.add(stack, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildJobModuleCard() {
        RoundedPanel card = new RoundedPanel(CARD_ARC, UI_Constants.BORDER_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        JPanel header = buildModuleHeader("\uD83D\uDCBC", "Job Application Module",
            new Color(209, 250, 229), UI_Constants.SUCCESS_COLOR);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(header);
        stack.add(Box.createVerticalStrut(10));

        String jobBody = ""
            + "Browse open TA roles across departments. Filter by course, hours, or work mode, then apply in a guided flow.\n\n"
            + "Track every submission from pending through review to a final decision.\n\n"
            + "Deadline hints on the dashboard help you prioritize before listings close.";
        JScrollPane descSp = wrapCardDescription(jobBody, 6);
        descSp.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(descSp);

        stack.add(Box.createVerticalStrut(14));

        JPanel jobActions = new JPanel(new GridLayout(1, 2, 12, 0));
        jobActions.setOpaque(false);
        jobActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JButton browseJobsBtn = UI_Helper.createDarkButtonLarge("Browse Jobs");
        browseJobsBtn.setPreferredSize(new Dimension(0, 46));
        browseJobsBtn.addActionListener(e -> callback.goToJobs());
        JButton myAppsBtn = UI_Helper.createOutlineButton("My Applications");
        myAppsBtn.setPreferredSize(new Dimension(0, 46));
        myAppsBtn.addActionListener(e -> callback.goToApplications());
        jobActions.add(browseJobsBtn);
        jobActions.add(myAppsBtn);
        stack.add(jobActions);

        card.add(stack, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildModuleHeader(String icon, String title, Color iconBg, Color iconFg) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        header.setOpaque(false);

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
        };
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setOpaque(false);
        iconPanel.setLayout(new GridBagLayout());
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        iconLbl.setForeground(iconFg);
        iconPanel.add(iconLbl);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLbl.setForeground(UI_Constants.TEXT_PRIMARY);
        header.add(iconPanel);
        header.add(titleLbl);
        return header;
    }

    /** 模块说明：Segoe UI + 自动换行，避免 HTML 默认衬线体与裁切 */
    private static JScrollPane wrapCardDescription(String text, int rows) {
        JTextArea ta = new JTextArea(text);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        ta.setForeground(new Color(0x4B5563));
        ta.setOpaque(false);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setRows(rows);
        ta.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 8));
        ta.setHighlighter(null);
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        int h = ta.getPreferredSize().height + 2;
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return sp;
    }

    /** 默认行数的重载方法（兼容现有调用） */
    private static JScrollPane wrapCardDescription(String text) {
        return wrapCardDescription(text, 10);
    }

    /** 实时从 DataService 读取数据构建 Quick Status 卡片 */
    private JPanel buildQuickStatusCard() {
        RoundedPanel outer = new RoundedPanel(CARD_ARC, UI_Constants.BORDER_COLOR);
        outer.setLayout(new BorderLayout(0, 14));
        outer.setBorder(new EmptyBorder(20, 24, 24, 24));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel statusTitle = new JLabel("Quick Status Overview");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        statusTitle.setForeground(UI_Constants.TEXT_PRIMARY);

        JLabel updateTime = new JLabel("Updated: " + java.time.LocalDateTime.now().format(UPDATE_TIME_FMT));
        updateTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        updateTime.setForeground(UI_Constants.TEXT_SECONDARY);
        updateTime.setHorizontalAlignment(SwingConstants.RIGHT);

        headerRow.add(statusTitle, BorderLayout.WEST);
        headerRow.add(updateTime, BorderLayout.EAST);
        outer.add(headerRow, BorderLayout.NORTH);

        // 实时读取
        var userApps = dataService.getUserApplications();
        var openJobs = dataService.getOpenJobs();
        Map<String, Long> statusCount = userApps.stream()
            .collect(Collectors.groupingBy(Page_Dashboard::normalizeStatusBucket, Collectors.counting()));

        long pending    = statusCount.getOrDefault("pending", 0L);
        long inReview   = statusCount.getOrDefault("under_review", 0L);
        long accepted   = statusCount.getOrDefault("accepted", 0L);
        long rejected   = statusCount.getOrDefault("rejected", 0L);
        long cancelled  = statusCount.getOrDefault("cancelled", 0L);
        long other      = statusCount.getOrDefault("other", 0L);
        long total      = userApps.size() - cancelled;
        long openCount  = openJobs.size();

        // 各卡片的次要提示信息（分项之和与 total 一致）
        String appSummary = "Pending " + pending + " · In review " + inReview
            + " · Accepted " + accepted + " · Rejected " + rejected
            + (cancelled > 0 ? " · Cancelled " + cancelled : "")
            + (other > 0 ? " · Other " + other : "");

        String cvHint;
        TAUser.CV cv = currentUser.getCv();
        if (cv.isUploaded() && cv.getOriginalFileName() != null) {
            cvHint = "File: " + truncate(cv.getOriginalFileName(), 30);
        } else if (cv.isUploaded()) {
            cvHint = "CV ready to attach";
        } else {
            cvHint = "No CV on record";
        }

        // 开放职位提示（最接近截止的职位）
        String jobHint = openCount == 0
            ? "No open positions at the moment"
            : nextDeadlineHint(openJobs);

        JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 140));

        row.add(buildStatusCell("\u2713", new Color(220, 252, 231), new Color(22, 163, 74),
            "Profile Completion",
            currentUser.getProfileCompletion() + "%",
            currentUser.getProfileCompletion() == 100
                ? "Profile fully complete"
                : "Fill in remaining fields to reach 100%"));

        String appsSub = total == 0
            ? cvHint + "\n" + appSummary
            : cvHint + "\n" + latestAppInfo(userApps) + "\n" + appSummary;
        row.add(buildStatusCell("\u2191", new Color(219, 234, 254), new Color(37, 99, 235),
            "CV & Applications",
            total + " application" + (total == 1 ? "" : "s"),
            appsSub));

        row.add(buildStatusCell("\uD83D\uDCC4", new Color(237, 233, 254), new Color(109, 40, 217),
            "Open Positions",
            openCount + " job" + (openCount == 1 ? "" : "s") + " available",
            jobHint));

        outer.add(row, BorderLayout.CENTER);
        return outer;
    }

    private String latestAppInfo(java.util.List<Application> apps) {
        if (apps.isEmpty()) return "No applications yet";
        Application latest = apps.get(0);
        String jobId = latest.getJobId();
        Job job = dataService.getJobById(jobId);
        String course = job != null ? job.getCourseCode() : jobId;
        String status  = latest.getStatus().getLabel();
        return "Last: " + course + " (" + status + ")";
    }

    private String nextDeadlineHint(java.util.List<Job> jobs) {
        LocalDate today = LocalDate.now();
        LocalDate nearest = null;
        for (Job j : jobs) {
            if (j.getDates() != null && j.getDates().getDeadline() != null) {
                try {
                    LocalDate dl = LocalDate.parse(j.getDates().getDeadline().substring(0, 10));
                    if (nearest == null || dl.isBefore(nearest)) nearest = dl;
                } catch (Exception ignored) { }
            }
        }
        if (nearest == null) return "Check individual listings for deadlines";
        long days = ChronoUnit.DAYS.between(today, nearest);
        String when = days < 0 ? "Closed"
                    : days == 0 ? "Today"
                    : days == 1 ? "Tomorrow"
                    : "in " + days + " days";
        return "Next deadline " + when + " (" + nearest.format(DTF) + ")";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "\u2026";
    }

    private JPanel buildStatusCell(String symbol, Color chipBg, Color symbolColor,
                                   String label, String value, String subline) {
        JPanel cell = new JPanel(new BorderLayout(0, 0));
        cell.setOpaque(false);
        cell.setBorder(new EmptyBorder(10, 12, 14, 12));

        final int chipSize = 60;
        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(chipBg);
                int w = getWidth();
                int h = getHeight();
                int d = Math.min(w, h) - 2;
                int x = (w - d) / 2;
                int y = (h - d) / 2;
                g2.fillOval(x, y, d, d);
            }
        };
        Dimension chipDim = new Dimension(chipSize, chipSize);
        chip.setPreferredSize(chipDim);
        chip.setMinimumSize(chipDim);
        chip.setMaximumSize(chipDim);
        chip.setOpaque(false);
        chip.setLayout(new GridBagLayout());
        JLabel sym = new JLabel(symbol, SwingConstants.CENTER);
        sym.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        sym.setForeground(symbolColor);
        chip.add(sym);

        JPanel chipWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chipWrap.setOpaque(false);
        chipWrap.add(chip);

        JPanel textCol = new JPanel(new BorderLayout(0, 4));
        textCol.setOpaque(false);

        JLabel lab = new JLabel(label);
        lab.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lab.setForeground(UI_Constants.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(UI_Constants.TEXT_PRIMARY);

        JTextArea sub = new JTextArea(subline == null ? "" : subline);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0x9CA3AF));
        sub.setOpaque(false);
        sub.setEditable(false);
        sub.setFocusable(false);
        sub.setLineWrap(true);
        sub.setWrapStyleWord(true);
        sub.setBorder(BorderFactory.createEmptyBorder());
        sub.setHighlighter(null);
        sub.setRows(0);

        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setOpaque(false);
        topStack.add(lab);
        topStack.add(Box.createVerticalStrut(4));
        topStack.add(val);
        textCol.add(topStack, BorderLayout.NORTH);
        textCol.add(sub, BorderLayout.CENTER);

        JPanel inner = new JPanel(new BorderLayout(16, 0));
        inner.setOpaque(false);
        inner.add(chipWrap, BorderLayout.WEST);
        inner.add(textCol, BorderLayout.CENTER);
        cell.add(inner, BorderLayout.CENTER);
        return cell;
    }

    static final class RoundedPanel extends JPanel {
        private final int arc;
        private final Color stroke;

        RoundedPanel(int arc, Color stroke) {
            super(new BorderLayout());
            this.arc = arc;
            this.stroke = stroke;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(UI_Constants.CARD_BG);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, w, h, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
