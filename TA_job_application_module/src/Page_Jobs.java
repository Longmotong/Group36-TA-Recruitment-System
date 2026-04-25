package TA_Job_Application_Module;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private List<Job> filteredJobs;
    private List<Job> sortedJobs;
    private JLabel jobListCountLabel;
    private JobsCallback callback;
    private JButton sortByMatchBtn;
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
    private JProgressBar aiProgressBar;     // 进度条
    private JLabel aiProgressLabel;          // 进度标签
    private JButton aiCloseBtn;              // 关闭按钮
    private JButton aiViewDetailsBtn;        // 查看详情按钮
    private List<JobMatchResult> streamingResults = new ArrayList<>();
    private int streamingCurrentIndex = 0;
    private int streamingTotalJobs = 0;

    // AI排名详情页面
    private JPanel aiRankingDetailPanel;     // 排名详情面板
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

                        double score = extractMatchScore(result);
                        matchResults.add(new JobMatchResult(job, score, result));
                    } catch (Exception e) {
                        matchResults.add(new JobMatchResult(job, 0, "Analysis failed: " + e.getMessage()));
                    }
                }

                matchResults.sort((a, b) -> Double.compare(b.score, a.score));
                cachedMatchResults = matchResults;
                
                // Update UI to show AI button is ready
                SwingUtilities.invokeLater(() -> {
                    sortByMatchBtn.setText("AI Smart Sort \u2713");
                    sortByMatchBtn.setBackground(new Color(34, 197, 94)); // Green when ready
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
        filteredJobs = new ArrayList<>(dataService.getOpenJobs());
        filterJobs();
    }
    
    private void initPanel() {
        panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UI_Constants.BG_COLOR);
        panel.setBorder(new EmptyBorder(16, 48, 32, 48));

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
        
        JScrollPane scrollPane = new JScrollPane(jobsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshJobsList();
    }

    private void buildHeader() {
        northStack = new JPanel();
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
        
        // Right side: AI Smart Sort button + My Applications button
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightButtons.setOpaque(false);
        
        sortByMatchBtn = new JButton("AI Smart Sort");
        sortByMatchBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sortByMatchBtn.setForeground(Color.WHITE);
        sortByMatchBtn.setBackground(new Color(99, 102, 241));
        sortByMatchBtn.setOpaque(true);
        sortByMatchBtn.setFocusPainted(false);
        sortByMatchBtn.setBorderPainted(false);
        sortByMatchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sortByMatchBtn.setBorder(new EmptyBorder(8, 12, 8, 12));
        sortByMatchBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                sortByMatchBtn.setBackground(new Color(79, 70, 229));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                sortByMatchBtn.setBackground(new Color(99, 102, 241));
            }
        });
        sortByMatchBtn.addActionListener(e -> onAISmartSortClicked());
        rightButtons.add(sortByMatchBtn);
        
        JButton myAppsBtn = createJobsOutlineButton("My Applications");
        myAppsBtn.addActionListener(e -> callback.onGoToApplications());
        rightButtons.add(myAppsBtn);
        
        titleRow.add(rightButtons, BorderLayout.EAST);
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

    // 更新组件布局位置
    private void updateLayout() {
        panel.revalidate();
        panel.repaint();
    }

    private void hideAIResults() {
        isAnalyzing = false;
        if (aiResultsPanel != null) aiResultsPanel.setVisible(false);
        sortByMatchBtn.setText("AI Smart Sort");
        sortByMatchBtn.setBackground(new Color(99, 102, 241));
        updateLayout();
    }

    private void showAIResults() {
        if (aiResultsPanel != null) aiResultsPanel.setVisible(true);
        updateLayout();
    }

    // 构建内嵌的AI结果面板
    private void buildAIResultsPanel() {
        aiResultsPanel = new JPanel();
        aiResultsPanel.setLayout(new BoxLayout(aiResultsPanel, BoxLayout.Y_AXIS));
        aiResultsPanel.setBackground(UI_Constants.BG_COLOR);
        aiResultsPanel.setVisible(false);

        // 蓝色横幅
        JPanel bannerPanel = new JPanel(new BorderLayout(12, 0));
        bannerPanel.setBackground(new Color(99, 102, 241));
        bannerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        bannerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel bannerIcon = new JLabel("AI");
        bannerIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bannerIcon.setForeground(Color.WHITE);
        bannerPanel.add(bannerIcon, BorderLayout.WEST);

        // 进度条
        aiProgressBar = new JProgressBar(0, 100);
        aiProgressBar.setValue(0);
        aiProgressBar.setStringPainted(true);
        aiProgressBar.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        aiProgressBar.setForeground(new Color(129, 140, 248));
        aiProgressBar.setBackground(new Color(67, 56, 202));
        aiProgressBar.setBorderPainted(false);
        bannerPanel.add(aiProgressBar, BorderLayout.CENTER);

        // 状态标签
        aiStatusLabel = new JLabel("");
        aiStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        aiStatusLabel.setForeground(Color.WHITE);
        bannerPanel.add(aiStatusLabel, BorderLayout.EAST);

        aiResultsPanel.add(bannerPanel);

        // 结果卡片区域 - 横向排列3个卡片
        aiResultsContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        aiResultsContentPanel.setBackground(UI_Constants.BG_COLOR);
        aiResultsContentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        aiResultsPanel.add(aiResultsContentPanel);

        // 按钮区域 - 放在卡片下方
        aiButtonPanel = new JPanel();
        aiButtonPanel.setLayout(new BoxLayout(aiButtonPanel, BoxLayout.X_AXIS));
        aiButtonPanel.setBackground(UI_Constants.BG_COLOR);
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
            for (Job job : jobsToShow) {
                jobsListPanel.add(createJobCard(job));
                jobsListPanel.add(Box.createVerticalStrut(20));
            }
        }
    
        jobsListPanel.add(Box.createVerticalGlue());
        
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

        // If cached results exist, show directly
        if (cachedMatchResults != null) {
            streamingResults = new ArrayList<>(cachedMatchResults);
            applySortedResults(cachedMatchResults);
            showAIRankingDetail();
            return;
        }

        // If analysis is in progress, just show the panel
        if (isAnalyzing) {
            showAIResults();
            return;
        }

        // Start streaming analysis
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
        sortByMatchBtn.setBackground(new Color(99, 102, 241));

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

                    double score = extractMatchScore(result);
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

            SwingUtilities.invokeLater(() -> {
                sortByMatchBtn.setText("AI Smart Sort");
                sortByMatchBtn.setBackground(new Color(34, 197, 94));

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

        // 添加View Ranking按钮到按钮面板（正下方中间）
        aiButtonPanel.removeAll();

        JButton viewRankingBtn = new JButton("View Ranking \u2192");
        viewRankingBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewRankingBtn.setForeground(Color.WHITE);
        viewRankingBtn.setBackground(new Color(99, 102, 241));
        viewRankingBtn.setOpaque(true);
        viewRankingBtn.setFocusPainted(false);
        viewRankingBtn.setBorderPainted(false);
        viewRankingBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewRankingBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        viewRankingBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                viewRankingBtn.setBackground(new Color(79, 70, 229));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                viewRankingBtn.setBackground(new Color(99, 102, 241));
            }
        });
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
        JLabel analyzingLabel = new JLabel("Analyzing... " + streamingCurrentIndex + "/" + streamingTotalJobs);
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
        JPanel card = new JPanel(new BorderLayout(6, 0));
        card.setBackground(UI_Constants.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
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

        return card;
    }

    // 显示AI排名详情页面
    private void showAIRankingDetail() {
        if (streamingResults.isEmpty()) return;

        allMatchResults = new ArrayList<>(streamingResults);

        // 隐藏AI结果面板（底部的3张卡片区域）
        if (aiResultsPanel != null) aiResultsPanel.setVisible(false);

        // 隐藏职位列表和顶部区域
        if (jobsListPanel != null) jobsListPanel.setVisible(false);
        if (northStack != null) northStack.setVisible(false);

        // 填充排名详情内容
        populateRankingDetailContent();

        // 显示排名详情面板
        aiRankingDetailPanel.setVisible(true);
        panel.revalidate();
        panel.repaint();
    }

    private void populateRankingDetailContent() {
        aiRankingDetailContent.removeAll();

        // 添加统计信息栏
        aiRankingDetailContent.add(buildStatsBar());
        aiRankingDetailContent.add(Box.createVerticalStrut(16));

        for (int i = 0; i < allMatchResults.size(); i++) {
            JobMatchResult result = allMatchResults.get(i);
            int rank = i + 1;
            JPanel card = createRankingDetailCard(result, rank);
            aiRankingDetailContent.add(card);
            aiRankingDetailContent.add(Box.createVerticalStrut(12));
        }

        aiRankingDetailContent.add(Box.createVerticalGlue());
        aiRankingDetailContent.revalidate();
        aiRankingDetailContent.repaint();
    }

    // 构建统计信息栏
    private JPanel buildStatsBar() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statsPanel.setOpaque(false);

        int totalJobs = allMatchResults.size();
        double avgScore = allMatchResults.stream().mapToDouble(r -> r.score).average().orElse(0);
        int highMatchCount = (int) allMatchResults.stream().filter(r -> r.score >= 70).count();

        // 总职位数
        JPanel stat1 = createStatItem("Total Jobs Analyzed", String.valueOf(totalJobs));
        statsPanel.add(stat1);

        // 平均匹配度
        JPanel stat2 = createStatItem("Average Match", String.format("%.0f%%", avgScore));
        statsPanel.add(stat2);

        // 高匹配数
        JPanel stat3 = createStatItem("High Match (>=70%)", String.valueOf(highMatchCount));
        statsPanel.add(stat3);

        return statsPanel;
    }

    private JPanel createStatItem(String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(99, 102, 241));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valueLabel);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(labelLabel);

        return panel;
    }

    private JPanel createRankingDetailCard(JobMatchResult result, int rank) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Constants.BORDER_COLOR, 1),
            new EmptyBorder(20, 24, 20, 24)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // 顶部：排名、职位信息、匹配度百分比（醒目显示）
        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        // 排名徽章
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

        // 排名和职位信息区域
        JPanel leftArea = new JPanel();
        leftArea.setLayout(new BoxLayout(leftArea, BoxLayout.Y_AXIS));
        leftArea.setOpaque(false);

        // 排名标签
        JPanel rankBadgePanel = new JPanel();
        rankBadgePanel.setLayout(new BoxLayout(rankBadgePanel, BoxLayout.X_AXIS));
        rankBadgePanel.setOpaque(false);

        JLabel rankBadge = new JLabel(rankLabel);
        rankBadge.setFont(new Font("Segoe UI", Font.BOLD, 32));
        rankBadge.setForeground(rankColor);
        rankBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        rankBadgePanel.add(rankBadge);

        JLabel matchLabel = new JLabel("  MATCH");
        matchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        matchLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        matchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rankBadgePanel.add(matchLabel);

        leftArea.add(rankBadgePanel);

        // 职位标题
        JLabel titleLabel = new JLabel(result.job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftArea.add(Box.createVerticalStrut(8));
        leftArea.add(titleLabel);

        // 职位元信息
        JLabel metaLabel = new JLabel(result.job.getCourseCode() + " • " + result.job.getDepartment() + " • " + result.job.getInstructorName());
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        metaLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftArea.add(Box.createVerticalStrut(4));
        leftArea.add(metaLabel);

        topRow.add(leftArea, BorderLayout.WEST);

        // 右侧：醒目的大号匹配度百分比
        JPanel scoreArea = new JPanel();
        scoreArea.setLayout(new BoxLayout(scoreArea, BoxLayout.Y_AXIS));
        scoreArea.setOpaque(false);
        scoreArea.setBorder(new EmptyBorder(0, 20, 0, 0));

        Color scoreColor = getScoreColor(result.score);

        JLabel scoreLabel = new JLabel(String.format("%.0f%%", result.score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        scoreLabel.setForeground(scoreColor);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreArea.add(scoreLabel);

        // 匹配度标签
        JLabel scoreTextLabel = new JLabel("Match Score");
        scoreTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        scoreTextLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        scoreTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreArea.add(scoreTextLabel);

        topRow.add(scoreArea, BorderLayout.EAST);

        card.add(topRow);

        // 分隔线
        JPanel separator = new JPanel();
        separator.setBackground(new Color(229, 231, 235));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setMinimumSize(new Dimension(0, 1));
        separator.setPreferredSize(new Dimension(0, 1));
        card.add(Box.createVerticalStrut(16));
        card.add(separator);
        card.add(Box.createVerticalStrut(16));

        // Summary 部分
        JPanel summaryRow = new JPanel();
        summaryRow.setLayout(new BoxLayout(summaryRow, BoxLayout.X_AXIS));
        summaryRow.setOpaque(false);

        JLabel summaryTitle = new JLabel("Summary:");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        summaryTitle.setPreferredSize(new Dimension(80, 20));
        summaryRow.add(summaryTitle);

        JLabel summaryText = new JLabel("<html><body style='width: 500px'>" + extractBriefAnalysis(result.analysis) + "</body></html>");
        summaryText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryText.setForeground(UI_Constants.TEXT_SECONDARY);
        summaryRow.add(summaryText);

        card.add(summaryRow);

        // Comparison 部分
        JPanel comparisonRow = new JPanel();
        comparisonRow.setLayout(new BoxLayout(comparisonRow, BoxLayout.X_AXIS));
        comparisonRow.setOpaque(false);
        comparisonRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel comparisonTitle = new JLabel("Comparison:");
        comparisonTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        comparisonTitle.setForeground(UI_Constants.TEXT_PRIMARY);
        comparisonTitle.setPreferredSize(new Dimension(90, 20));
        comparisonRow.add(comparisonTitle);

        // 技能匹配
        JPanel skillMatch = createMiniProgressBar("Skills Match", 85, new Color(34, 197, 94));
        comparisonRow.add(skillMatch);
        comparisonRow.add(Box.createHorizontalStrut(24));

        // GPA匹配
        JPanel gpaMatch = createMiniProgressBar("GPA Match", 75, new Color(59, 130, 246));
        comparisonRow.add(gpaMatch);
        comparisonRow.add(Box.createHorizontalStrut(24));

        // 经验匹配
        JPanel expMatch = createMiniProgressBar("Experience", 60, new Color(168, 85, 247));
        comparisonRow.add(expMatch);

        // 详情按钮
        JButton detailBtn = new JButton("View Detail");
        detailBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setBackground(new Color(99, 102, 241));
        detailBtn.setFocusPainted(false);
        detailBtn.setBorderPainted(false);
        detailBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailBtn.addActionListener(e -> showJobDetailReport(result));

        comparisonRow.add(Box.createHorizontalStrut(8));
        comparisonRow.add(detailBtn);

        card.add(comparisonRow);

        // 点击事件
        final JobMatchResult finalResult = result;
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showJobDetailReport(finalResult);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    // 创建小型进度条
    private JPanel createMiniProgressBar(String label, int percentage, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(80, 50));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        labelText.setForeground(UI_Constants.TEXT_SECONDARY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelText);

        // 进度条背景
        JPanel barBg = new JPanel();
        barBg.setLayout(new BoxLayout(barBg, BoxLayout.X_AXIS));
        barBg.setBackground(new Color(229, 231, 235));
        barBg.setBorder(new EmptyBorder(2, 0, 2, 0));
        barBg.setMaximumSize(new Dimension(70, 10));

        // 进度条前景（使用不透明背景显示颜色）
        JPanel barFill = new JPanel();
        barFill.setLayout(new BoxLayout(barFill, BoxLayout.X_AXIS));
        barFill.setBackground(color);
        barFill.setOpaque(true);
        int fillWidth = (int) (70 * percentage / 100.0);
        barFill.setMaximumSize(new Dimension(Math.max(fillWidth, 0), 10));

        barBg.add(barFill);
        barBg.add(Box.createHorizontalGlue());

        panel.add(barBg);

        // 百分比文字（与进度条居中对齐）
        JLabel percentText = new JLabel(percentage + "%");
        percentText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        percentText.setForeground(color);
        percentText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(percentText);

        return panel;
    }

    // 创建分数详情行（用于详情页面）
    private JPanel createScoreRow(String label, double percentage, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // 标签
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelText.setForeground(new Color(55, 65, 81));
        labelText.setPreferredSize(new Dimension(140, 20));
        labelText.setMaximumSize(new Dimension(140, 20));
        labelText.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(labelText);

        // 标签和横条之间的间距
        panel.add(Box.createHorizontalStrut(8));

        // 使用 JProgressBar 代替手动进度条，更好地控制对齐
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setForeground(color);
        progressBar.setBackground(new Color(229, 231, 235));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setMaximumSize(new Dimension(400, 20));
        progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(progressBar);

        // 百分比
        JLabel percentText = new JLabel(String.format("%.0f%%", percentage));
        percentText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        percentText.setForeground(color);
        percentText.setPreferredSize(new Dimension(50, 20));
        percentText.setMaximumSize(new Dimension(50, 20));
        percentText.setHorizontalAlignment(SwingConstants.RIGHT);
        percentText.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(percentText);

        return panel;
    }

    private void buildAIJobDetailPanel() {
        aiJobDetailPanel = new JPanel(new BorderLayout(0, 0));
        aiJobDetailPanel.setBackground(UI_Constants.BG_COLOR);
        aiJobDetailPanel.setVisible(false);
        aiJobDetailPanel.setPreferredSize(new Dimension(900, 600));

        // 顶部导航栏 - 蓝色背景
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(99, 102, 241));
        navBar.setBorder(new EmptyBorder(12, 24, 12, 24));
        navBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton backBtn = new JButton("\u2190 Back to Ranking");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            aiJobDetailPanel.setVisible(false);
            if (aiRankingDetailPanel != null) {
                aiRankingDetailPanel.setVisible(true);
            }
            bottomContainer.revalidate();
            bottomContainer.repaint();
        });
        navBar.add(backBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("AI Match Detail");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        navBar.add(titleLabel, BorderLayout.CENTER);

        JButton closeBtn = new JButton("\u00d7");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 20));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            aiJobDetailPanel.setVisible(false);
            hideAIRankingDetail();
        });
        navBar.add(closeBtn, BorderLayout.EAST);

        aiJobDetailPanel.add(navBar, BorderLayout.NORTH);

        // 内容区域
        aiJobDetailContent = new JPanel();
        aiJobDetailContent.setLayout(new BoxLayout(aiJobDetailContent, BoxLayout.Y_AXIS));
        aiJobDetailContent.setBackground(UI_Constants.BG_COLOR);
        aiJobDetailContent.setBorder(new EmptyBorder(16, 48, 32, 48));

        aiJobDetailScrollPane = new JScrollPane(aiJobDetailContent);
        aiJobDetailScrollPane.setBorder(null);
        aiJobDetailScrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        aiJobDetailScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        aiJobDetailScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        aiJobDetailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        aiJobDetailScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        aiJobDetailPanel.add(aiJobDetailScrollPane, BorderLayout.CENTER);

        bottomContainer.add(aiJobDetailPanel);
    }

    private void populateJobDetailContent(JobMatchResult result) {
        aiJobDetailContent.removeAll();

        // 解析AI返回的数据
        String[] strengths = extractStrengths(result.analysis);
        String[] weaknesses = extractWeaknesses(result.analysis);
        String[] recommendations = extractRecommendations(result.analysis);
        String summary = extractBriefAnalysis(result.analysis);

        // ============ 职位基本信息卡片 ============
        JPanel jobInfoCard = new JPanel(new BorderLayout(16, 0));
        jobInfoCard.setBackground(Color.WHITE);
        jobInfoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(24, 28, 24, 28)
        ));
        jobInfoCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        jobInfoCard.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        JPanel jobInfoLeft = new JPanel();
        jobInfoLeft.setLayout(new BoxLayout(jobInfoLeft, BoxLayout.Y_AXIS));
        jobInfoLeft.setOpaque(false);

        JLabel jobTitleLabel = new JLabel(result.job.getTitle());
        jobTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        jobTitleLabel.setForeground(new Color(31, 41, 55));
        jobInfoLeft.add(jobTitleLabel);

        JLabel jobMetaLabel = new JLabel(result.job.getCourseCode() + "  ·  " + result.job.getDepartment() + "  ·  " + result.job.getInstructorName());
        jobMetaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jobMetaLabel.setForeground(new Color(107, 114, 128));
        jobInfoLeft.add(Box.createVerticalStrut(6));
        jobInfoLeft.add(jobMetaLabel);

        jobInfoCard.add(jobInfoLeft, BorderLayout.WEST);

        // 分数卡片
        JPanel scoreCard = new JPanel(new BorderLayout(0, 4));
        scoreCard.setBackground(new Color(249, 250, 251));
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
        matchTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        matchTextLabel.setForeground(new Color(107, 114, 128));
        matchTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreCard.add(matchTextLabel, BorderLayout.SOUTH);

        jobInfoCard.add(scoreCard, BorderLayout.EAST);

        aiJobDetailContent.add(jobInfoCard);
        aiJobDetailContent.add(Box.createVerticalStrut(16));

        // ============ 分数详情卡片 ============
        JPanel scoreDetailCard = new JPanel();
        scoreDetailCard.setLayout(new BoxLayout(scoreDetailCard, BoxLayout.Y_AXIS));
        scoreDetailCard.setBackground(Color.WHITE);
        scoreDetailCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 24, 20, 24)
        ));
        scoreDetailCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreDetailCard.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        JLabel scoreTitle = new JLabel("Score Breakdown");
        scoreTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scoreTitle.setForeground(new Color(55, 65, 81));
        scoreTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreDetailCard.add(scoreTitle);
        scoreDetailCard.add(Box.createVerticalStrut(12));

        // 分数数据
        double skillsMatch = extractMatchScoreByType(result.analysis, "skillsMatch");
        double gpaMatch = extractMatchScoreByType(result.analysis, "gpaMatch");
        double expMatch = extractMatchScoreByType(result.analysis, "experienceMatch");

        // 创建分数条
        scoreDetailCard.add(createScoreRow("Skills Match", skillsMatch, new Color(59, 130, 246)));
        scoreDetailCard.add(Box.createVerticalStrut(8));
        scoreDetailCard.add(createScoreRow("GPA Match", gpaMatch, new Color(34, 197, 94)));
        scoreDetailCard.add(Box.createVerticalStrut(8));
        scoreDetailCard.add(createScoreRow("Experience Match", expMatch, new Color(168, 85, 247)));
        scoreDetailCard.add(Box.createVerticalStrut(8));
        scoreDetailCard.add(createScoreRow("Overall Match", result.score, getScoreColor(result.score)));

        aiJobDetailContent.add(scoreDetailCard);
        aiJobDetailContent.add(Box.createVerticalStrut(16));

        // ============ Strengths 卡片（绿色）============
        if (strengths.length > 0) {
            JPanel strengthsCard = createDetailCard("Strengths", strengths,
                new Color(22, 163, 74),   // 深绿色标题
                new Color(220, 252, 231), // 浅绿背景
                new Color(21, 128, 61));  // 深绿色圆点
            strengthsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            strengthsCard.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            aiJobDetailContent.add(strengthsCard);
            aiJobDetailContent.add(Box.createVerticalStrut(16));
        }

        // ============ Weaknesses 卡片（红色）============
        if (weaknesses.length > 0) {
            JPanel weaknessesCard = createDetailCard("Areas for Improvement", weaknesses,
                new Color(185, 28, 28),   // 深红色标题
                new Color(254, 226, 226), // 浅红背景
                new Color(153, 27, 27));  // 深红色圆点
            weaknessesCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            weaknessesCard.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            aiJobDetailContent.add(weaknessesCard);
            aiJobDetailContent.add(Box.createVerticalStrut(16));
        }

        // ============ Recommendations 卡片（蓝色）============
        if (recommendations.length > 0) {
            JPanel recCard = createDetailCard("Recommendations", recommendations,
                new Color(29, 78, 216),   // 深蓝色标题
                new Color(219, 234, 254), // 浅蓝背景
                new Color(30, 64, 175));  // 深蓝色圆点
            recCard.setAlignmentX(Component.CENTER_ALIGNMENT);
            recCard.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            aiJobDetailContent.add(recCard);
            aiJobDetailContent.add(Box.createVerticalStrut(16));
        }

        // ============ 原始AI分析报告 ============
        JPanel rawAnalysisCard = new JPanel();
        rawAnalysisCard.setLayout(new BoxLayout(rawAnalysisCard, BoxLayout.Y_AXIS));
        rawAnalysisCard.setBackground(Color.WHITE);
        rawAnalysisCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 24, 20, 24)
        ));
        rawAnalysisCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        rawAnalysisCard.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        JLabel rawTitle = new JLabel("AI Raw Analysis");
        rawTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        rawTitle.setForeground(new Color(107, 114, 128));
        rawTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rawAnalysisCard.add(rawTitle);
        rawAnalysisCard.add(Box.createVerticalStrut(10));

        JTextArea rawArea = new JTextArea(result.analysis);
        rawArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // 使用等宽字体更易读
        rawArea.setForeground(new Color(55, 65, 81));
        rawArea.setBackground(new Color(249, 250, 251));
        rawArea.setBorder(new EmptyBorder(12, 16, 12, 16));
        rawArea.setLineWrap(true);
        rawArea.setWrapStyleWord(true);
        rawArea.setEditable(false);
        rawArea.setRows(6);

        JScrollPane rawScroll = new JScrollPane(rawArea);
        rawScroll.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        rawScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rawScroll.getViewport().setBackground(new Color(249, 250, 251));

        rawAnalysisCard.add(rawScroll);
        aiJobDetailContent.add(rawAnalysisCard);
        aiJobDetailContent.add(Box.createVerticalStrut(16));

        // ============ 查看完整职位信息按钮 ============
        JButton viewJobBtn = new JButton("View Full Job Details");
        viewJobBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewJobBtn.setForeground(Color.WHITE);
        viewJobBtn.setBackground(UI_Constants.PRIMARY_COLOR);
        viewJobBtn.setOpaque(true);
        viewJobBtn.setFocusPainted(false);
        viewJobBtn.setBorderPainted(false);
        viewJobBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewJobBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        viewJobBtn.addActionListener(e -> {
            aiJobDetailPanel.setVisible(false);
            hideAIRankingDetail();
            callback.onViewJobDetail(result.job);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(viewJobBtn);
        aiJobDetailContent.add(btnPanel);

        aiJobDetailContent.add(Box.createVerticalGlue());
        aiJobDetailContent.revalidate();
        aiJobDetailContent.repaint();
    }

    // 创建详情卡片（用于显示strengths, weaknesses, recommendations）
    private JPanel createDetailCard(String title, String[] items, Color titleColor, Color itemBgColor, Color bulletColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            new EmptyBorder(20, 24, 20, 24)
        ));

        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(titleColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(14));

        // 项目列表
        for (String item : items) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
            itemPanel.setOpaque(false);
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            // 圆点
            JLabel bullet = new JLabel("\u2022");
            bullet.setFont(new Font("Segoe UI", Font.BOLD, 18));
            bullet.setForeground(bulletColor);
            bullet.setPreferredSize(new Dimension(28, 28));
            bullet.setVerticalAlignment(SwingConstants.TOP);
            itemPanel.add(bullet);

            // 项目文字 - 不使用固定宽度，让它自动换行
            JTextArea itemText = new JTextArea(item);
            itemText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            itemText.setForeground(new Color(55, 65, 81));
            itemText.setBackground(itemBgColor);
            itemText.setLineWrap(true);
            itemText.setWrapStyleWord(true);
            itemText.setEditable(false);
            itemText.setBorder(new EmptyBorder(4, 8, 8, 8));
            itemPanel.add(itemText);

            card.add(itemPanel);
            card.add(Box.createVerticalStrut(8));
        }

        return card;
    }

    private void showJobDetailReport(JobMatchResult result) {
        // 隐藏排名详情，显示职位详细报告
        aiRankingDetailPanel.setVisible(false);

        // 创建详细报告面板
        if (aiJobDetailPanel == null) {
            buildAIJobDetailPanel();
        }

        // 填充报告内容
        populateJobDetailContent(result);

        aiJobDetailPanel.setVisible(true);

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
        aiRankingDetailPanel.setBackground(UI_Constants.BG_COLOR);
        aiRankingDetailPanel.setVisible(false);
        aiRankingDetailPanel.setPreferredSize(new Dimension(900, 600));

        // 顶部导航栏 - 带蓝色背景
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(99, 102, 241));
        navBar.setBorder(new EmptyBorder(16, 24, 16, 24));
        navBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));

        JButton backBtn = new JButton("\u2190 Back to Jobs");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> hideAIRankingDetail());
        navBar.add(backBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("AI Match Ranking");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        navBar.add(titleLabel, BorderLayout.CENTER);

        JButton closeBtn = new JButton("\u00d7");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 20));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> hideAIRankingDetail());
        navBar.add(closeBtn, BorderLayout.EAST);

        aiRankingDetailPanel.add(navBar, BorderLayout.NORTH);

        // 内容区域 - 可滚动的结果列表
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UI_Constants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(16, 24, 32, 24));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Constants.BG_COLOR);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        aiRankingDetailPanel.add(scrollPane, BorderLayout.CENTER);

        // 存储内容面板引用以便后续填充
        aiRankingDetailContent = contentPanel;

        bottomContainer.add(aiRankingDetailPanel);
    }

    private JPanel aiRankingDetailContent; // 排名详情内容面板

    private void hideAIRankingDetail() {
        // 隐藏所有AI相关面板
        if (aiJobDetailPanel != null) aiJobDetailPanel.setVisible(false);
        if (aiRankingDetailPanel != null) aiRankingDetailPanel.setVisible(false);
        if (jobsListPanel != null) jobsListPanel.setVisible(true);
        if (northStack != null) northStack.setVisible(true);
        // 返回时重新显示AI结果面板（如果有结果）
        if (cachedMatchResults != null && aiResultsPanel != null) {
            aiResultsPanel.setVisible(true);
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
        sortByMatchBtn.setText("Reset Sort");
        sortByMatchBtn.setBackground(new Color(107, 114, 128));
        refreshJobsList();
    }

    private String buildUserProfile() {
        StringBuilder profile = new StringBuilder();
        TAUser currentUser = dataService.getCurrentUser();
        if (currentUser != null) {
            TAUser.Skills skills = currentUser.getSkills();
            if (skills != null) {
                List<String> allSkills = new ArrayList<>();
                if (skills.getProgramming() != null) {
                    for (TAUser.Skill s : skills.getProgramming()) {
                        if (s.getName() != null) allSkills.add(s.getName());
                    }
                }
                if (skills.getTeaching() != null) {
                    for (TAUser.Skill s : skills.getTeaching()) {
                        if (s.getName() != null) allSkills.add(s.getName());
                    }
                }
                if (skills.getCommunication() != null) {
                    for (TAUser.Skill s : skills.getCommunication()) {
                        if (s.getName() != null) allSkills.add(s.getName());
                    }
                }
                if (skills.getOther() != null) {
                    for (TAUser.Skill s : skills.getOther()) {
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
        }
        return profile.length() > 0 ? profile.toString() : "No profile information available";
    }

    private double extractMatchScore(String analysis) {
        if (analysis == null) return 0;
        // 支持两种格式: "85%" 或 "matchScore": 85
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("\"matchScore\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
        java.util.regex.Matcher matcher1 = pattern1.matcher(analysis);
        if (matcher1.find()) {
            try {
                return Double.parseDouble(matcher1.group(1));
            } catch (NumberFormatException e) {}
        }
        return 0;
    }

    // 提取特定类型的分数
    private double extractMatchScoreByType(String analysis, String type) {
        if (analysis == null) return 0;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"" + type + "\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
        java.util.regex.Matcher matcher = pattern.matcher(analysis);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {}
        }
        return 0;
    }

    // 解析JSON数组的正确方法（处理引号内可能包含逗号的情况）
    private String[] extractJsonArray(String analysis, String fieldName) {
        if (analysis == null || analysis.isEmpty()) return new String[0];

        try {
            // 尝试使用Gson解析JSON
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(analysis).getAsJsonObject();
            if (root.has(fieldName)) {
                com.google.gson.JsonArray arr = root.getAsJsonArray(fieldName);
                String[] result = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    result[i] = arr.get(i).getAsString();
                }
                return result;
            }
        } catch (Exception e) {
            // 如果Gson解析失败，使用正则表达式作为后备
            // 匹配数组内容，忽略嵌套的引号
            String patternStr = "\"" + fieldName + "\"\\s*:\\s*\\[([^\\]]*)\\]";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
            java.util.regex.Matcher matcher = pattern.matcher(analysis);
            if (matcher.find()) {
                String arr = matcher.group(1);
                // 智能分割：按逗号分割但忽略引号内的逗号
                List<String> items = new java.util.ArrayList<>();
                int depth = 0;
                int start = 0;
                for (int i = 0; i < arr.length(); i++) {
                    char c = arr.charAt(i);
                    if (c == '"' && (i == 0 || arr.charAt(i - 1) != '\\')) {
                        depth++;
                    }
                    if (c == ',' && depth % 2 == 0) {
                        String item = arr.substring(start, i).trim();
                        item = item.replace("\"", "");
                        if (!item.isEmpty()) {
                            items.add(item);
                        }
                        start = i + 1;
                    }
                }
                // 添加最后一个元素
                String lastItem = arr.substring(start).trim().replace("\"", "");
                if (!lastItem.isEmpty()) {
                    items.add(lastItem);
                }
                return items.toArray(new String[0]);
            }
        }
        return new String[0];
    }

    // 解析AI返回的JSON，获取Strengths
    private String[] extractStrengths(String analysis) {
        return extractJsonArray(analysis, "strengths");
    }

    // 解析AI返回的JSON，获取Weaknesses
    private String[] extractWeaknesses(String analysis) {
        return extractJsonArray(analysis, "weaknesses");
    }

    // 解析AI返回的JSON，获取Recommendations
    private String[] extractRecommendations(String analysis) {
        return extractJsonArray(analysis, "recommendations");
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
        String analysisPreview = extractBriefAnalysis(result.analysis);
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

    private String extractBriefAnalysis(String analysis) {
        if (analysis == null || analysis.isEmpty()) {
            return "Analysis in progress...";
        }
        String[] lines = analysis.split("\n");
        StringBuilder brief = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && count < 2) {
                brief.append(line).append(" ");
                count++;
            }
        }
        String result = brief.toString().trim();
        if (result.length() > 150) {
            result = result.substring(0, 147) + "...";
        }
        return result.isEmpty() ? "Tap to view full analysis" : result;
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
                applyBtn.setBackground(new Color(99, 102, 241));
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
            if (userSkills.getProgramming() != null) {
                for (TAUser.Skill s : userSkills.getProgramming()) {
                    if (s.getName() != null) skills.add(s.getName());
                }
            }
            if (userSkills.getTeaching() != null) {
                for (TAUser.Skill s : userSkills.getTeaching()) {
                    if (s.getName() != null) skills.add(s.getName());
                }
            }
            if (userSkills.getCommunication() != null) {
                for (TAUser.Skill s : userSkills.getCommunication()) {
                    if (s.getName() != null) skills.add(s.getName());
                }
            }
            if (userSkills.getOther() != null) {
                for (TAUser.Skill s : userSkills.getOther()) {
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

    private String getDoubaoAPIKey() {
        return System.getProperty("doubao.api.key",
            System.getenv("ARK_API_KEY") != null ? System.getenv("ARK_API_KEY") : System.getenv("DOUBao_API_KEY"));
    }

    private static class JobMatchResult {
        Job job;
        double score;
        String analysis;

        JobMatchResult(Job job, double score, String analysis) {
            this.job = job;
            this.score = score;
            this.analysis = analysis;
        }
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

        
        boolean hasApplied = dataService.hasAppliedToJob(job.getJobId());
        Color stripColor = hasApplied ? new Color(34, 197, 94) : new Color(209, 213, 219);
        JPanel stripPanel = new JPanel();
        stripPanel.setOpaque(true);
        stripPanel.setBackground(stripColor);
        stripPanel.setPreferredSize(new Dimension(6, 0));
        card.add(stripPanel, BorderLayout.WEST);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 14, 0, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        if (hasApplied) {
            JLabel appliedBadge = new JLabel("Applied");
            appliedBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            appliedBadge.setForeground(new Color(22, 163, 74));
            appliedBadge.setBackground(new Color(220, 252, 231));
            appliedBadge.setOpaque(true);
            appliedBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
            c.insets = new Insets(0, 0, 6, 0);
            center.add(appliedBadge, c);
            c.gridy++;
        }

        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UI_Constants.TEXT_PRIMARY);
        c.insets = new Insets(0, 0, 6, 0);
        center.add(titleLabel, c);
        c.gridy++;

        String meta = job.getCourseCode() + "  \u2022  " + job.getDepartment() + "  \u2022  " + job.getInstructorName();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        metaLabel.setForeground(UI_Constants.TEXT_SECONDARY);
        c.insets = new Insets(0, 0, 8, 0);
        center.add(metaLabel, c);
        c.gridy++;

        String summary = job.getSummary();
        if (summary == null || summary.isEmpty()) {
            summary = job.getDescription();
        }
        if (summary != null && summary.length() > 120) {
            summary = summary.substring(0, 117) + "...";
        }
        JTextArea sumArea = new JTextArea(summary != null ? summary : "");
        sumArea.setLineWrap(true);
        sumArea.setWrapStyleWord(true);
        sumArea.setEditable(false);
        sumArea.setFocusable(false);
        sumArea.setOpaque(false);
        sumArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sumArea.setForeground(new Color(75, 85, 99));
        sumArea.setBorder(null);
        sumArea.setMargin(new Insets(0, 0, 0, 0));
        sumArea.setRows(1);
        c.insets = new Insets(0, 0, 14, 0);
        center.add(sumArea, c);
        c.gridy++;

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        footer.setOpaque(false);
        footer.add(createMetaChip("Hours:", job.getWeeklyHoursDisplay()));
        footer.add(createMetaChip("Deadline:", formatDeadline(job)));
        footer.add(createMetaChip("Location:", job.getLocationMode()));
        c.insets = new Insets(0, 0, 0, 0);
        center.add(footer, c);

        JButton viewBtn = UI_Helper.createDarkButton("View Details  >");
        viewBtn.setPreferredSize(new Dimension(160, 44));
        viewBtn.setMaximumSize(new Dimension(160, 44));
        viewBtn.addActionListener(e -> callback.onViewJobDetail(job));
        JPanel eastWrap = new JPanel(new BorderLayout());
        eastWrap.setOpaque(false);
        eastWrap.setBorder(new EmptyBorder(0, 0, 0, 4));
        eastWrap.add(viewBtn, BorderLayout.NORTH);

        card.add(center, BorderLayout.CENTER);
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

