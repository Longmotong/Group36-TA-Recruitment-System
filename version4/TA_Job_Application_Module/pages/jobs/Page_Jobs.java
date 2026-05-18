package TA_Job_Application_Module.pages.jobs;

import TA_Job_Application_Module.model.Job;
import TA_Job_Application_Module.model.JobMatchResult;
import TA_Job_Application_Module.model.TAUser;
import TA_Job_Application_Module.service.DataService;
import TA_Job_Application_Module.service.ai.DoubaoAIService;
import TA_Job_Application_Module.ui.UI_Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;




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
    private JScrollPane jobsListScrollPane;
    /** Holds exactly one of: jobs scroll / AI ranking / AI job detail (single child avoids overlap with CardLayout on some LAFs). */
    private JPanel mainCenterStack;
    private List<Job> filteredJobs;
    private List<Job> sortedJobs;
    private JLabel jobListCountLabel;
    private JobsCallback callback;
    private JButton sortByMatchBtn;
    private JButton resetAnalysisBtn;
    private boolean isSortedByMatch = false;
    
    // AI Analysis caching
    private List<JobMatchResult> cachedMatchResults = null;
    private volatile boolean isAnalyzing = false;
    private String apiKey;

    // 流式分析状态
    private JPanel aiResultsPanel;           // AI结果面板（内嵌）
    private JPanel aiResultsContentPanel;    // 结果内容区域
    private JScrollPane aiResultsScrollPane; // 结果滚动窗格
    private JLabel aiStatusLabel;            // 状态标签
    private ColorProgressBar aiProgressBar;  // 自定义彩色进度条
    private JLabel aiProgressLabel;          // 进度标签
    private JButton aiCloseBtn;              // 关闭按钮
    private JButton aiViewDetailsBtn;        // 查看详情按钮
    private List<JobMatchResult> streamingResults = new ArrayList<>();
    private int streamingCurrentIndex = 0;
    private int streamingTotalJobs = 0;

    // AI排名详情页面
    private JPanel aiRankingDetailPanel;     // 排名详情面板
    private JScrollPane aiRankingDetailScrollPane; // 排名列表滚动（打开时滚回顶部）
    private List<JobMatchResult> allMatchResults; // 所有匹配结果
    private JPanel aiJobDetailPanel; // 职位详细报告面板
    private JPanel aiJobDetailContent; // 详细报告内容
    private JScrollPane aiJobDetailScrollPane; // AI详情页滚动面板（用于滚动到顶部）

    public Page_Jobs(DataService dataService, JobsCallback callback) {
        this.dataService = dataService;
        this.callback = callback;
        this.filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        this.sortedJobs = new ArrayList<>();
        initPanel();
    }
    
    public void startBackgroundAnalysis(String apiKey) {
        this.apiKey = apiKey;
        if (isAnalyzing || cachedMatchResults != null) {
            return;
        }
        
        new Thread(() -> {
            isAnalyzing = true;
            try {
                String userProfile = buildUserProfile();
                List<JobMatchResult> matchResults = new ArrayList<>();
                DoubaoAIService aiService = new DoubaoAIService(apiKey);

                for (Job job : filteredJobs) {
                    try {
                        String jobTitle = job.getTitle() != null ? job.getTitle() : "";
                        String courseCode = job.getCourseCode() != null ? job.getCourseCode() : "";
                        String department = job.getDepartment() != null ? job.getDepartment() : "";
                        String jobSummary = job.getSummary() != null ? job.getSummary() : "";
                        String requirements = "";
                        if (job.getContent() != null && job.getContent().getRequirements() != null) {
                            requirements = String.join(", ", job.getContent().getRequirements());
                        }
                        String preferredSkills = "";
                        if (job.getContent() != null && job.getContent().getPreferredSkills() != null) {
                            preferredSkills = String.join(", ", job.getContent().getPreferredSkills());
                        }

                        String result = aiService.analyzeJobMatch(jobTitle, jobSummary + " | Requirements: " + requirements,
                            courseCode, department, preferredSkills, userProfile, "", "");

                        double score = JobsAnalysisParser.extractMatchScore(result);
                        matchResults.add(new JobMatchResult(job, score, result));
                    } catch (Exception e) {
                        matchResults.add(new JobMatchResult(job, 0, "Analysis failed: " + e.getMessage()));
                    }
                }

                matchResults.sort((a, b) -> Double.compare(b.score, a.score));
                cachedMatchResults = matchResults;
                
                // 同步保存到 DataService 的全局缓存
                dataService.setCachedAIResults(new ArrayList<>(matchResults));
                
                // Update UI to show AI button is ready
                SwingUtilities.invokeLater(() -> {
                    sortByMatchBtn.setText("AI Smart Match \u2713");
                    sortByMatchBtn.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isAnalyzing = false;
            }
        }).start();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public void refreshJobs() {
        List<Job> previousJobs = new ArrayList<>(filteredJobs);
        filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        filterJobs();
        
        // 如果之前有 AI 排序的结果，需要更新 sortedJobs 以匹配新的 filteredJobs
        if (isSortedByMatch && cachedMatchResults != null && !cachedMatchResults.isEmpty()) {
            // 保留 AI 排序状态，只是更新 filteredJobs 的引用
            // 需要确保 sortedJobs 与新的 filteredJobs 匹配
            sortedJobs.clear();
            for (Job job : filteredJobs) {
                // 查找这个职位在 cachedMatchResults 中的匹配结果
                JobMatchResult match = findMatchResult(job);
                if (match != null) {
                    sortedJobs.add(job);
                }
            }
            // 添加没有匹配结果的职位
            for (Job job : filteredJobs) {
                if (!sortedJobs.contains(job)) {
                    sortedJobs.add(job);
                }
            }
            refreshJobsList();
        }
    }
    
    // 查找职位对应的匹配结果
    private JobMatchResult findMatchResult(Job job) {
        if (cachedMatchResults == null) return null;
        for (JobMatchResult result : cachedMatchResults) {
            if (result.job != null && result.job.equals(job)) {
                return result;
            }
        }
        return null;
    }
    
    /**
     * 恢复 AI 分析结果的显示状态
     * 在从其他页面返回时调用
     */
    public void restoreAIState() {
        // 如果 DataService 中有缓存的结果，加载并显示
        if (dataService.hasCachedAIResults()) {
            cachedMatchResults = dataService.getCachedAIResults();
            streamingResults = new ArrayList<>(cachedMatchResults);
            
            // 应用排序
            applySortedResults(cachedMatchResults);
            
            // 显示 AI 结果面板（进度条区域）
            if (aiResultsPanel != null) {
                aiResultsPanel.setVisible(true);
            }
            
            // 设置进度条为 100%
            if (aiProgressBar != null) {
                aiProgressBar.setValue(100);
                aiProgressBar.setString("100%");
            }
            if (aiStatusLabel != null) {
                aiStatusLabel.setText("Complete!");
            }
            
            // 显示前三名卡片
            updateCompleteUI();

            setJobsPageBottomStripVisible(true);
            installJobsListInCenter();
            if (northStack != null) {
                northStack.setVisible(true);
            }
            
            // 保持按钮状态
            if (sortByMatchBtn != null) {
                sortByMatchBtn.setText("AI Smart Match");
                sortByMatchBtn.repaint();
            }
            
            // 显示重置分析按钮
            if (resetAnalysisBtn != null) {
                resetAnalysisBtn.setVisible(true);
            }
        }
    }
    
    private void initPanel() {
        panel = new JPanel(new BorderLayout(0, 0)) {
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

                // 右上角浅紫点阵，仅作为视觉层，不参与布局计算
                g2.setColor(new Color(109, 77, 235, 18));
                int startX = Math.max(0, getWidth() - 240);
                for (int x = startX; x < getWidth() - 18; x += 10) {
                    for (int y = 0; y < 150; y += 10) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
                g2.dispose();
            }
        };
        panel.setOpaque(true);
        panel.setBackground(JobsPortalUi.PAGE_BG);
        panel.setBorder(new EmptyBorder(12, 40, 22, 40));

        buildHeader();
        buildJobsList();

        // 创建底部容器来放置AI相关面板
        bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);
        bottomContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        buildAIResultsPanel();
        buildAIRankingDetailPanel();

        panel.add(bottomContainer, BorderLayout.SOUTH);
    }

    private JPanel bottomContainer; // 底部容器，用于放置AI结果面板

    private void buildJobsList() {
        jobsListPanel = new JPanel();
        jobsListPanel.setLayout(new BoxLayout(jobsListPanel, BoxLayout.Y_AXIS));
        jobsListPanel.setOpaque(false);

        jobsListScrollPane = new JScrollPane(jobsListPanel);
        jobsListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jobsListScrollPane.setOpaque(false);
        jobsListScrollPane.getViewport().setOpaque(false);
        jobsListScrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        jobsListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jobsListScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        jobsListScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        jobsListScrollPane.getVerticalScrollBar().setBackground(new Color(248, 246, 255));

        mainCenterStack = new JPanel(new BorderLayout(0, 0));
        mainCenterStack.setOpaque(true);
        mainCenterStack.setBackground(JobsPortalUi.PAGE_BG);
        mainCenterStack.add(jobsListScrollPane, BorderLayout.CENTER);

        panel.add(mainCenterStack, BorderLayout.CENTER);

        refreshJobsList();
    }

    /** Exactly one center child: removes list vs ranking stacking issues on some platforms. */
    private void installCenterSingleton(Component view) {
        if (mainCenterStack == null || view == null) {
            return;
        }
        mainCenterStack.removeAll();
        mainCenterStack.add(view, BorderLayout.CENTER);
        view.setVisible(true);
        mainCenterStack.revalidate();
        mainCenterStack.repaint();
    }

    /** 立即把视口滚到顶部（需在布局有效后调用）。 */
    private void scrollVerticalToTop(JScrollPane sp) {
        if (sp == null) {
            return;
        }
        sp.doLayout();
        JViewport vp = sp.getViewport();
        if (vp != null) {
            Component v = vp.getView();
            if (v instanceof JComponent jc) {
                jc.doLayout();
                jc.scrollRectToVisible(new Rectangle(0, 0, Math.max(1, jc.getWidth()), Math.max(1, jc.getHeight())));
            } else if (v != null) {
                v.doLayout();
            }
            vp.setViewPosition(new Point(0, 0));
        }
        sp.getVerticalScrollBar().setValue(0);
        sp.getHorizontalScrollBar().setValue(0);
    }

    /**
     * 布局与焦点（如子按钮 requestFocus）会在同一 EDT 周期末尾再次改变滚动条；用短延迟再滚一次。
     */
    private void scheduleScrollToTop(JScrollPane sp) {
        if (sp == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> scrollVerticalToTop(sp));
        javax.swing.Timer t = new javax.swing.Timer(50, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            scrollVerticalToTop(sp);
        });
        t.setRepeats(false);
        t.start();
    }

    /** 切到 Jobs 路由或从子页返回时，由 {@link PortalNavigator} 调用，保证列表/排名从顶部开始。 */
    public void scrollJobsAndAiRankingToTop() {
        scheduleScrollToTop(jobsListScrollPane);
        scheduleScrollToTop(aiRankingDetailScrollPane);
        if (aiJobDetailScrollPane != null) {
            scheduleScrollToTop(aiJobDetailScrollPane);
        }
    }

    private void installJobsListInCenter() {
        installCenterSingleton(jobsListScrollPane);
        scheduleScrollToTop(jobsListScrollPane);
    }

    private void installAIRankingInCenter() {
        installCenterSingleton(aiRankingDetailPanel);
        scheduleScrollToTop(aiRankingDetailScrollPane);
    }

    private void installAIJobDetailInCenter() {
        installCenterSingleton(aiJobDetailPanel);
    }

    /** South strip (AI progress/cards) hidden while ranking or AI job detail uses full center area. */
    private void setJobsPageBottomStripVisible(boolean visible) {
        if (bottomContainer != null) {
            bottomContainer.setVisible(visible);
        }
    }

    /** Prefer live streaming buffer; fall back to cached results after analysis completes. */
    private List<JobMatchResult> resolveRankingSourceList() {
        if (streamingResults != null && !streamingResults.isEmpty()) {
            return new ArrayList<>(streamingResults);
        }
        if (cachedMatchResults != null && !cachedMatchResults.isEmpty()) {
            return new ArrayList<>(cachedMatchResults);
        }
        return null;
    }

    private void buildHeader() {
        northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.setOpaque(false);

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        JButton backHome = new JButton("←  Back to Home");
        backHome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backHome.setForeground(PRIMARY_PURPLE);
        backHome.setContentAreaFilled(false);
        backHome.setBorderPainted(false);
        backHome.setFocusPainted(false);
        backHome.setBorder(new EmptyBorder(0, 0, 8, 0));
        backHome.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backHome.addActionListener(e -> callback.onGoToHome());
        backRow.add(backHome);
        northStack.add(backRow);

        JPanel titleRow = new JPanel(new BorderLayout(12, 0));
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel titleCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleCluster.setOpaque(false);

        JLabel briefcaseIcon = new JLabel(JobsPortalUi.briefcaseGlyph(PRIMARY_PURPLE, 22));
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(briefcaseIcon, 16, LAVENDER_BG,
                LIGHT_PURPLE_BORDER, 1f, false, new Insets(8, 8, 8, 8));
        iconTile.setPreferredSize(new Dimension(46, 46));
        iconTile.setMinimumSize(new Dimension(46, 46));
        iconTile.setMaximumSize(new Dimension(46, 46));
        titleCluster.add(iconTile);

        JPanel titleText = new JPanel();
        titleText.setOpaque(false);
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Available Positions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(titleLabel);
        titleText.add(Box.createVerticalStrut(3));

        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(40, 3));
        underline.setMinimumSize(new Dimension(40, 3));
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(underline);
        titleText.add(Box.createVerticalStrut(4));

        JLabel subtitleLabel = new JLabel("Explore open Teaching Assistant opportunities across all departments");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(subtitleLabel);

        titleCluster.add(titleText);
        titleRow.add(titleCluster, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);

        JButton myAppsBtn = JobsPortalUi.outlineButton("My Applications", new Font("Segoe UI", Font.BOLD, 13));
        myAppsBtn.setIcon(JobsPortalUi.fileTextIcon(DEEP_PURPLE, 16));
        myAppsBtn.setIconTextGap(8);
        myAppsBtn.addActionListener(e -> callback.onGoToApplications());
        rightButtons.add(myAppsBtn);

        sortByMatchBtn = JobsPortalUi.aiGradientButton("AI Smart Match", new Font("Segoe UI", Font.BOLD, 13), JobsPortalUi.sparkleIcon(Color.WHITE, 16));
        sortByMatchBtn.setIconTextGap(8);
        sortByMatchBtn.addActionListener(e -> onAISmartSortClicked());
        rightButtons.add(sortByMatchBtn);

        resetAnalysisBtn = JobsPortalUi.roseHarmonyButton("Re-analyze", new Font("Segoe UI", Font.BOLD, 13));
        resetAnalysisBtn.setVisible(false);
        resetAnalysisBtn.addActionListener(e -> onResetAnalysisClicked());
        rightButtons.add(resetAnalysisBtn);

        titleRow.add(rightButtons, BorderLayout.EAST);
        northStack.add(titleRow);

        JobsPortalUi.RoundedSurface searchCard = new JobsPortalUi.RoundedSurface(
                16, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        JPanel searchCardInner = new JPanel(new BorderLayout());
        searchCardInner.setOpaque(false);
        searchCardInner.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel bottomInputs = new JPanel(new BorderLayout(12, 0));
        bottomInputs.setOpaque(false);

        JPanel searchFieldShell = new JPanel(new BorderLayout(8, 0));
        searchFieldShell.setOpaque(false);
        JLabel searchGlyph = new JLabel(JobsPortalUi.searchIcon(MUTED_TEXT, 16));
        searchFieldShell.add(searchGlyph, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        searchField.setOpaque(false);
        searchField.setBackground(new Color(0, 0, 0, 0));
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
        searchFieldShell.add(searchField, BorderLayout.CENTER);

        JPanel searchRounded = JobsPortalUi.wrapRoundedInner(searchFieldShell, 11, Color.WHITE,
                new Color(221, 226, 236), 1f, false, new Insets(6, 10, 6, 10));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        JPanel deptRow = new JPanel();
        deptRow.setLayout(new BoxLayout(deptRow, BoxLayout.X_AXIS));
        deptRow.setOpaque(false);
        deptRow.add(new JLabel(JobsPortalUi.buildingIcon(PRIMARY_PURPLE, 16)));
        deptRow.add(Box.createHorizontalStrut(6));
        departmentFilter = new JComboBox<>(new String[]{"All Departments", "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology"});
        styleComboBox(departmentFilter);
        departmentFilter.addActionListener(e -> filterJobs());
        deptRow.add(departmentFilter);
        deptRow.add(Box.createHorizontalStrut(4));
        deptRow.add(new JLabel(JobsPortalUi.chevronDownIcon(MUTED_TEXT, 12)));
        JPanel deptRounded = JobsPortalUi.wrapRoundedInner(deptRow, 11, Color.WHITE,
                new Color(221, 226, 236), 1f, false, new Insets(5, 10, 5, 10));

        JPanel jobTypeRow = new JPanel();
        jobTypeRow.setLayout(new BoxLayout(jobTypeRow, BoxLayout.X_AXIS));
        jobTypeRow.setOpaque(false);
        jobTypeRow.add(new JLabel(JobsPortalUi.briefcaseGlyph(MUTED_TEXT, 16)));
        jobTypeRow.add(Box.createHorizontalStrut(6));
        jobTypeFilter = new JComboBox<>(new String[]{"All Job Types", "TA", "Lab TA", "Grading TA", "Part-time TA"});
        styleComboBox(jobTypeFilter);
        jobTypeFilter.addActionListener(e -> filterJobs());
        jobTypeRow.add(jobTypeFilter);
        jobTypeRow.add(Box.createHorizontalStrut(4));
        jobTypeRow.add(new JLabel(JobsPortalUi.chevronDownIcon(MUTED_TEXT, 12)));
        JPanel jobTypeRounded = JobsPortalUi.wrapRoundedInner(jobTypeRow, 11, Color.WHITE,
                new Color(221, 226, 236), 1f, false, new Insets(5, 10, 5, 10));

        filters.add(deptRounded);
        filters.add(jobTypeRounded);

        bottomInputs.add(searchRounded, BorderLayout.CENTER);
        bottomInputs.add(filters, BorderLayout.EAST);

        searchCardInner.add(bottomInputs, BorderLayout.CENTER);
        searchCard.add(searchCardInner, BorderLayout.CENTER);
        northStack.add(searchCard);

        jobListCountLabel = new JLabel(" ");
        jobListCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jobListCountLabel.setForeground(MUTED_TEXT);
        jobListCountLabel.setHorizontalAlignment(SwingConstants.LEFT);
        jobListCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobListCountLabel.setBorder(new EmptyBorder(8, 0, 10, 0));

        JPanel countRow = new JPanel();
        countRow.setLayout(new BoxLayout(countRow, BoxLayout.X_AXIS));
        countRow.setOpaque(false);
        countRow.add(new JLabel(JobsPortalUi.listLinesIcon(PRIMARY_PURPLE, 16)));
        countRow.add(Box.createHorizontalStrut(6));
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
        combo.setForeground(DARK_TEXT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(188, 30));
        combo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        combo.setFocusable(false);
        combo.setOpaque(false);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton();
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setFocusable(false);
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                label.setBorder(new EmptyBorder(0, 2, 0, 2));
                if (!isSelected) {
                    label.setForeground(DARK_TEXT);
                    label.setBackground(Color.WHITE);
                }
                return label;
            }
        });
    }

    private JPanel portalMetaLine(javax.swing.Icon icon, String text) {
        return createMetaPill(icon, text, LAVENDER_BG, PRIMARY_PURPLE);
    }

    // 更新组件布局位置
    private void updateLayout() {
        panel.revalidate();
        panel.repaint();
    }

    private void hideAIResults() {
        isAnalyzing = false;
        if (aiResultsPanel != null) aiResultsPanel.setVisible(false);
        sortByMatchBtn.setText("AI Smart Match");
        sortByMatchBtn.repaint();
        updateLayout();
    }

    private void showAIResults() {
        if (aiResultsPanel != null) aiResultsPanel.setVisible(true);
        setJobsPageBottomStripVisible(true);
        updateLayout();
    }

    // 构建内嵌的AI结果面板
    private void buildAIResultsPanel() {
        aiResultsPanel = new JPanel();
        aiResultsPanel.setLayout(new BoxLayout(aiResultsPanel, BoxLayout.Y_AXIS));
        aiResultsPanel.setBackground(JobsPortalUi.PAGE_BG);
        aiResultsPanel.setVisible(false);

        JPanel bannerPanel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(88, 72, 148), w, 0, new Color(172, 152, 228)));
                g2.fillRoundRect(0, 0, w, h, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bannerPanel.setOpaque(false);
        bannerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        bannerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel bannerIcon = new JLabel("AI");
        bannerIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bannerIcon.setForeground(Color.WHITE);
        bannerPanel.add(bannerIcon, BorderLayout.WEST);

        // 进度条 - 使用自定义彩色进度条，不影响全局
        aiProgressBar = new ColorProgressBar();
        aiProgressBar.setProgressColor(JobsPortalUi.LAVENDER_LIGHT);
        aiProgressBar.setTrackColor(JobsPortalUi.PURPLE_800);
        aiProgressBar.setStringPainted(true);
        bannerPanel.add(aiProgressBar, BorderLayout.CENTER);

        // 状态标签
        aiStatusLabel = new JLabel("");
        aiStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        aiStatusLabel.setForeground(Color.WHITE);
        bannerPanel.add(aiStatusLabel, BorderLayout.EAST);

        aiResultsPanel.add(bannerPanel);

        // 结果卡片区域 - 横向排列3个卡片
        aiResultsContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        aiResultsContentPanel.setBackground(JobsPortalUi.PAGE_BG);
        aiResultsContentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        aiResultsPanel.add(aiResultsContentPanel);

        // 按钮区域 - 放在卡片下方
        aiButtonPanel = new JPanel();
        aiButtonPanel.setLayout(new BoxLayout(aiButtonPanel, BoxLayout.X_AXIS));
        aiButtonPanel.setBackground(JobsPortalUi.PAGE_BG);
        aiButtonPanel.setBorder(new EmptyBorder(8, 0, 16, 0));
        aiButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        aiButtonPanel.setMinimumSize(new Dimension(0, 50));
        aiButtonPanel.setPreferredSize(new Dimension(0, 50));
        aiButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        aiResultsPanel.add(aiButtonPanel);

        bottomContainer.add(aiResultsPanel);
    }

    private JPanel aiButtonPanel; // 按钮面板

    private String getEffectiveSearchText() {
        String t = searchField.getText();
        if (SEARCH_PLACEHOLDER.equals(t)) {
            return "";
        }
        return t.toLowerCase();
    }

    private void filterJobs() {
        filteredJobs.clear();
        JobsFilterModel filterModel = new JobsFilterModel(
                getEffectiveSearchText(),
                (String) departmentFilter.getSelectedItem(),
                (String) jobTypeFilter.getSelectedItem()
        );

        for (Job job : dataService.getOpenJobs()) {
            if (filterModel.matches(job)) {
                filteredJobs.add(job);
            }
        }

        if (isSortedByMatch) {
            refreshSortedJobsWithAI();
        } else {
            refreshJobsList();
        }
    }

    private void refreshSortedJobsWithAI() {
        sortedJobs.clear();
        sortedJobs.addAll(filteredJobs);
        refreshJobsList();
    }
    
    private void refreshJobsList() {
        jobsListPanel.removeAll();
        
        int total = dataService.getOpenJobs().size();
        int shown = filteredJobs.size();
        if (jobListCountLabel != null) {
            String sortInfo = isSortedByMatch ? " (AI sorted by match)" : "";
            jobListCountLabel.setText("Showing " + shown + " of " + total + " positions" + sortInfo);
        }
        
        List<Job> jobsToShow = isSortedByMatch ? sortedJobs : filteredJobs;
        
        if (jobsToShow.isEmpty() && !isSortedByMatch) {
            JLabel emptyLabel = new JLabel("No jobs found matching your criteria");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(UI_Constants.TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            jobsListPanel.add(emptyLabel);
        } else if (jobsToShow.isEmpty()) {
            JLabel emptyLabel = new JLabel("No jobs to display");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(UI_Constants.TEXT_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            jobsListPanel.add(emptyLabel);
        } else {
            int index = 0;
            for (Job job : jobsToShow) {
                index++;
                jobsListPanel.add(createJobCard(job, index));
                jobsListPanel.add(Box.createVerticalStrut(20));
            }
        }

        jobsListPanel.revalidate();
        jobsListPanel.repaint();
    }

    private void onAISmartSortClicked() {
        String apiKey = getDoubaoAPIKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            JOptionPane.showMessageDialog(panel,
                "Please set your Doubao API key first.\n\n" +
                "You can set it via system property:\n" +
                "java -Ddoubao.api.key=YOUR_API_KEY ...\n\n" +
                "Or set environment variable:\n" +
                "ARK_API_KEY=YOUR_API_KEY",
                "API Key Required",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 如果 DataService 中有缓存的结果，直接显示
        if (dataService.hasCachedAIResults()) {
            cachedMatchResults = dataService.getCachedAIResults();
            streamingResults = new ArrayList<>(cachedMatchResults);
            applySortedResults(cachedMatchResults);
            showAIRankingDetail();
            return;
        }

        // 如果本地缓存存在，也直接显示
        if (cachedMatchResults != null) {
            streamingResults = new ArrayList<>(cachedMatchResults);
            applySortedResults(cachedMatchResults);
            showAIRankingDetail();
            return;
        }

        // 如果正在分析中，直接显示面板
        if (isAnalyzing) {
            showAIResults();
            return;
        }

        // 开始流式分析
        startStreamingAnalysis(apiKey);
    }
    
    /**
     * 重新分析按钮点击处理
     * 清除旧的分析结果，重新用当前用户信息进行分析
     */
    private void onResetAnalysisClicked() {
        String apiKey = getDoubaoAPIKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            JOptionPane.showMessageDialog(panel,
                "Please set your Doubao API key first.\n\n" +
                "You can set it via system property:\n" +
                "java -Ddoubao.api.key=YOUR_API_KEY ...\n\n" +
                "Or set environment variable:\n" +
                "ARK_API_KEY=YOUR_API_KEY",
                "API Key Required",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 清除缓存的 AI 结果
        cachedMatchResults = null;
        streamingResults.clear();
        dataService.clearCachedAIResults();
        isSortedByMatch = false;
        sortedJobs.clear();
        
        // 隐藏重置按钮
        if (resetAnalysisBtn != null) {
            resetAnalysisBtn.setVisible(false);
        }
        
        // 开始新的流式分析
        startStreamingAnalysis(apiKey);
    }

    /**
     * 流式分析：逐个分析职位，结果即时显示在页面下方
     */
    private void startStreamingAnalysis(String apiKey) {
        DoubaoAIService aiService = new DoubaoAIService(apiKey);
        String userProfile = buildUserProfile();

        // 准备要分析的工作列表
        List<Job> jobsToAnalyze = new ArrayList<>(filteredJobs);
        streamingTotalJobs = jobsToAnalyze.size();
        streamingCurrentIndex = 0;
        streamingResults.clear();

        // 清空之前的结果
        if (aiResultsContentPanel != null) {
            aiResultsContentPanel.removeAll();
        }

        // 初始化进度显示
        if (aiProgressBar != null) {
            aiProgressBar.setValue(0);
            aiProgressBar.setString("0%");
        }
        if (aiStatusLabel != null) aiStatusLabel.setText("0 / " + streamingTotalJobs);

        // 显示AI结果面板
        showAIResults();

        // 更新按钮状态
        isAnalyzing = true;
        sortByMatchBtn.setText("AI Analyzing...");
        sortByMatchBtn.repaint();

        // 在后台线程执行分析
        new Thread(() -> {
            analyzeJobsStreaming(aiService, userProfile, jobsToAnalyze);
        }).start();
    }

    // 使用线程池进行并发分析
    private static final int PARALLEL_THREADS = 3; // 并发线程数

    private void analyzeJobsStreaming(DoubaoAIService aiService, String userProfile, List<Job> jobsToAnalyze) {
        int totalJobs = jobsToAnalyze.size();
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(totalJobs);

        // 创建线程池
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(PARALLEL_THREADS);

        for (Job job : jobsToAnalyze) {
            if (!isAnalyzing) {
                // 用户取消分析
                executor.shutdownNow();
                return;
            }

            final int currentIndex = streamingCurrentIndex;
            executor.submit(() -> {
                try {
                    String jobTitle = job.getTitle() != null ? job.getTitle() : "";
                    String courseCode = job.getCourseCode() != null ? job.getCourseCode() : "";
                    String department = job.getDepartment() != null ? job.getDepartment() : "";
                    String jobSummary = job.getSummary() != null ? job.getSummary() : "";
                    String requirements = "";
                    if (job.getContent() != null && job.getContent().getRequirements() != null) {
                        requirements = String.join(", ", job.getContent().getRequirements());
                    }
                    String preferredSkills = "";
                    if (job.getContent() != null && job.getContent().getPreferredSkills() != null) {
                        preferredSkills = String.join(", ", job.getContent().getPreferredSkills());
                    }

                    String result = aiService.analyzeJobMatch(jobTitle, jobSummary + " | Requirements: " + requirements,
                        courseCode, department, preferredSkills, userProfile, "", "");

                    double score = JobsAnalysisParser.extractMatchScore(result);
                    JobMatchResult matchResult = new JobMatchResult(job, score, result);
                    synchronized (streamingResults) {
                        streamingResults.add(matchResult);
                        streamingResults.sort((a, b) -> Double.compare(b.score, a.score));
                    }

                    final JobMatchResult finalResult = matchResult;
                    SwingUtilities.invokeLater(() -> {
                        updateStreamingUI(currentIndex, finalResult);
                        updateProgressFromResults(totalJobs);
                    });

                } catch (Exception e) {
                    JobMatchResult matchResult = new JobMatchResult(job, 0, "Analysis failed: " + e.getMessage());
                    synchronized (streamingResults) {
                        streamingResults.add(matchResult);
                        streamingResults.sort((a, b) -> Double.compare(b.score, a.score));
                    }

                    final JobMatchResult finalResult = matchResult;
                    SwingUtilities.invokeLater(() -> {
                        updateStreamingUI(currentIndex, finalResult);
                        updateProgressFromResults(totalJobs);
                    });
                } finally {
                    latch.countDown();
                }
            });

            streamingCurrentIndex++;
        }

        // 等待所有任务完成
        new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 分析完成
            isAnalyzing = false;
            cachedMatchResults = new ArrayList<>(streamingResults);
            
            // 同步保存到 DataService 的全局缓存
            dataService.setCachedAIResults(new ArrayList<>(streamingResults));

            SwingUtilities.invokeLater(() -> {
                sortByMatchBtn.setText("AI Smart Match");
                sortByMatchBtn.repaint();

                // 显示重置分析按钮
                if (resetAnalysisBtn != null) {
                    resetAnalysisBtn.setVisible(true);
                }

                // 隐藏进度条，显示完成状态
                if (aiProgressBar != null) {
                    aiProgressBar.setValue(100);
                    aiProgressBar.setString("100%");
                }
                if (aiStatusLabel != null) {
                    aiStatusLabel.setText("Complete!");
                }

                // 更新卡片显示，添加View All按钮
                updateCompleteUI();
            });

            executor.shutdown();
        }).start();
    }

    // 根据结果数量更新进度
    private void updateProgressFromResults(int totalJobs) {
        int completed = streamingResults.size();
        int percent = (int) ((completed / (double) totalJobs) * 100);
        if (aiProgressBar != null) {
            aiProgressBar.setValue(percent);
            aiProgressBar.setString(percent + "%");
        }
        if (aiStatusLabel != null) {
            aiStatusLabel.setText(completed + " / " + totalJobs);
        }
    }

    private void updateCompleteUI() {
        if (aiResultsContentPanel == null) return;
        aiResultsContentPanel.removeAll();

        // 重新渲染前3名
        int displayCount = Math.min(3, streamingResults.size());
        for (int i = 0; i < displayCount; i++) {
            JobMatchResult r = streamingResults.get(i);
            JPanel resultCard = createHorizontalResultCard(r, i + 1);
            aiResultsContentPanel.add(resultCard);
        }

        // 添加View Ranking按钮到按钮面板（正下方中间）— 与主 CTA 同款渐变，字更清晰
        aiButtonPanel.removeAll();

        JobsPortalUi.PurpleGradientButton viewRankingBtn =
                JobsPortalUi.gradientButton("View Ranking \u2192", new Font("Segoe UI", Font.BOLD, 12), null);
        viewRankingBtn.addActionListener(e -> showAIRankingDetail());

        aiButtonPanel.add(Box.createHorizontalGlue());
        aiButtonPanel.add(viewRankingBtn);
        aiButtonPanel.add(Box.createHorizontalGlue());

        aiResultsContentPanel.revalidate();
        aiResultsContentPanel.repaint();
        aiButtonPanel.revalidate();
        aiButtonPanel.repaint();
    }

    private void updateStreamingUI(int index, JobMatchResult result) {
        if (aiResultsContentPanel == null) return;
        if (!aiResultsPanel.isVisible()) {
            return;
        }

        // 更新进度
        if (aiProgressBar != null) {
            int percent = (int) ((streamingCurrentIndex * 100.0) / streamingTotalJobs);
            aiProgressBar.setValue(percent);
            aiProgressBar.setString(percent + "%");
        }
        if (aiStatusLabel != null) {
            aiStatusLabel.setText(streamingCurrentIndex + " / " + streamingTotalJobs);
        }

        // 重新渲染前3名（横向排列）
        if (aiResultsContentPanel != null) {
            aiResultsContentPanel.removeAll();
            int displayCount = Math.min(3, streamingResults.size());
            for (int i = 0; i < displayCount; i++) {
                JobMatchResult r = streamingResults.get(i);
                JPanel resultCard = createHorizontalResultCard(r, i + 1);
                aiResultsContentPanel.add(resultCard);
            }
            aiResultsContentPanel.revalidate();
            aiResultsContentPanel.repaint();
        }

        // 更新按钮面板显示 "Analyzing..."
        aiButtonPanel.removeAll();
        JLabel analyzingLabel = new JLabel("Analyzing... ");
        analyzingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        analyzingLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        aiButtonPanel.add(Box.createHorizontalGlue());
        aiButtonPanel.add(analyzingLabel);
        aiButtonPanel.add(Box.createHorizontalGlue());
        aiButtonPanel.revalidate();
        aiButtonPanel.repaint();
    }

    // 创建垂直排列的结果卡片
    private JPanel createVerticalResultCard(JobMatchResult result, int rank) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // 顶部：排名和分数
        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        // 排名
        Color rankColor;
        String rankLabel;
        if (rank == 1) {
            rankColor = new Color(234, 179, 8);
            rankLabel = "#1";
        } else if (rank == 2) {
            rankColor = new Color(156, 163, 175);
            rankLabel = "#2";
        } else {
            rankColor = new Color(180, 119, 69);
            rankLabel = "#3";
        }

        JLabel rankBadge = new JLabel(rankLabel);
        rankBadge.setFont(new Font("Segoe UI", Font.BOLD, 20));
        rankBadge.setForeground(rankColor);
        topRow.add(rankBadge, BorderLayout.WEST);

        // 分数
        JLabel scoreLabel = new JLabel(String.format("%.0f%%", result.score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        Color scoreColor = getScoreColor(result.score);
        scoreLabel.setForeground(scoreColor);
        topRow.add(scoreLabel, BorderLayout.EAST);

        card.add(topRow);
        card.add(Box.createVerticalStrut(4));

        // 职位信息
        JLabel titleLabel = new JLabel(result.job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);

        JLabel courseLabel = new JLabel(result.job.getCourseCode());
        courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        courseLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        courseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(courseLabel);

        return card;
    }

    // 创建横向紧凑的结果卡片
    private JPanel createHorizontalResultCard(JobMatchResult result, int rank) {
        Color stripeColor;
        Color softFill;
        if (rank == 1) {
            stripeColor = new Color(251, 191, 36);
            softFill = new Color(255, 253, 246);
        } else if (rank == 2) {
            stripeColor = new Color(148, 163, 184);
            softFill = new Color(248, 250, 252);
        } else {
            stripeColor = new Color(217, 119, 6);
            softFill = new Color(255, 251, 245);
        }

        JPanel card = new JPanel(new BorderLayout(6, 0));
        card.setBackground(softFill);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JobsPortalUi.VIOLET_200, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        card.setPreferredSize(new Dimension(280, 70)); // 减小高度
        card.setMaximumSize(new Dimension(280, 70));
        card.setMinimumSize(new Dimension(280, 70));

        // 排名和分数
        Color rankColor;
        String rankLabel;
        if (rank == 1) {
            rankColor = new Color(234, 179, 8);
            rankLabel = "#1";
        } else if (rank == 2) {
            rankColor = new Color(156, 163, 175);
            rankLabel = "#2";
        } else {
            rankColor = new Color(180, 119, 69);
            rankLabel = "#3";
        }

        // 左侧：排名
        JPanel rankArea = new JPanel();
        rankArea.setLayout(new BoxLayout(rankArea, BoxLayout.Y_AXIS));
        rankArea.setOpaque(false);
        rankArea.setPreferredSize(new Dimension(40, 54));

        JLabel rankBadge = new JLabel(rankLabel);
        rankBadge.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rankBadge.setForeground(rankColor);
        rankBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        rankArea.add(rankBadge);

        JLabel matchLabel = new JLabel("MATCH");
        matchLabel.setFont(new Font("Segoe UI", Font.BOLD, 8));
        matchLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        matchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rankArea.add(matchLabel);

        card.add(rankArea, BorderLayout.WEST);

        // 中间：职位信息
        JPanel infoArea = new JPanel();
        infoArea.setLayout(new BoxLayout(infoArea, BoxLayout.Y_AXIS));
        infoArea.setOpaque(false);

        JLabel titleLabel = new JLabel(result.job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoArea.add(titleLabel);

        JLabel courseLabel = new JLabel(result.job.getCourseCode() + " • " + result.job.getDepartment());
        courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        courseLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        courseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoArea.add(courseLabel);

        card.add(infoArea, BorderLayout.CENTER);

        // 右侧：分数
        JPanel scoreArea = new JPanel();
        scoreArea.setLayout(new BoxLayout(scoreArea, BoxLayout.Y_AXIS));
        scoreArea.setOpaque(false);
        scoreArea.setPreferredSize(new Dimension(50, 54));

        JLabel scoreLabel = new JLabel(String.format("%.0f%%", result.score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        Color scoreColor = getScoreColor(result.score);
        scoreLabel.setForeground(scoreColor);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreArea.add(scoreLabel);

        JLabel scoreTextLabel = new JLabel("Score");
        scoreTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        scoreTextLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        scoreTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreArea.add(scoreTextLabel);

        card.add(scoreArea, BorderLayout.EAST);

        JPanel stripe = new JPanel();
        stripe.setBackground(stripeColor);
        stripe.setPreferredSize(new Dimension(5, 0));

        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setOpaque(false);
        outer.add(stripe, BorderLayout.WEST);
        outer.add(card, BorderLayout.CENTER);
        outer.setPreferredSize(new Dimension(285, 70));
        outer.setMaximumSize(new Dimension(285, 70));
        outer.setMinimumSize(new Dimension(285, 70));
        return outer;
    }

    // 显示AI排名详情页面
    private void showAIRankingDetail() {
        List<JobMatchResult> source = resolveRankingSourceList();
        if (source == null || source.isEmpty()) {
            return;
        }
        allMatchResults = source;

        // 隐藏AI结果面板（底部的3张卡片区域）
        if (aiResultsPanel != null) aiResultsPanel.setVisible(false);
        setJobsPageBottomStripVisible(false);

        if (northStack != null) northStack.setVisible(false);

        // 填充排名详情内容
        populateRankingDetailContent();

        aiRankingDetailPanel.setVisible(true);
        installAIRankingInCenter();
        panel.revalidate();
        panel.repaint();
    }

    private void populateRankingDetailContent() {
        aiRankingDetailContent.removeAll();

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridy = 0;
        gc.weighty = 0;
        gc.insets = new Insets(0, 0, 16, 0);
        aiRankingDetailContent.add(buildStatsBar(), gc);

        for (int i = 0; i < allMatchResults.size(); i++) {
            JobMatchResult result = allMatchResults.get(i);
            int rank = i + 1;
            JPanel card = createRankingDetailCard(result, rank);
            gc.gridy = i + 1;
            gc.insets = new Insets(0, 0, 12, 0);
            aiRankingDetailContent.add(card, gc);
        }

        aiRankingDetailContent.revalidate();
        aiRankingDetailContent.repaint();
    }

    private JPanel buildStatsBar() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        int totalJobs = allMatchResults.size();
        double avgScore = allMatchResults.stream().mapToDouble(r -> r.score).average().orElse(0);
        int highMatchCount = (int) allMatchResults.stream().filter(r -> r.score >= 70).count();

        statsPanel.add(createStatCard(
                String.valueOf(totalJobs),
                "Total Jobs Analyzed",
                LAVENDER_BG,
                LIGHT_PURPLE_BORDER,
                PRIMARY_PURPLE,
                JobsPortalUi.fileTextIcon(PRIMARY_PURPLE, 24)));
        statsPanel.add(createStatCard(
                String.format("%.0f%%", avgScore),
                "Average Match",
                new Color(232, 241, 255),
                new Color(188, 214, 255),
                BLUE_ACCENT,
                JobsPortalUi.lineChartIcon(BLUE_ACCENT, 25)));
        statsPanel.add(createStatCard(
                String.valueOf(highMatchCount),
                "High Match (>=70%)",
                new Color(224, 250, 237),
                new Color(178, 238, 209),
                new Color(16, 163, 105),
                JobsPortalUi.targetIcon(new Color(16, 163, 105), 25)));

        return statsPanel;
    }

    /** AI ranking statistic card: same rounded-card language as the job list, but compact. */
    private JPanel createStatCard(String value, String label, Color fill, Color stroke, Color valueColor, Icon icon) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, fill, stroke, 1f, true, new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inner = new JPanel(new BorderLayout(14, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(iconLabel, 14, new Color(255, 255, 255, 170),
                null, 0f, false, new Insets(10, 10, 10, 10));
        iconTile.setPreferredSize(new Dimension(48, 48));
        iconTile.setMinimumSize(new Dimension(48, 48));
        iconTile.setMaximumSize(new Dimension(48, 48));
        inner.add(iconTile, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(4));

        JLabel labelLabel = new JLabel("<html><div style='width:92px;font-size:12px;'>" + escapeHtmlSnippet(label) + "</div></html>");
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelLabel.setForeground(MUTED_TEXT);
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(labelLabel);

        inner.add(text, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private static String escapeHtmlSnippet(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static int clampPercentInt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return 0;
        }
        return (int) Math.max(0, Math.min(100, Math.round(v)));
    }

    private static String safeText(String s) {
        return s == null ? "" : s.trim();
    }

    private String buildJobMeta(Job job) {
        List<String> parts = new ArrayList<>();
        String cc = safeText(job.getCourseCode());
        String dept = safeText(job.getDepartment());
        String instr = safeText(job.getInstructorName());
        if (!cc.isBlank()) parts.add(cc);
        if (!dept.isBlank()) parts.add(dept);
        if (!instr.isBlank()) parts.add(instr);
        return String.join("  •  ", parts);
    }

    private String compactSummary(JobMatchResult result, int maxChars) {
        String text = JobsAnalysisParser.extractRankingSummaryPreview(result.analysis, maxChars + 30);
        if (text == null || text.isBlank()) {
            text = JobsAnalysisParser.extractBriefAnalysis(result.analysis);
        }
        if (text == null || text.isBlank()) {
            text = safeText(result.job.getSummary());
        }
        text = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (text.length() > maxChars) {
            text = text.substring(0, Math.max(0, maxChars - 3)) + "...";
        }
        return text;
    }

    private Color rankColor(int rank) {
        if (rank == 1) return new Color(221, 160, 0);
        if (rank == 2) return new Color(146, 157, 174);
        if (rank == 3) return new Color(211, 104, 16);
        return MUTED_TEXT;
    }

    private Color rankSoftColor(int rank) {
        if (rank == 1) return new Color(255, 250, 236);
        if (rank == 2) return new Color(245, 247, 250);
        if (rank == 3) return new Color(255, 247, 237);
        return new Color(247, 248, 252);
    }

    private JPanel createRankingDetailCard(JobMatchResult result, int rank) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints sx = new GridBagConstraints();
        sx.gridy = 0;
        sx.anchor = GridBagConstraints.NORTHWEST;
        sx.weighty = 0;

        JPanel rankTile = createRankTile(rank);
        sx.gridx = 0;
        sx.weightx = 0;
        sx.fill = GridBagConstraints.NONE;
        sx.insets = new Insets(0, 0, 0, 10);
        shell.add(rankTile, sx);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        sx.gridx = 1;
        sx.weightx = 1;
        sx.fill = GridBagConstraints.HORIZONTAL;
        sx.insets = new Insets(0, 0, 0, 10);
        shell.add(center, sx);

        String titleStr = safeText(result.job.getTitle()).isBlank() ? "Untitled Position" : safeText(result.job.getTitle());
        JTextArea titleArea = new JTextArea(titleStr);
        titleArea.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titleArea.setForeground(DARK_TEXT);
        titleArea.setRows(1);
        titleArea.setColumns(18);
        titleArea.setLineWrap(true);
        titleArea.setWrapStyleWord(true);
        titleArea.setEditable(false);
        titleArea.setFocusable(false);
        titleArea.setOpaque(false);
        titleArea.setBorder(null);
        titleArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(titleArea);
        center.add(Box.createVerticalStrut(4));

        JTextArea metaArea = new JTextArea(buildJobMeta(result.job));
        metaArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaArea.setForeground(MUTED_TEXT);
        metaArea.setRows(1);
        metaArea.setColumns(22);
        metaArea.setLineWrap(true);
        metaArea.setWrapStyleWord(true);
        metaArea.setEditable(false);
        metaArea.setFocusable(false);
        metaArea.setOpaque(false);
        metaArea.setBorder(null);
        metaArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(metaArea);
        center.add(Box.createVerticalStrut(8));

        JTextArea summaryArea = new JTextArea(compactSummary(result, 220));
        summaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryArea.setForeground(new Color(82, 90, 120));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setEditable(false);
        summaryArea.setFocusable(false);
        summaryArea.setOpaque(false);
        summaryArea.setBorder(null);
        summaryArea.setRows(2);
        summaryArea.setColumns(20);
        summaryArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(summaryArea);
        center.add(Box.createVerticalStrut(8));

        int skillsPct = clampPercentInt(JobsAnalysisParser.extractMatchScoreByType(result.analysis, "skillsMatch"));
        int gpaPct = clampPercentInt(JobsAnalysisParser.extractMatchScoreByType(result.analysis, "gpaMatch"));
        int expPct = clampPercentInt(JobsAnalysisParser.extractMatchScoreByType(result.analysis, "experienceMatch"));

        JPanel comparisonBlock = new JPanel();
        comparisonBlock.setLayout(new BoxLayout(comparisonBlock, BoxLayout.Y_AXIS));
        comparisonBlock.setOpaque(false);
        comparisonBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel comparisonTitle = new JLabel("Comparison:");
        comparisonTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comparisonTitle.setForeground(DARK_TEXT);
        comparisonTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        comparisonBlock.add(comparisonTitle);
        comparisonBlock.add(Box.createVerticalStrut(4));
        JPanel meterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        meterRow.setOpaque(false);
        meterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        meterRow.add(createMiniProgressBar("Skills", skillsPct, BLUE_ACCENT));
        meterRow.add(createMiniProgressBar("GPA", gpaPct, new Color(34, 197, 94)));
        meterRow.add(createMiniProgressBar("Exp", expPct, new Color(168, 85, 247)));
        comparisonBlock.add(meterRow);
        center.add(comparisonBlock);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        // 不要用小于渐变按钮 intrinsic 宽度的 preferred，否则会裁掉 pill 右侧圆角
        right.setMinimumSize(new Dimension(96, 96));
        right.setMaximumSize(new Dimension(200, 240));
        sx.gridx = 2;
        sx.weightx = 0;
        sx.fill = GridBagConstraints.NONE;
        sx.insets = new Insets(0, 0, 0, 8);
        shell.add(right, sx);

        GridBagConstraints rgc = new GridBagConstraints();
        rgc.gridx = 0;
        rgc.gridy = 0;
        rgc.anchor = GridBagConstraints.NORTH;
        rgc.insets = new Insets(0, 0, 12, 0);
        right.add(createScoreTile(result.score, false), rgc);
        rgc.gridy = 1;
        rgc.insets = new Insets(0, 0, 0, 0);
        JButton detailBtn = JobsPortalUi.gradientButton("Detail →", new Font("Segoe UI", Font.BOLD, 10), null);
        detailBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailBtn.addActionListener(e -> showJobDetailReport(result));
        right.add(detailBtn, rgc);

        card.add(shell, BorderLayout.CENTER);

        final JobMatchResult finalResult = result;
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showJobDetailReport(finalResult);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.repaint();
            }
        });

        return card;
    }

    private JPanel createRankTile(int rank) {
        Color accent = rankColor(rank);
        JobsPortalUi.RoundedSurface tile = new JobsPortalUi.RoundedSurface(
                16, rankSoftColor(rank), null, 0f, false, new BorderLayout());
        tile.setPreferredSize(new Dimension(100, 100));
        tile.setMinimumSize(new Dimension(96, 96));
        tile.setMaximumSize(new Dimension(108, 108));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(12, 0, 10, 0));

        JLabel medal = new JLabel(JobsPortalUi.medalIcon(accent, 28));
        medal.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(medal);
        inner.add(Box.createVerticalStrut(4));

        JLabel rankText = new JLabel("#" + rank);
        rankText.setFont(new Font("Segoe UI", Font.BOLD, 32));
        rankText.setForeground(accent);
        rankText.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(rankText);

        JLabel matchText = new JLabel("MATCH");
        matchText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        matchText.setForeground(MUTED_TEXT);
        matchText.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(matchText);

        tile.add(inner, BorderLayout.CENTER);
        return tile;
    }

    // 创建小型进度条
    private JPanel createMiniProgressBar(String label, int percentage, Color color) {
        int pct = clampPercentInt(percentage);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setPreferredSize(new Dimension(74, 52));
        panel.setMaximumSize(new Dimension(80, 64));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        labelText.setForeground(MUTED_TEXT);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelText);
        panel.add(Box.createVerticalStrut(3));

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = Math.max(4, getHeight());
                g2.setColor(new Color(226, 230, 236));
                g2.fillRoundRect(0, (getHeight() - h) / 2, getWidth(), h, h, h);
                int fillW = (int) Math.round(getWidth() * pct / 100.0);
                if (fillW > 0) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, (getHeight() - h) / 2, fillW, h, h, h);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(62, 7));
        bar.setMinimumSize(new Dimension(48, 6));
        bar.setMaximumSize(new Dimension(70, 10));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bar);
        panel.add(Box.createVerticalStrut(4));

        JLabel percentText = new JLabel(pct + "%");
        percentText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        percentText.setForeground(color);
        percentText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(percentText);

        return panel;
    }

    // 创建分数详情行（用于详情页面）
    private JPanel createScoreRow(String label, double percentage, Color color) {
        int pct = clampPercentInt(percentage);
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelText.setForeground(new Color(55, 65, 81));
        labelText.setPreferredSize(new Dimension(108, 24));
        labelText.setMinimumSize(new Dimension(88, 22));
        panel.add(labelText, BorderLayout.WEST);

        JPanel progressBar = createLargeProgressBar(pct, color);
        panel.add(progressBar, BorderLayout.CENTER);

        JLabel percentText = new JLabel(pct + "%");
        percentText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        percentText.setForeground(color);
        percentText.setPreferredSize(new Dimension(52, 24));
        percentText.setMinimumSize(new Dimension(44, 24));
        percentText.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(percentText, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLargeProgressBar(int percentage, Color color) {
        int pct = clampPercentInt(percentage);
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = 10;
                int y = (getHeight() - h) / 2;
                g2.setColor(new Color(226, 230, 236));
                g2.fillRoundRect(0, y, getWidth(), h, h, h);
                int fillW = (int) Math.round(getWidth() * pct / 100.0);
                if (fillW > 0) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, y, fillW, h, h, h);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setMinimumSize(new Dimension(48, 24));
        bar.setPreferredSize(new Dimension(120, 24));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return bar;
    }

    private JPanel createScoreTile(double score, boolean bordered) {
        Color color = getScoreColor(score);
        JPanel content = new JPanel(new BorderLayout(0, 3));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(4, 10, 4, 10));

        JLabel scoreLabel = new JLabel(String.format("%.0f%%", score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, bordered ? 32 : 30));
        scoreLabel.setForeground(color);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setMinimumSize(new Dimension(64, 34));
        content.add(scoreLabel, BorderLayout.NORTH);

        JLabel scoreTextLabel = new JLabel("Match Score");
        scoreTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        scoreTextLabel.setForeground(MUTED_TEXT);
        scoreTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(scoreTextLabel, BorderLayout.SOUTH);

        if (bordered) {
            return JobsPortalUi.wrapRoundedInner(content, 6, new Color(255, 255, 255, 0), color, 2f,
                    false, new Insets(12, 16, 12, 16));
        }
        return content;
    }

    /**
     * 与职位列表顶栏一致：首行紫色返回链接，次行大标题 + 灰色副标题 + 主题图标。
     */
    private JPanel buildAiSubpageHeader(String backLabel, Runnable onBack, String pageTitle, String subtitleText) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton backBtn = new JButton(backLabel);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(PRIMARY_PURPLE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(new EmptyBorder(0, 0, 14, 0));
        backBtn.setMargin(new Insets(0, 0, 0, 0));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> onBack.run());
        backRow.add(backBtn);
        wrap.add(backRow);

        JPanel titleCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        titleCluster.setOpaque(false);
        titleCluster.setAlignmentX(Component.LEFT_ALIGNMENT);
        Icon pageIcon = pageTitle != null && pageTitle.contains("Detail")
                ? JobsPortalUi.lineChartIcon(PRIMARY_PURPLE, 28)
                : JobsPortalUi.trophyIcon(PRIMARY_PURPLE, 28);
        JLabel iconLabel = new JLabel(pageIcon);
        JPanel iconTile = JobsPortalUi.wrapRoundedInner(iconLabel, 18, LAVENDER_BG,
                LIGHT_PURPLE_BORDER, 1f, false, new Insets(12, 12, 12, 12));
        iconTile.setPreferredSize(new Dimension(56, 56));
        iconTile.setMinimumSize(new Dimension(56, 56));
        iconTile.setMaximumSize(new Dimension(56, 56));
        titleCluster.add(iconTile);

        JPanel titleText = new JPanel();
        titleText.setOpaque(false);
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        JLabel titleLab = new JLabel(pageTitle);
        titleLab.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titleLab.setForeground(DARK_TEXT);
        titleLab.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(titleLab);
        titleText.add(Box.createVerticalStrut(6));
        JPanel underline = new JPanel();
        underline.setOpaque(true);
        underline.setBackground(PRIMARY_PURPLE);
        underline.setPreferredSize(new Dimension(52, 4));
        underline.setMinimumSize(new Dimension(52, 4));
        underline.setMaximumSize(new Dimension(52, 4));
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(underline);
        titleText.add(Box.createVerticalStrut(10));
        JLabel subLab = new JLabel(subtitleText);
        subLab.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subLab.setForeground(MUTED_TEXT);
        subLab.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleText.add(subLab);
        titleCluster.add(titleText);
        wrap.add(titleCluster);

        return wrap;
    }

    private void buildAIJobDetailPanel() {
        aiJobDetailPanel = new JPanel(new BorderLayout(0, 0));
        aiJobDetailPanel.setBackground(JobsPortalUi.PAGE_BG);
        aiJobDetailPanel.setOpaque(true);
        aiJobDetailPanel.setVisible(false);

        JPanel header = buildAiSubpageHeader(
                "← Back to Ranking",
                () -> {
                    aiJobDetailPanel.setVisible(false);
                    if (aiRankingDetailPanel != null) {
                        aiRankingDetailPanel.setVisible(true);
                    }
                    installAIRankingInCenter();
                    bottomContainer.revalidate();
                    bottomContainer.repaint();
                },
                "AI Match Detail",
                "Score breakdown, strengths, and recommendations for this position.");
        JPanel headerAlign = new JPanel(new BorderLayout());
        headerAlign.setOpaque(false);
        headerAlign.add(header, BorderLayout.WEST);
        aiJobDetailPanel.add(headerAlign, BorderLayout.NORTH);

        aiJobDetailContent = new JPanel();
        aiJobDetailContent.setLayout(new BoxLayout(aiJobDetailContent, BoxLayout.Y_AXIS));
        aiJobDetailContent.setOpaque(true);
        aiJobDetailContent.setBackground(JobsPortalUi.PAGE_BG);
        aiJobDetailContent.setBorder(new EmptyBorder(12, 12, 24, 12));

        aiJobDetailScrollPane = new JScrollPane(aiJobDetailContent);
        aiJobDetailScrollPane.setBorder(null);
        aiJobDetailScrollPane.setOpaque(true);
        aiJobDetailScrollPane.getViewport().setOpaque(true);
        aiJobDetailScrollPane.getViewport().setBackground(JobsPortalUi.PAGE_BG);
        aiJobDetailScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        aiJobDetailScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        aiJobDetailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // BACKINGSTORE 在半透明/嵌套组件上易产生重影与“叠窗”错觉，改用默认简单滚动
        aiJobDetailScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        aiJobDetailPanel.add(aiJobDetailScrollPane, BorderLayout.CENTER);

        if (mainCenterStack == null) {
            bottomContainer.add(aiJobDetailPanel);
        }
    }

    private void populateJobDetailContent(JobMatchResult result) {
        aiJobDetailContent.removeAll();

        String[] strengths = JobsAnalysisParser.extractStrengths(result.analysis);
        String[] weaknesses = JobsAnalysisParser.extractWeaknesses(result.analysis);
        String[] recommendations = JobsAnalysisParser.extractRecommendations(result.analysis);
        String summary = JobsAnalysisParser.extractRankingSummaryPreview(result.analysis, 0);
        if (summary == null || summary.isBlank() || "\u2014".equals(summary)) {
            summary = safeText(result.job.getSummary());
        }

        int skillsMatch = clampPercentInt(JobsAnalysisParser.extractMatchScoreByType(result.analysis, "skillsMatch"));
        int gpaMatch = clampPercentInt(JobsAnalysisParser.extractMatchScoreByType(result.analysis, "gpaMatch"));
        int expMatch = clampPercentInt(JobsAnalysisParser.extractMatchScoreByType(result.analysis, "experienceMatch"));
        int overallMatch = clampPercentInt(result.score);

        aiJobDetailContent.add(createDetailHeroCard(result, summary));
        aiJobDetailContent.add(Box.createVerticalStrut(14));

        // 单列纵向排布，避免双列 GridBag 在窄窗口下总首选宽度过大触发横向滚动
        JPanel scoreCard = createScoreBreakdownCard(skillsMatch, gpaMatch, expMatch, overallMatch);
        scoreCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiJobDetailContent.add(scoreCard);
        aiJobDetailContent.add(Box.createVerticalStrut(14));

        JPanel strengthsCard = createInsightCard("Strengths", strengths,
                new Color(22, 163, 74), new Color(220, 252, 231), new Color(21, 128, 61),
                JobsPortalUi.starCircleIcon(new Color(22, 163, 74), 18));
        strengthsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiJobDetailContent.add(strengthsCard);
        aiJobDetailContent.add(Box.createVerticalStrut(14));

        JPanel weaknessesCard = createInsightCard("Areas for Improvement", weaknesses,
                new Color(185, 28, 28), new Color(254, 226, 226), new Color(153, 27, 27),
                JobsPortalUi.exclamationCircleIcon(new Color(185, 28, 28), 18));
        weaknessesCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiJobDetailContent.add(weaknessesCard);
        aiJobDetailContent.add(Box.createVerticalStrut(14));

        JPanel recCard = createInsightCard("Recommendations", recommendations,
                new Color(29, 78, 216), new Color(219, 234, 254), new Color(30, 64, 175),
                JobsPortalUi.lightbulbIcon(new Color(29, 78, 216), 18));
        recCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiJobDetailContent.add(recCard);
        aiJobDetailContent.add(Box.createVerticalStrut(16));

        aiJobDetailContent.add(createRawAnalysisCard(result.analysis));
        aiJobDetailContent.add(Box.createVerticalStrut(16));

        JButton viewJobBtn = JobsPortalUi.gradientButton("View Full Job Details", new Font("Segoe UI", Font.BOLD, 13),
                JobsPortalUi.fileTextIcon(Color.WHITE, 16));
        viewJobBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewJobBtn.addActionListener(e -> {
            aiJobDetailPanel.setVisible(false);
            hideAIRankingDetail();
            callback.onViewJobDetail(result.job);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(viewJobBtn);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiJobDetailContent.add(btnPanel);
        aiJobDetailContent.add(Box.createVerticalGlue());

        aiJobDetailContent.revalidate();
        aiJobDetailContent.repaint();
    }

    private JPanel createDetailHeroCard(JobMatchResult result, String summary) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout(22, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel inner = new JPanel(new BorderLayout(22, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel medalLabel = new JLabel(JobsPortalUi.medalIcon(new Color(221, 160, 0), 42));
        medalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel medalTile = JobsPortalUi.wrapRoundedInner(medalLabel, 16, new Color(255, 250, 236),
                null, 0f, false, new Insets(18, 22, 18, 22));
        medalTile.setPreferredSize(new Dimension(96, 80));
        medalTile.setMinimumSize(new Dimension(88, 72));
        medalTile.setMaximumSize(new Dimension(104, 88));
        inner.add(medalTile, BorderLayout.WEST);

        JPanel jobInfoLeft = new JPanel();
        jobInfoLeft.setLayout(new BoxLayout(jobInfoLeft, BoxLayout.Y_AXIS));
        jobInfoLeft.setOpaque(false);
        jobInfoLeft.setAlignmentX(Component.LEFT_ALIGNMENT);
        String heroTitle = safeText(result.job.getTitle()).isBlank() ? "Untitled Position" : safeText(result.job.getTitle());
        JTextArea jobTitleArea = new JTextArea(heroTitle);
        jobTitleArea.setFont(new Font("Segoe UI", Font.BOLD, 22));
        jobTitleArea.setForeground(DARK_TEXT);
        jobTitleArea.setRows(1);
        jobTitleArea.setColumns(26);
        jobTitleArea.setLineWrap(true);
        jobTitleArea.setWrapStyleWord(true);
        jobTitleArea.setEditable(false);
        jobTitleArea.setFocusable(false);
        jobTitleArea.setOpaque(false);
        jobTitleArea.setBorder(null);
        jobTitleArea.setMargin(new Insets(0, 0, 0, 0));
        jobTitleArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobInfoLeft.add(jobTitleArea);
        jobInfoLeft.add(Box.createVerticalStrut(2));

        JTextArea jobMetaArea = new JTextArea(buildJobMeta(result.job));
        jobMetaArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jobMetaArea.setForeground(MUTED_TEXT);
        jobMetaArea.setRows(1);
        jobMetaArea.setColumns(26);
        jobMetaArea.setLineWrap(true);
        jobMetaArea.setWrapStyleWord(true);
        jobMetaArea.setEditable(false);
        jobMetaArea.setFocusable(false);
        jobMetaArea.setOpaque(false);
        jobMetaArea.setBorder(null);
        jobMetaArea.setMargin(new Insets(0, 0, 0, 0));
        jobMetaArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobInfoLeft.add(jobMetaArea);
        jobInfoLeft.add(Box.createVerticalStrut(6));

        JTextArea summaryText = new JTextArea(summary != null ? summary.trim().replaceAll("\\s+", " ") : "");
        summaryText.setRows(3);
        summaryText.setColumns(26);
        summaryText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryText.setForeground(new Color(82, 90, 120));
        summaryText.setOpaque(false);
        summaryText.setEditable(false);
        summaryText.setFocusable(false);
        summaryText.setLineWrap(true);
        summaryText.setWrapStyleWord(true);
        summaryText.setBorder(null);
        summaryText.setMargin(new Insets(0, 0, 0, 0));
        summaryText.setAlignmentX(Component.LEFT_ALIGNMENT);
        jobInfoLeft.add(summaryText);
        inner.add(jobInfoLeft, BorderLayout.CENTER);

        JPanel scoreTile = createScoreTile(result.score, true);
        scoreTile.setPreferredSize(new Dimension(140, 100));
        scoreTile.setMinimumSize(new Dimension(124, 88));
        inner.add(scoreTile, BorderLayout.EAST);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel createScoreBreakdownCard(int skillsMatch, int gpaMatch, int expMatch, int overallMatch) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(18, 20, 20, 20));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.add(new JLabel(JobsPortalUi.lineChartIcon(PRIMARY_PURPLE, 17)));
        JLabel scoreTitle = new JLabel("Score Breakdown");
        scoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scoreTitle.setForeground(DARK_TEXT);
        titleRow.add(scoreTitle);
        inner.add(titleRow);
        inner.add(Box.createVerticalStrut(16));

        inner.add(createScoreRow("Skills Match", skillsMatch, BLUE_ACCENT));
        inner.add(Box.createVerticalStrut(8));
        inner.add(createScoreRow("GPA Match", gpaMatch, new Color(34, 197, 94)));
        inner.add(Box.createVerticalStrut(8));
        inner.add(createScoreRow("Experience Match", expMatch, new Color(168, 85, 247)));
        inner.add(Box.createVerticalStrut(8));
        inner.add(createScoreRow("Overall Match", overallMatch, getScoreColor(overallMatch)));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInsightCard(String title, String[] items, Color titleColor, Color itemBgColor, Color bulletColor, Icon titleIcon) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(18, 20, 20, 20));
        inner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout(0, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleLeft.setOpaque(false);
        titleLeft.add(new JLabel(titleIcon));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(titleColor);
        titleLeft.add(titleLabel);
        titleRow.add(titleLeft, BorderLayout.WEST);
        inner.add(titleRow);
        inner.add(Box.createVerticalStrut(10));

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] safeItems = items != null ? items : new String[0];
        if (safeItems.length == 0) {
            safeItems = new String[]{"No structured points were extracted from the AI response."};
        }
        int limit = Math.min(6, safeItems.length);
        Color stroke = new Color(
                (itemBgColor.getRed() + bulletColor.getRed()) / 2,
                (itemBgColor.getGreen() + bulletColor.getGreen()) / 2,
                (itemBgColor.getBlue() + bulletColor.getBlue()) / 2);
        for (int i = 0; i < limit; i++) {
            stack.add(createInsightSentenceBox(safeItems[i], itemBgColor, stroke));
            if (i < limit - 1) {
                stack.add(Box.createVerticalStrut(8));
            }
        }
        inner.add(stack);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /** 每条一句一框，无项目符号 */
    private JPanel createInsightSentenceBox(String sentence, Color fill, Color stroke) {
        String t = sentence != null ? sentence.trim() : "";
        JobsPortalUi.RoundedSurface box = new JobsPortalUi.RoundedSurface(
                10, fill, stroke, 1f, false, new BorderLayout());
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea ta = new JTextArea(t);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        ta.setForeground(new Color(45, 55, 78));
        ta.setOpaque(false);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(11, 14, 11, 14));
        ta.setMargin(new Insets(0, 0, 0, 0));
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(ta, BorderLayout.CENTER);
        return box;
    }

    private JPanel createRawAnalysisCard(String raw) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(16, 20, 18, 20));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.add(new JLabel(JobsPortalUi.fileTextIcon(PRIMARY_PURPLE, 17)));
        JLabel rawTitle = new JLabel("AI Raw Analysis");
        rawTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        rawTitle.setForeground(DARK_TEXT);
        titleRow.add(rawTitle);
        inner.add(titleRow);
        inner.add(Box.createVerticalStrut(10));

        JTextArea rawArea = new JTextArea(raw != null ? raw : "");
        rawArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        rawArea.setForeground(new Color(55, 65, 81));
        rawArea.setBackground(new Color(249, 250, 253));
        rawArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        rawArea.setLineWrap(true);
        rawArea.setWrapStyleWord(true);
        rawArea.setEditable(false);
        rawArea.setRows(4);

        JScrollPane rawScroll = new JScrollPane(rawArea);
        rawScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 230, 236), 1));
        rawScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rawScroll.getViewport().setBackground(new Color(249, 250, 253));
        rawScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rawScroll.setPreferredSize(new Dimension(200, 168));
        rawScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        inner.add(rawScroll);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // 创建详情卡片（兼容旧调用；新 AI 详情页使用 createInsightCard）
    private JPanel createDetailCard(String title, String[] items, Color titleColor, Color itemBgColor, Color bulletColor) {
        return createInsightCard(title, items, titleColor, itemBgColor, bulletColor,
                JobsPortalUi.starCircleIcon(titleColor, 18));
    }

    private void showJobDetailReport(JobMatchResult result) {
        // 创建详细报告面板
        if (aiJobDetailPanel == null) {
            buildAIJobDetailPanel();
        }

        // 填充报告内容
        populateJobDetailContent(result);

        aiJobDetailPanel.setVisible(true);
        setJobsPageBottomStripVisible(false);
        installAIJobDetailInCenter();

        // 滚动到顶部
        SwingUtilities.invokeLater(() -> {
            if (aiJobDetailScrollPane != null) {
                aiJobDetailScrollPane.getVerticalScrollBar().setValue(0);
            }
            panel.revalidate();
            panel.repaint();
        });
    }

    private void buildAIRankingDetailPanel() {
        aiRankingDetailPanel = new JPanel(new BorderLayout(0, 0));
        aiRankingDetailPanel.setBackground(JobsPortalUi.PAGE_BG);
        aiRankingDetailPanel.setVisible(false);

        JPanel header = buildAiSubpageHeader(
                "\u2190 Back to Jobs",
                this::hideAIRankingDetail,
                "AI Match Ranking",
                "All analyzed positions, sorted by match score.");
        JPanel headerAlign = new JPanel(new BorderLayout());
        headerAlign.setOpaque(false);
        headerAlign.add(header, BorderLayout.WEST);
        aiRankingDetailPanel.add(headerAlign, BorderLayout.NORTH);

        // 内容区域 - 可滚动的结果列表（GridBagLayout 每行占满视口宽度，避免 BoxLayout 把窄子组件水平居中导致与统计条错位）
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(JobsPortalUi.PAGE_BG);
        contentPanel.setBorder(new EmptyBorder(12, 12, 24, 12));

        aiRankingDetailScrollPane = new JScrollPane(contentPanel);
        aiRankingDetailScrollPane.setBorder(null);
        aiRankingDetailScrollPane.getViewport().setBackground(JobsPortalUi.PAGE_BG);
        aiRankingDetailScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        aiRankingDetailScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        aiRankingDetailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        aiRankingDetailScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        aiRankingDetailPanel.add(aiRankingDetailScrollPane, BorderLayout.CENTER);

        aiRankingDetailContent = contentPanel;

        if (mainCenterStack == null) {
            bottomContainer.add(aiRankingDetailPanel);
        }
    }

    private JPanel aiRankingDetailContent; // 排名详情内容面板

    private void hideAIRankingDetail() {
        // 隐藏所有AI相关面板
        if (aiJobDetailPanel != null) aiJobDetailPanel.setVisible(false);
        if (aiRankingDetailPanel != null) aiRankingDetailPanel.setVisible(false);
        installJobsListInCenter();
        if (northStack != null) northStack.setVisible(true);
        // 返回时重新显示AI结果面板（如果有结果）
        if (cachedMatchResults != null && aiResultsPanel != null) {
            aiResultsPanel.setVisible(true);
        }
        setJobsPageBottomStripVisible(true);
        // 分析完成后，按钮保持绿色状态
        if (cachedMatchResults != null && sortByMatchBtn != null) {
            sortByMatchBtn.setText("AI Smart Match");
            sortByMatchBtn.repaint();
        }
        bottomContainer.revalidate();
        bottomContainer.repaint();
        panel.revalidate();
        panel.repaint();
    }

    private JPanel northStack; // 顶部区域引用

    private void applySortedResults(List<JobMatchResult> matchResults) {
        sortedJobs.clear();
        for (JobMatchResult result : matchResults) {
            sortedJobs.add(result.job);
        }
        isSortedByMatch = true;
        sortByMatchBtn.setText("AI Smart Match");
        sortByMatchBtn.repaint();
        refreshJobsList();
    }

    private String buildUserProfile() {
        StringBuilder profile = new StringBuilder();
        TAUser currentUser = dataService.getCurrentUser();
        if (currentUser != null) {
            TAUser.Skills skills = currentUser.getSkills();
            if (skills != null) {
                List<String> allSkills = new ArrayList<>();
                // 使用getSelectedSkills()方法获取所有选中的技能
                List<TAUser.Skill> selectedSkills = skills.getSelectedSkills();
                if (selectedSkills != null) {
                    for (TAUser.Skill s : selectedSkills) {
                        if (s.getName() != null) allSkills.add(s.getName());
                    }
                }
                if (!allSkills.isEmpty()) {
                    profile.append("Skills: ").append(String.join(", ", allSkills)).append("; ");
                }
            }
            TAUser.Profile userProfile = currentUser.getProfile();
            if (userProfile != null) {
                if (userProfile.getProgramMajor() != null) {
                    profile.append("Major: ").append(userProfile.getProgramMajor()).append("; ");
                }
                if (userProfile.getYear() != null) {
                    profile.append("Year: ").append(userProfile.getYear()).append("; ");
                }
            }
            TAUser.Academic academic = currentUser.getAcademic();
            if (academic != null && academic.getGpa() > 0) {
                profile.append("GPA: ").append(academic.getGpa()).append("; ");
            }
        }
        return profile.length() > 0 ? profile.toString() : "No profile information available";
    }

    private void showMatchResultsDialog(List<JobMatchResult> results) {
        JDialog dialog = new JDialog((java.awt.Frame) null, "AI Match Analysis", true);
        dialog.setSize(900, 650);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(UI_Constants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Header with title and stats
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleArea = new JPanel(new BorderLayout(0, 8));
        titleArea.setOpaque(false);
        JLabel titleLabel = new JLabel("AI Match Analysis Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleArea.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel("Based on your profile, skills, and experience");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        titleArea.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.add(titleArea, BorderLayout.WEST);

        // Stats summary
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);
        if (!results.isEmpty()) {
            double avgScore = results.stream().mapToDouble(r -> r.score).average().orElse(0);
            JLabel analyzedLabel = new JLabel("Analyzed: " + results.size() + " jobs");
            analyzedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            analyzedLabel.setForeground(UI_Constants.TEXT_SECONDARY);
            statsPanel.add(analyzedLabel);

            JLabel avgLabel = new JLabel("Avg Match: " + String.format("%.0f%%", avgScore));
            avgLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            avgLabel.setForeground(UI_Constants.PRIMARY_COLOR);
            statsPanel.add(avgLabel);
        }
        headerPanel.add(statsPanel, BorderLayout.EAST);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Results list with detailed cards
        JPanel resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setOpaque(false);

        for (int i = 0; i < Math.min(10, results.size()); i++) {
            JobMatchResult result = results.get(i);
            resultsContainer.add(createDetailedResultCard(result, i + 1));
            resultsContainer.add(Box.createVerticalStrut(12));
        }

        JScrollPane scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with hint
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        JLabel hintLabel = new JLabel("Click on any job card to see detailed analysis");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        hintLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        bottomPanel.add(hintLabel);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private JPanel createDetailedResultCard(JobMatchResult result, int rank) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(16, 20, 16, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Rank badge on left
        JPanel rankPanel = new JPanel();
        rankPanel.setOpaque(false);
        rankPanel.setLayout(new BoxLayout(rankPanel, BoxLayout.Y_AXIS));
        rankPanel.setBorder(new EmptyBorder(0, 0, 0, 16));

        Color rankColor;
        String rankLabel;
        if (rank == 1) {
            rankColor = new Color(234, 179, 8);
            rankLabel = "#1";
        } else if (rank == 2) {
            rankColor = new Color(156, 163, 175);
            rankLabel = "#2";
        } else if (rank == 3) {
            rankColor = new Color(180, 119, 69);
            rankLabel = "#3";
        } else {
            rankColor = UI_Constants.TEXT_SECONDARY;
            rankLabel = "#" + rank;
        }

        JLabel rankBadge = new JLabel(rankLabel);
        rankBadge.setFont(new Font("Segoe UI", Font.BOLD, 22));
        rankBadge.setForeground(rankColor);
        rankBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        rankPanel.add(rankBadge);
        card.add(rankPanel, BorderLayout.WEST);

        // Main content area
        JPanel mainContent = new JPanel(new BorderLayout(16, 8));
        mainContent.setOpaque(false);

        // Top row: title and match score
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 4));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(result.job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        String meta = result.job.getCourseCode() + "  \u2022  " + result.job.getDepartment();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        metaLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        titlePanel.add(metaLabel, BorderLayout.SOUTH);

        topRow.add(titlePanel, BorderLayout.CENTER);

        // Match score badge
        JPanel scorePanel = new JPanel(new BorderLayout(0, 4));
        scorePanel.setOpaque(false);

        JLabel scoreLabel = new JLabel(String.format("%.0f%%", result.score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        Color scoreColor = getScoreColor(result.score);
        scoreLabel.setForeground(scoreColor);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scorePanel.add(scoreLabel, BorderLayout.NORTH);

        JLabel matchLabel = new JLabel("MATCH");
        matchLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        matchLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        matchLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scorePanel.add(matchLabel, BorderLayout.SOUTH);

        scorePanel.setBorder(new EmptyBorder(0, 20, 0, 0));
        topRow.add(scorePanel, BorderLayout.EAST);
        mainContent.add(topRow, BorderLayout.NORTH);

        // Bottom row: brief analysis preview
        String analysisPreview = JobsAnalysisParser.extractBriefAnalysis(result.analysis);
        JTextArea previewArea = new JTextArea(2, 40); // 固定列数
        previewArea.setText(analysisPreview);
        previewArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        previewArea.setForeground(UI_Constants.TEXT_SECONDARY);
        previewArea.setEditable(false);
        previewArea.setFocusable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setOpaque(false);
        previewArea.setBorder(null);
        mainContent.add(previewArea, BorderLayout.CENTER);

        card.add(mainContent, BorderLayout.CENTER);

        // Click action
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showJobAnalysisDetail(result);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UI_Constants.PRIMARY_COLOR, 2),
                    new EmptyBorder(16, 20, 16, 20)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UI_Constants.CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
                    new EmptyBorder(16, 20, 16, 20)
                ));
            }
        });

        return card;
    }

    private Color getScoreColor(double score) {
        if (score >= 80) return new Color(22, 163, 74);
        if (score >= 60) return new Color(202, 138, 4);
        if (score >= 40) return new Color(234, 88, 12);
        return new Color(220, 38, 38);
    }

    private void showJobAnalysisDetail(JobMatchResult result) {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Detailed Analysis", true);
        dialog.setSize(800, 700);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(UI_Constants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel jobInfoPanel = new JPanel(new BorderLayout(0, 6));
        jobInfoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(result.job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        jobInfoPanel.add(titleLabel, BorderLayout.NORTH);

        String meta = result.job.getCourseCode() + "  \u2022  " + result.job.getDepartment() + "  \u2022  " + result.job.getInstructorName();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        jobInfoPanel.add(metaLabel, BorderLayout.SOUTH);

        headerPanel.add(jobInfoPanel, BorderLayout.WEST);

        // Match score card
        JPanel scoreCard = new JPanel(new BorderLayout(0, 6));
        scoreCard.setBackground(UI_Constants.CARD_BG);
        scoreCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getScoreColor(result.score), 2),
            new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel bigScoreLabel = new JLabel(String.format("%.0f%%", result.score));
        bigScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        bigScoreLabel.setForeground(getScoreColor(result.score));
        bigScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreCard.add(bigScoreLabel, BorderLayout.NORTH);

        JLabel matchTextLabel = new JLabel("Match Score");
        matchTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        matchTextLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        matchTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreCard.add(matchTextLabel, BorderLayout.SOUTH);

        headerPanel.add(scoreCard, BorderLayout.EAST);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Analysis content
        JPanel analysisPanel = new JPanel(new BorderLayout(0, 16));
        analysisPanel.setOpaque(false);

        // Section: Analysis breakdown
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);

        JLabel sectionTitle = new JLabel("AI Analysis Report");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionPanel.add(sectionTitle);

        JPanel analysisCard = new JPanel(new BorderLayout());
        analysisCard.setBackground(Color.WHITE);
        analysisCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JTextArea analysisArea = new JTextArea(10, 50); // 限制行数和列数
        analysisArea.setText(result.analysis);
        analysisArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        analysisArea.setBackground(Color.WHITE);
        analysisArea.setForeground(UI_Constants.TEXT_PRIMARY);
        analysisArea.setBorder(null);

        analysisCard.add(analysisArea, BorderLayout.CENTER);
        sectionPanel.add(analysisCard);

        analysisPanel.add(sectionPanel, BorderLayout.NORTH);

        // Requirements comparison panel
        JPanel comparePanel = new JPanel();
        comparePanel.setLayout(new BoxLayout(comparePanel, BoxLayout.Y_AXIS));
        comparePanel.setOpaque(false);

        JLabel compareTitle = new JLabel("Job Requirements vs Your Profile");
        compareTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        compareTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        compareTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        compareTitle.setBorder(new EmptyBorder(16, 0, 8, 0));
        comparePanel.add(compareTitle);

        // Requirements
        JPanel reqCard = createRequirementCard("Required Skills",
            result.job.getContent() != null && result.job.getContent().getRequirements() != null ?
                result.job.getContent().getRequirements() : java.util.Arrays.asList("Not specified"),
            getUserSkillsList(), true);
        comparePanel.add(reqCard);

        JPanel prefCard = createRequirementCard("Preferred Skills",
            result.job.getContent() != null && result.job.getContent().getPreferredSkills() != null ?
                result.job.getContent().getPreferredSkills() : java.util.Arrays.asList("None"),
            getUserSkillsList(), false);
        comparePanel.add(prefCard);

        analysisPanel.add(comparePanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(analysisPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton applyBtn = new JButton("View Job Details");
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setBackground(UI_Constants.PRIMARY_COLOR);
        applyBtn.setOpaque(true);
        applyBtn.setFocusPainted(false);
        applyBtn.setBorderPainted(false);
        applyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyBtn.setBorder(new EmptyBorder(10, 24, 10, 24));
        applyBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                applyBtn.setBackground(JobsPortalUi.PURPLE_600);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                applyBtn.setBackground(UI_Constants.PRIMARY_COLOR);
            }
        });
        applyBtn.addActionListener(e -> {
            dialog.dispose();
            callback.onViewJobDetail(result.job);
        });
        buttonPanel.add(applyBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setForeground(UI_Constants.TEXT_PRIMARY);
        closeBtn.setBackground(UI_Constants.CARD_BG);
        closeBtn.setOpaque(true);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setBackground(new Color(229, 231, 235));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setBackground(UI_Constants.CARD_BG);
            }
        });
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private List<String> getUserSkillsList() {
        List<String> skills = new ArrayList<>();
        TAUser currentUser = dataService.getCurrentUser();
        if (currentUser != null && currentUser.getSkills() != null) {
            TAUser.Skills userSkills = currentUser.getSkills();
            // 使用getSelectedSkills()方法获取所有选中的技能
            List<TAUser.Skill> selectedSkills = userSkills.getSelectedSkills();
            if (selectedSkills != null) {
                for (TAUser.Skill s : selectedSkills) {
                    if (s.getName() != null) skills.add(s.getName());
                }
            }
        }
        return skills;
    }

    private JPanel createRequirementCard(String title, List<String> requirements, List<String> userSkills, boolean isRequired) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR),
            new EmptyBorder(12, 16, 12, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 40));

        JLabel cardTitle = new JLabel(title);
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel skillsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        skillsGrid.setOpaque(false);

        for (String req : requirements) {
            boolean hasSkill = userSkills.stream().anyMatch(s ->
                s.toLowerCase().contains(req.toLowerCase()) || req.toLowerCase().contains(s.toLowerCase()));

            JLabel skillTag = new JLabel(req);
            skillTag.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (isRequired) {
                if (hasSkill) {
                    skillTag.setForeground(new Color(22, 163, 74));
                    skillTag.setBackground(new Color(220, 252, 231));
                } else {
                    skillTag.setForeground(new Color(220, 38, 38));
                    skillTag.setBackground(new Color(254, 226, 226));
                }
            } else {
                if (hasSkill) {
                    skillTag.setForeground(new Color(202, 138, 4));
                    skillTag.setBackground(new Color(254, 249, 195));
                } else {
                    skillTag.setForeground(UI_Constants.TEXT_SECONDARY);
                    skillTag.setBackground(new Color(243, 244, 246));
                }
            }

            skillTag.setOpaque(true);
            skillTag.setBorder(new EmptyBorder(4, 10, 4, 10));
            skillsGrid.add(skillTag);
        }

        card.add(skillsGrid, BorderLayout.CENTER);
        return card;
    }


    // ===== Modern purple UI helpers for this page only =====
    private static final Color PRIMARY_PURPLE = JobsPortalUi.PRIMARY_PURPLE;
    private static final Color DEEP_PURPLE = JobsPortalUi.DEEP_PURPLE;
    private static final Color LAVENDER_BG = JobsPortalUi.LAVENDER;
    private static final Color LIGHT_PURPLE_BORDER = JobsPortalUi.LIGHT_PURPLE_BORDER;
    private static final Color DARK_TEXT = JobsPortalUi.DARK_TEXT;
    private static final Color MUTED_TEXT = JobsPortalUi.MUTED_TEXT;
    private static final Color BLUE_ACCENT = JobsPortalUi.BLUE_ACCENT;
    private static final Color TEAL_ACCENT = JobsPortalUi.TEAL_ACCENT;
    private static final Color DEADLINE_ACCENT = JobsPortalUi.CORAL_ACCENT;

    private Icon glyphIcon(String glyph, Color color, int size) {
        return new GlyphIcon(glyph, color, size);
    }

    private JPanel createCourseBadge(String text, boolean isCourseCode) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, isCourseCode ? 16 : 22));
        label.setForeground(isCourseCode ? TEAL_ACCENT : Color.WHITE);

        Color fill = isCourseCode ? new Color(218, 246, 243) : PRIMARY_PURPLE;
        Color stroke = isCourseCode ? new Color(190, 237, 232) : DEEP_PURPLE;
        JPanel badge = JobsPortalUi.wrapRoundedInner(label, 14, fill, stroke, 1f, false,
                new Insets(isCourseCode ? 13 : 10, isCourseCode ? 10 : 18, isCourseCode ? 13 : 10, isCourseCode ? 10 : 18));
        badge.setPreferredSize(new Dimension(78, 62));
        badge.setMinimumSize(new Dimension(78, 62));
        badge.setMaximumSize(new Dimension(78, 62));
        return badge;
    }

    private JPanel createTinyTag(String text, Color bg, Color fg) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(fg);
        // FlowLayout inside hug wrapper avoids BorderLayout.CENTER squeezing labels into “…” ellipsis.
        JPanel tag = JobsPortalUi.wrapRoundedInnerHug(label, 10, bg, null, 0f, false, new Insets(4, 9, 4, 9));
        Dimension pref = tag.getPreferredSize();
        tag.setPreferredSize(pref);
        tag.setMinimumSize(pref);
        tag.setMaximumSize(pref);
        return tag;
    }

    private JPanel createMetaPill(Icon icon, String text, Color bg, Color fg) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        if (icon != null) {
            row.add(new JLabel(icon));
            row.add(Box.createHorizontalStrut(7));
        }
        JLabel label = new JLabel(text != null ? text : "");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(fg);
        row.add(label);
        JPanel pill = JobsPortalUi.wrapRoundedInnerHug(row, 12, bg, null, 0f, false, new Insets(7, 11, 7, 11));
        Dimension pref = pill.getPreferredSize();
        pill.setPreferredSize(pref);
        pill.setMinimumSize(pref);
        pill.setMaximumSize(pref);
        return pill;
    }

    private static class GlyphIcon implements Icon {
        private final String glyph;
        private final Color color;
        private final int size;

        GlyphIcon(String glyph, Color color, int size) {
            this.glyph = glyph;
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size + 4;
        }

        @Override
        public int getIconHeight() {
            return size + 4;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, size));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (getIconWidth() - fm.stringWidth(glyph)) / 2;
            int ty = y + (getIconHeight() + fm.getAscent()) / 2 - fm.getDescent() - 1;
            g2.drawString(glyph, tx, ty);
            g2.dispose();
        }
    }

    private String getDoubaoAPIKey() {
        return System.getProperty("doubao.api.key",
            System.getenv("ARK_API_KEY") != null ? System.getenv("ARK_API_KEY") : System.getenv("DOUBao_API_KEY"));
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
    
    private JPanel createJobCard(Job job, int displayIndex) {
        JobsPortalUi.RoundedSurface card = new JobsPortalUi.RoundedSurface(
                18, Color.WHITE, LIGHT_PURPLE_BORDER, 1f, true, new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boolean hasApplied = dataService.hasAppliedToJob(job.getJobId());

        JPanel shell = new JPanel(new BorderLayout(22, 0));
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(22, 26, 22, 26));

        if (hasApplied) {
            JPanel strip = new JPanel();
            strip.setOpaque(true);
            strip.setBackground(new Color(34, 197, 94));
            strip.setPreferredSize(new Dimension(6, 0));
            shell.add(strip, BorderLayout.WEST);
        }

        JPanel westCol = new JPanel();
        westCol.setOpaque(false);
        westCol.setLayout(new BoxLayout(westCol, BoxLayout.Y_AXIS));
        westCol.setBorder(new EmptyBorder(0, 0, 0, 6));
        westCol.setPreferredSize(new Dimension(86, 96));

        String courseCode = job.getCourseCode();
        JPanel badge = createCourseBadge(
                courseCode != null && !courseCode.trim().isEmpty() ? courseCode.trim() : String.valueOf(displayIndex),
                courseCode != null && !courseCode.trim().isEmpty());
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        westCol.add(badge);
        westCol.add(Box.createVerticalStrut(12));

        JLabel miniIcon = new JLabel(JobsPortalUi.briefcaseGlyph(PRIMARY_PURPLE, 30));
        miniIcon.setHorizontalAlignment(SwingConstants.CENTER);
        miniIcon.setVerticalAlignment(SwingConstants.CENTER);
        JPanel iconWrap = JobsPortalUi.wrapRoundedInner(miniIcon, 16, LAVENDER_BG,
                null, 0f, false, new Insets(14, 14, 14, 14));
        iconWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension badgeSize = badge.getPreferredSize();
        int iconTileW = Math.max(badgeSize.width, iconWrap.getPreferredSize().width);
        int iconTileH = Math.max(58, iconWrap.getPreferredSize().height);
        iconWrap.setPreferredSize(new Dimension(iconTileW, iconTileH));
        iconWrap.setMinimumSize(iconWrap.getPreferredSize());
        iconWrap.setMaximumSize(iconWrap.getPreferredSize());
        westCol.add(iconWrap);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JPanel topMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topMeta.setOpaque(false);
        topMeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        String department = job.getDepartment() != null ? job.getDepartment() : "";
        if (!department.isBlank()) {
            topMeta.add(createTinyTag(department, LAVENDER_BG, DEEP_PURPLE));
        }

        String instructor = job.getInstructorName() != null ? job.getInstructorName() : "";
        if (!instructor.isBlank()) {
            if (!department.isBlank()) {
                topMeta.add(Box.createHorizontalStrut(14));
                JLabel dot = new JLabel("•");
                dot.setFont(new Font("Segoe UI", Font.BOLD, 13));
                dot.setForeground(MUTED_TEXT);
                topMeta.add(dot);
                topMeta.add(Box.createHorizontalStrut(14));
            }
            topMeta.add(new JLabel(JobsPortalUi.userIcon(TEAL_ACCENT, 15)));
            topMeta.add(Box.createHorizontalStrut(6));
            JLabel instructorLabel = new JLabel(instructor);
            instructorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            instructorLabel.setForeground(MUTED_TEXT);
            topMeta.add(instructorLabel);
        }

        if (topMeta.getComponentCount() > 0) {
            center.add(topMeta);
            center.add(Box.createVerticalStrut(7));
        }

        JLabel titleLabel = new JLabel(job.getTitle() != null ? job.getTitle() : "Untitled Position");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(8));

        String summary = job.getSummary();
        if (summary == null || summary.isEmpty()) {
            summary = job.getDescription();
        }
        summary = summary != null ? summary.trim().replaceAll("\\s+", " ") : "";
        if (summary.length() > 165) {
            summary = summary.substring(0, 162) + "...";
        }
        JTextArea sumArea = new JTextArea(summary);
        sumArea.setLineWrap(true);
        sumArea.setWrapStyleWord(true);
        sumArea.setEditable(false);
        sumArea.setFocusable(false);
        sumArea.setOpaque(false);
        sumArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sumArea.setForeground(new Color(82, 90, 120));
        sumArea.setBorder(null);
        sumArea.setMargin(new Insets(0, 0, 0, 0));
        sumArea.setRows(2);
        sumArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(sumArea);
        center.add(Box.createVerticalStrut(14));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(createMetaPill(JobsPortalUi.clockPlainIcon(PRIMARY_PURPLE, 16),
                job.getWeeklyHoursDisplay() != null ? job.getWeeklyHoursDisplay() : "", LAVENDER_BG, PRIMARY_PURPLE));
        footer.add(createMetaPill(JobsPortalUi.calendarPlainIcon(DEADLINE_ACCENT, 16),
                "Deadline: " + formatDeadline(job), new Color(255, 239, 239), DEADLINE_ACCENT));
        String location = job.getLocationMode() != null ? job.getLocationMode() : "";
        footer.add(createMetaPill(location.toLowerCase().contains("campus")
                        ? JobsPortalUi.buildingIcon(BLUE_ACCENT, 16)
                        : JobsPortalUi.mapPinIcon(BLUE_ACCENT, 16),
                location, new Color(232, 241, 255), BLUE_ACCENT));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        center.add(footer);

        JPanel actionArea = new JPanel(new GridBagLayout());
        actionArea.setOpaque(false);
        actionArea.setPreferredSize(new Dimension(170, 100));
        JButton actionBtn = hasApplied
                ? JobsPortalUi.appliedOutlineButton("Applied", new Font("Segoe UI", Font.BOLD, 14))
                : JobsPortalUi.gradientButton("View Details  →", new Font("Segoe UI", Font.BOLD, 14), null);
        actionBtn.addActionListener(e -> callback.onViewJobDetail(job));
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionArea.add(actionBtn);

        shell.add(westCol, BorderLayout.WEST);
        shell.add(center, BorderLayout.CENTER);
        shell.add(actionArea, BorderLayout.EAST);
        card.add(shell, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(253, 252, 255));
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.repaint();
            }
        });
        return card;
    }

    /**
     * 自定义彩色进度条 - 只影响这个模块的AI分析进度条
     * 不使用 JProgressBar 而是使用自定义绘制的进度条组件
     */
    private class ColorProgressBar extends JPanel {
        private int value = 0;
        private int max = 100;
        private Color progressColor = new Color(198, 185, 255);
        private Color trackColor = new Color(188, 178, 232);
        private boolean showText = true;

        public ColorProgressBar() {
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        public void setValue(int value) {
            this.value = Math.max(0, Math.min(value, max));
            repaint();
        }

        public int getValue() {
            return value;
        }

        public void setMaximum(int max) {
            this.max = max;
            repaint();
        }

        public void setProgressColor(Color color) {
            this.progressColor = color;
            repaint();
        }

        public void setTrackColor(Color color) {
            this.trackColor = color;
            repaint();
        }

        public void setStringPainted(boolean show) {
            this.showText = show;
            repaint();
        }

        public void setString(String text) {
            // 这个方法只是为了兼容 JProgressBar 的接口
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // 绘制背景轨道
            g2.setColor(trackColor);
            g2.fillRoundRect(0, 0, width, height, height / 2, height / 2);

            // 计算填充宽度
            double percent = (double) value / max;
            int fillWidth = (int) (width * percent);

            if (fillWidth > 0) {
                // 绘制进度填充
                g2.setColor(progressColor);
                g2.fillRoundRect(0, 0, fillWidth, height, height / 2, height / 2);
            }

            // 绘制进度文字
            if (showText && percent > 0.1) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String text = (int) (percent * 100) + "%";
                FontMetrics fm = g2.getFontMetrics();
                int textX = (width - fm.stringWidth(text)) / 2;
                int textY = (height + fm.getAscent()) / 2 - fm.getDescent();
                g2.drawString(text, textX, textY);
            }

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 8);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(100, 6);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 12);
        }
    }
}

