

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;



/**
 * 职位列表页面
 * 显示所有可申请的职位，支持搜索和筛选
 */
public class Page_Jobs {

    private static final String SEARCH_PLACEHOLDER =
            "Search by job title or course code...";
    
    public interface JobsCallback {
        void onViewJobDetail(Job job);
        void onGoToApplications();
        void onGoToHome();
    }
    
    private JPanel panel;
    private DataService dataService;
    private JTextField searchField;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> jobTypeFilter;
    private JPanel jobsListPanel;
    private List<Job> filteredJobs;
    private JLabel jobListCountLabel;
    private JobsCallback callback;
    
    public Page_Jobs(DataService dataService, JobsCallback callback) {
        this.dataService = dataService;
        this.callback = callback;
        this.filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        initPanel();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public void refreshJobs() {
        filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        filterJobs();
    }
    
    private void initPanel() {
        panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(16, 48, 32, 48));
        
        buildHeader();
        buildJobsList();
    }
    
    private void buildHeader() {
        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);
        
        // Back button
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        JButton backHome = new JButton("\u2190 Back to Home");
        backHome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backHome.setForeground(UI_Constants.TEXT_SECONDARY);
        backHome.setContentAreaFilled(false);
        backHome.setBorder(new EmptyBorder(0, 0, 16, 0));
        backHome.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backHome.addActionListener(e -> callback.onGoToHome());
        backRow.add(backHome);
        northStack.add(backRow);
        
