

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;



/**
 * 职位详情页面：左侧分区卡片 + 右侧职位摘要，与整体设计稿对齐
 */
public class Page_JobDetail {
    
    public interface JobDetailCallback {
        void onBackToJobs();
        void onApply(Job job);
    }
    
    private static final int CARD_PAD = 20;
    private static final int CARD_GAP = 14;
    private static final int SUMMARY_WIDTH = 300;
    
    private JPanel panel;
    private JobDetailCallback callback;
    private DataService dataService;
    
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
        panel = UI_Helper.createPagePanel();
    }
    
    private void buildContent(Job job) {
        JButton backBtn = new JButton("\u2190 Back to Jobs");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setForeground(UI_Constants.TEXT_SECONDARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 8, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> callback.onBackToJobs());
        panel.add(backBtn);
        
        JLabel pageTitle = new JLabel("Job Detail");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(pageTitle);
        
        JPanel mainRow = new JPanel(new BorderLayout(28, 0));
        mainRow.setOpaque(false);
        mainRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftCol.add(buildHeaderCard(job));
        leftCol.add(Box.createVerticalStrut(CARD_GAP));
        leftCol.add(buildTextSectionCard("Job Description", job.getDescription()));
        leftCol.add(Box.createVerticalStrut(CARD_GAP));
        leftCol.add(buildBulletSectionCard("Responsibilities", job.getResponsibilities()));
        leftCol.add(Box.createVerticalStrut(CARD_GAP));
        leftCol.add(buildBulletSectionCard("Requirements", job.getRequirements()));
        leftCol.add(Box.createVerticalStrut(CARD_GAP));
        leftCol.add(buildSkillsCard(job.getPreferredSkills()));
        
        mainRow.add(leftCol, BorderLayout.CENTER);
        mainRow.add(buildSummaryCard(job), BorderLayout.EAST);
        
        panel.add(mainRow);
    }
    
    /** 顶栏：标题 + 状态、课程与导师信息 */
    private JPanel buildHeaderCard(Job job) {
        JPanel card = createCardShell();
        card.setLayout(new BorderLayout(0, 12));
        
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        JLabel title = new JLabel(job.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UI_Constants.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);
        top.add(createStatusBadge(job.getStatus()), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        
        JPanel meta = new JPanel();
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.setOpaque(false);
        String courseLine = job.getCourseCode() + "  \u2022  " + job.getDepartment();
        JLabel line1 = new JLabel(courseLine);
        line1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        line1.setForeground(UI_Constants.TEXT_SECONDARY);
        line1.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.add(line1);
        meta.add(Box.createVerticalStrut(6));
        JLabel line2 = new JLabel(job.getInstructorName());
        line2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        line2.setForeground(UI_Constants.TEXT_PRIMARY);
        line2.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.add(line2);
        String email = job.getInstructorEmail();
        if (email != null && !email.isEmpty()) {
            meta.add(Box.createVerticalStrut(4));
            JLabel em = new JLabel(email);
            em.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            em.setForeground(UI_Constants.TEXT_SECONDARY);
            em.setAlignmentX(Component.LEFT_ALIGNMENT);
            meta.add(em);
        }
        card.add(meta, BorderLayout.CENTER);
        
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
    
    private JLabel createStatusBadge(String rawStatus) {
        String s = rawStatus == null || rawStatus.isEmpty() ? "open" : rawStatus.trim();
        String label = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        boolean open = "open".equalsIgnoreCase(s);
        Color bg = open ? new Color(209, 250, 229) : new Color(243, 244, 246);
        Color fg = open ? new Color(5, 122, 85) : UI_Constants.TEXT_SECONDARY;
        JLabel badge = new JLabel("  " + label + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(fg);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(open ? new Color(167, 243, 208) : UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return badge;
    }
    
    private JPanel buildTextSectionCard(String heading, String body) {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(sectionHeading(heading));
        inner.add(Box.createVerticalStrut(10));
        String text = body == null ? "" : body;
        JLabel desc = new JLabel("<html><body style='width:560px'>" + escapeHtml(text) + "</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(UI_Constants.TEXT_PRIMARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(desc);
        
        JPanel card = wrapInCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
    
    private JPanel buildBulletSectionCard(String heading, List<String> items) {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(sectionHeading(heading));
        inner.add(Box.createVerticalStrut(10));
        if (items == null || items.isEmpty()) {
            JLabel empty = new JLabel("None specified.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(UI_Constants.TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            inner.add(empty);
        } else {
            for (String line : items) {
                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel bullet = new JLabel("\u2022");
                bullet.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                bullet.setForeground(UI_Constants.TEXT_SECONDARY);
                row.add(bullet, BorderLayout.WEST);
                JLabel text = new JLabel("<html><body style='width:520px'>" + escapeHtml(line) + "</body></html>");
                text.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                text.setForeground(UI_Constants.TEXT_PRIMARY);
                row.add(text, BorderLayout.CENTER);
                inner.add(row);
                inner.add(Box.createVerticalStrut(6));
            }
        }
        JPanel card = wrapInCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
    
    private JPanel buildSkillsCard(List<String> skills) {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.add(sectionHeading("Preferred Skills"));
        inner.add(Box.createVerticalStrut(10));
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        flow.setOpaque(false);
        flow.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (skills != null) {
            for (String skill : skills) {
                flow.add(UI_Helper.createSkillTag(skill));
            }
        }
        inner.add(flow);
        JPanel card = wrapInCard(inner);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }
    
    private JPanel buildSummaryCard(Job job) {
        JPanel card = createCardShell();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(SUMMARY_WIDTH, 0));
        card.setMinimumSize(new Dimension(SUMMARY_WIDTH, 200));
        card.setMaximumSize(new Dimension(SUMMARY_WIDTH, Integer.MAX_VALUE));
        
        JLabel summaryTitle = new JLabel("Position Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        summaryTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(summaryTitle);
        card.add(Box.createVerticalStrut(18));
        
        addSummaryBlock(card, "Employment Type", job.getEmploymentType());
        addSummaryBlock(card, "Weekly Hours", job.getWeeklyHoursDisplay());
        addSummaryBlock(card, "Application Deadline", formatDeadline(job.getDeadlineDisplay()));
        addSummaryBlock(card, "Location / Mode", buildLocationSummary(job));
        
        card.add(Box.createVerticalGlue());
        boolean alreadyApplied = dataService != null && dataService.hasAppliedToJob(job.getJobId());
        JButton applyBtn;
        if (alreadyApplied) {
            applyBtn = UI_Helper.createSecondaryButton("Already Applied");
            applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            applyBtn.setEnabled(false);
        } else {
            applyBtn = UI_Helper.createPrimaryButton("Apply Now");
            applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            applyBtn.addActionListener(e -> callback.onApply(job));
        }
        applyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        card.add(Box.createVerticalStrut(12));
        card.add(applyBtn);
        
        return card;
    }
    
    private void addSummaryBlock(JPanel card, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UI_Constants.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createVerticalStrut(4));
        JLabel v = new JLabel(value == null || value.isEmpty() ? "\u2014" : value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));
        v.setForeground(UI_Constants.TEXT_PRIMARY);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(v);
        card.add(Box.createVerticalStrut(16));
    }
    
    private JLabel sectionHeading(String text) {
        JLabel h = new JLabel(text);
        h.setFont(new Font("Segoe UI", Font.BOLD, 15));
        h.setForeground(UI_Constants.TEXT_PRIMARY);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        return h;
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
        } catch (NumberFormatException ignored) { }
        return raw;
    }
    
    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