        // Title row
        JPanel titleRow = new JPanel(new BorderLayout(24, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel titleLeft = new JPanel(new BorderLayout(0, 6));
        titleLeft.setOpaque(false);
        JLabel titleLabel = new JLabel("Available Jobs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLeft.add(titleLabel, BorderLayout.NORTH);
        JLabel subtitleLabel = new JLabel("Browse all open TA positions.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        titleLeft.add(subtitleLabel, BorderLayout.SOUTH);
        titleRow.add(titleLeft, BorderLayout.WEST);
        
        JButton myAppsBtn = createJobsOutlineButton("My Applications");
        myAppsBtn.addActionListener(e -> callback.onGoToApplications());
        JPanel myAppsWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        myAppsWrap.setOpaque(false);
        myAppsWrap.add(myAppsBtn);
        titleRow.add(myAppsWrap, BorderLayout.EAST);
        northStack.add(titleRow);
        
        // Search card
        JPanel searchCard = new JPanel(new BorderLayout(16, 0));
        searchCard.setBackground(UI_Constants.CARD_BG);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(14, 18, 14, 18)
        ));
        
        JPanel searchWithIcon = new JPanel(new BorderLayout(10, 0));
        searchWithIcon.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setForeground(UI_Constants.TEXT_SECONDARY);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(UI_Constants.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(SEARCH_PLACEHOLDER);
                    searchField.setForeground(UI_Constants.TEXT_SECONDARY);
                }
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterJobs(); }
            public void removeUpdate(DocumentEvent e) { filterJobs(); }
            public void insertUpdate(DocumentEvent e) { filterJobs(); }
        });
        searchWithIcon.add(searchField, BorderLayout.CENTER);
        
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filters.setOpaque(false);
        JLabel funnel = new JLabel("\u25BC ");
        funnel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        funnel.setForeground(UI_Constants.TEXT_SECONDARY);
        filters.add(funnel);
        departmentFilter = new JComboBox<>(new String[]{"All Departments", "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology"});
        styleComboBox(departmentFilter);
        departmentFilter.addActionListener(e -> filterJobs());
        filters.add(departmentFilter);
        jobTypeFilter = new JComboBox<>(new String[]{"All Job Types", "TA", "Lab TA", "Grading TA", "Part-time TA"});
        styleComboBox(jobTypeFilter);
        jobTypeFilter.addActionListener(e -> filterJobs());
        filters.add(jobTypeFilter);
        
        searchCard.add(searchWithIcon, BorderLayout.CENTER);
        searchCard.add(filters, BorderLayout.EAST);
        northStack.add(searchCard);
        
        jobListCountLabel = new JLabel(" ");
        jobListCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobListCountLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        jobListCountLabel.setHorizontalAlignment(SwingConstants.LEFT);
        jobListCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobListCountLabel.setBorder(new EmptyBorder(12, 0, 16, 0));
        // 垂直 BoxLayout 易把窄行水平居中；用 X 轴 + 右侧 glue 占满宽度，保证文字贴左
        JPanel countRow = new JPanel();
        countRow.setLayout(new BoxLayout(countRow, BoxLayout.X_AXIS));
        countRow.setOpaque(false);
        countRow.add(jobListCountLabel);
        countRow.add(Box.createHorizontalGlue());
        countRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension countPref = countRow.getPreferredSize();
        countRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, countPref.height));
        northStack.add(countRow);
        
        panel.add(northStack, BorderLayout.NORTH);
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(UI_Constants.TEXT_PRIMARY);
        combo.setBackground(UI_Constants.CARD_BG);
        combo.setPreferredSize(new Dimension(168, 36));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
    }
    
    private JButton createJobsOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(UI_Constants.TEXT_PRIMARY);
        btn.setBackground(UI_Constants.CARD_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(10, 18, 10, 18)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(UI_Constants.BG_COLOR);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(UI_Constants.CARD_BG);
            }
        });
        return btn;
    }
    
    private void buildJobsList() {
        jobsListPanel = new JPanel();
        jobsListPanel.setLayout(new BoxLayout(jobsListPanel, BoxLayout.Y_AXIS));
        jobsListPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(jobsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshJobsList();
    }
    
    private String getEffectiveSearchText() {
        String t = searchField.getText();
        if (SEARCH_PLACEHOLDER.equals(t)) {
            return "";
        }
        return t.toLowerCase();
    }

    private void filterJobs() {
        filteredJobs.clear();
        String search = getEffectiveSearchText();
        String dept = (String) departmentFilter.getSelectedItem();
        String type = (String) jobTypeFilter.getSelectedItem();
        
        for (Job job : dataService.getOpenJobs()) {
            boolean match = true;
            
            if (!search.isEmpty()) {
                String title = job.getTitle().toLowerCase();
                String course = job.getCourseCode().toLowerCase();
                if (!title.contains(search) && !course.contains(search)) {
                    match = false;
                }
            }
            
            if (dept != null && !dept.equals("All Departments")) {
                if (!job.getDepartment().equals(dept)) {
                    match = false;
                }
            }
            
            if (type != null && !type.equals("All Job Types")) {
                if (!job.getEmploymentType().contains(type)) {
                    match = false;
                }
            }
            
            if (match) {
                filteredJobs.add(job);
            }
        }
        
        refreshJobsList();
    }
    
    private void refreshJobsList() {
        jobsListPanel.removeAll();
        
        int total = dataService.getOpenJobs().size();
        int shown = filteredJobs.size();
        if (jobListCountLabel != null) {
            jobListCountLabel.setText("Showing " + shown + " of " + total + " positions");
        }
        
        if (filteredJobs.isEmpty()) {
            JLabel emptyLabel = new JLabel("No jobs found matching your criteria");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(UI_Constants.TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            jobsListPanel.add(emptyLabel);
        } else {
            for (Job job : filteredJobs) {
                jobsListPanel.add(createJobCard(job));
                jobsListPanel.add(Box.createVerticalStrut(20));
            }
        }
        // 视口高于内容时 JViewport 会拉高列表面板；底部 glue 吃掉多余高度，避免单条职位卡片被纵向撑满
        jobsListPanel.add(Box.createVerticalGlue());
        
        jobsListPanel.revalidate();
        jobsListPanel.repaint();
    }
    
    private String formatDeadline(Job job) {
        String raw = job.getDeadlineDisplay();
        if (raw == null || raw.length() < 10) {
            return raw != null ? raw : "";
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
    
    private JPanel createJobCard(Job job) {
        JPanel card = new JPanel(new BorderLayout(28, 0));
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(22, 26, 22, 26)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        
        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleLabel);
        
        left.add(Box.createVerticalStrut(6));
        String meta = job.getCourseCode() + "  \u2022  " + job.getDepartment() + "  \u2022  " + job.getInstructorName();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(metaLabel);
        
        left.add(Box.createVerticalStrut(8));
        String summary = job.getSummary();
        if (summary == null || summary.isEmpty()) {
            summary = job.getDescription();
        }
        if (summary != null && summary.length() > 120) {
            summary = summary.substring(0, 117) + "...";
        }
        JLabel sumLabel = new JLabel("<html><div style='width:720px'>" + (summary != null ? summary : "") + "</div></html>");
        sumLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sumLabel.setForeground(new Color(75, 85, 99));
        sumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(sumLabel);
        
        left.add(Box.createVerticalStrut(14));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(createMetaChip("Hours:", job.getWeeklyHoursDisplay()));
        footer.add(createMetaChip("Deadline:", formatDeadline(job)));
        footer.add(createMetaChip("Location:", job.getLocationMode()));
        left.add(footer);
        
        // NORTH：卡片若被拉高时只拉伸下方空白，不把摘要区撑成大片留白
        card.add(left, BorderLayout.NORTH);
        
        JButton viewBtn = UI_Helper.createDarkButton("View Details  >");
        viewBtn.setPreferredSize(new Dimension(160, 44));
        viewBtn.addActionListener(e -> callback.onViewJobDetail(job));
        JPanel eastWrap = new JPanel(new BorderLayout());
        eastWrap.setOpaque(false);
        eastWrap.add(viewBtn, BorderLayout.NORTH);
        card.add(eastWrap, BorderLayout.EAST);
        
        return card;
    }
    
    private JLabel createMetaChip(String icon, String text) {
        JLabel l = new JLabel(icon + "  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(UI_Constants.TEXT_SECONDARY);
        return l;
    }
}
