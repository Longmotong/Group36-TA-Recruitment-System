package com.ebuko.moapp.ui;

import com.ebuko.moapp.data.DataRepository;
import com.ebuko.moapp.data.StatusConfig;
import com.ebuko.moapp.ui.theme.RoundedPanel;
import com.ebuko.moapp.ui.theme.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * UI flow:
 * 1) TA Applications list
 * 2) TA Application Detail
 * 3) Review TA Application
 * 4) My Review Records
 */
public class ApplicationReviewPanelV2 extends JPanel {
  private enum Page {
    LIST,
    DETAIL,
    REVIEW,
    RECORDS
  }

  private final DataRepository repo;
  private final StatusConfig statusConfig;

  private String currentMoUserId;

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel pagePanel = new JPanel(cardLayout);

  private String selectedApplicationId = null;

  // List UI
  private final JTextField searchField = new JTextField();
  private final JComboBox<String> courseCombo = new JComboBox<>();
  private final JComboBox<String> statusCombo = new JComboBox<>();

  private final JLabel statsTotal = new JLabel("-");
  private final JLabel statsPending = new JLabel("-");
  private final JLabel statsApproved = new JLabel("-");
  private final JLabel statsRejected = new JLabel("-");

  // Records stats
  private final JLabel recordsTotal = new JLabel("-");
  private final JLabel recordsApproved = new JLabel("-");
  private final JLabel recordsRejected = new JLabel("-");

  private final DefaultTableModel tableModel;
  private final JTable table;

  private final JButton recordsLink = new JButton("View My Review Records ->");

  // Detail UI
  private final JPanel detailPanel;
  private final JLabel detailTitle = new JLabel("TA Application Detail");
  private final JLabel detailSummary = new JLabel();

  // Review UI
  private final JRadioButton approveRadio = new JRadioButton("Approve Application");
  private final JRadioButton rejectRadio = new JRadioButton("Reject Application");
  private final JTextArea reviewNotesArea = new JTextArea();
  private final JButton submitReviewBtn = new JButton("Submit Review");
  private final JButton cancelReviewBtn = new JButton("Cancel");

  // Records UI
  private final JTextField recordsSearchField = new JTextField();
  private final JComboBox<String> recordsTimeCombo = new JComboBox<>(new String[]{"All Time"});
  private final JComboBox<String> recordsResultCombo = new JComboBox<>(new String[]{"All Results"});
  private final DefaultTableModel recordsModel;
  private final JTable recordsTable;

  private final Consumer<Void> backToHomeCallback;

  public ApplicationReviewPanelV2(
      DataRepository repo,
      StatusConfig statusConfig,
      String currentMoUserId,
      Consumer<Void> backToHomeCallback
  ) {
    super(new BorderLayout());
    this.repo = repo;
    this.statusConfig = statusConfig;
    this.currentMoUserId = currentMoUserId;
    this.backToHomeCallback = backToHomeCallback;

    setBackground(Theme.APP_BG);

    // -------- List Page --------
    JPanel listPage = new JPanel(new BorderLayout());
    listPage.setOpaque(false);

    JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    topRow.setOpaque(false);
    JButton backBtn = new JButton("< Back to Home");
    backBtn.setFocusPainted(false);
    backBtn.setBorderPainted(false);
    backBtn.setOpaque(false);
    backBtn.setForeground(new Color(80, 80, 80));
    backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    backBtn.addActionListener(e -> backToHomeCallback.accept(null));
    topRow.add(backBtn);

    listPage.add(topRow, BorderLayout.NORTH);

    JPanel listHeader = new JPanel();
    listHeader.setOpaque(false);
    listHeader.setLayout(new BoxLayout(listHeader, BoxLayout.Y_AXIS));
    listHeader.setBorder(BorderFactory.createEmptyBorder(6, 16, 10, 16));

    JLabel title = new JLabel("TA Applications");
    title.setFont(Theme.FONT_BOLD_28.deriveFont(26f));
    title.setForeground(Theme.TEXT);
    JLabel sub = new JLabel("Review and manage Teaching Assistant applications");
    sub.setFont(Theme.FONT_PLAIN_14);
    sub.setForeground(Theme.MUTED);
    listHeader.add(title);
    listHeader.add(Box.createVerticalStrut(4));
    listHeader.add(sub);
    listPage.add(listHeader, BorderLayout.BEFORE_FIRST_LINE);

    JPanel listContent = new JPanel();
    listContent.setOpaque(false);
    listContent.setLayout(new BorderLayout());
    listContent.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

    // Stats cards
    JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
    statsRow.setOpaque(false);
    statsRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 14, 0));

    statsTotal.setFont(Theme.FONT_BOLD_22);
    statsPending.setFont(Theme.FONT_BOLD_22);
    statsApproved.setFont(Theme.FONT_BOLD_22);
    statsRejected.setFont(Theme.FONT_BOLD_22);

    statsRow.add(statCard(statsTotal, "Total Applications"));
    statsRow.add(statCard(statsPending, "Pending Reviews"));
    statsRow.add(statCard(statsApproved, "Approved"));
    statsRow.add(statCard(statsRejected, "Rejected"));
    listContent.add(statsRow, BorderLayout.NORTH);

    // Filters
    JPanel filters = new JPanel();
    filters.setOpaque(false);
    filters.setLayout(new GridLayout(1, 3, 16, 0));

    RoundedPanel searchBox = new RoundedPanel(10);
    searchBox.setBorderColor(Theme.CARD_BORDER);
    searchBox.setLayout(new BorderLayout());
    searchBox.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

    searchField.setBorder(BorderFactory.createEmptyBorder());
    searchField.setForeground(Theme.TEXT);
    searchField.setPreferredSize(new Dimension(260, 36));
    searchBox.add(searchField);

    courseCombo.setPreferredSize(new Dimension(240, 36));
    statusCombo.setPreferredSize(new Dimension(220, 36));
    courseCombo.addItem("All Courses");
    statusCombo.addItem("All Status");

    // Put search into a panel with icon space (visual alignment)
    JPanel courseWrap = new JPanel(new BorderLayout());
    courseWrap.setOpaque(false);
    courseWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    courseWrap.add(new JLabel(""), BorderLayout.NORTH);
    courseWrap.add(courseCombo, BorderLayout.CENTER);

    JPanel statusWrap = new JPanel(new BorderLayout());
    statusWrap.setOpaque(false);
    statusWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    statusWrap.add(new JLabel(""), BorderLayout.NORTH);
    statusWrap.add(statusCombo, BorderLayout.CENTER);

    filters.add(searchBox);
    filters.add(courseCombo);
    filters.add(statusCombo);
    listContent.add(filters, BorderLayout.BEFORE_FIRST_LINE);

    // Table
    String[] cols = new String[]{
        "TA Name",
        "Student ID",
        "Applied Course",
        "Match Score",
        "Missing Skills",
        "Current Workload",
        "Status",
        "Actions"
    };
    tableModel = new DefaultTableModel(cols, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table = new JTable(tableModel);
    table.setRowHeight(52);
    table.setFillsViewportHeight(true);
    table.setBackground(Theme.APP_BG);
    table.setShowGrid(false);

    JTableHeader header = table.getTableHeader();
    header.setReorderingAllowed(false);
    header.setBackground(new Color(250, 250, 250));
    header.setForeground(Theme.MUTED);

    listContent.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel listBottom = new JPanel(new BorderLayout());
    listBottom.setOpaque(false);
    recordsLink.setForeground(Theme.LINK);
    recordsLink.setBorderPainted(false);
    recordsLink.setOpaque(false);
    recordsLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    recordsLink.setHorizontalAlignment(SwingConstants.RIGHT);
    recordsLink.addActionListener(e -> switchToPage(Page.RECORDS));
    listBottom.add(new JPanel(), BorderLayout.CENTER);
    listBottom.add(recordsLink, BorderLayout.SOUTH);
    listBottom.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
    listContent.add(listBottom, BorderLayout.SOUTH);

    // -------- Detail Page --------
    detailPanel = new JPanel(new BorderLayout());
    detailPanel.setOpaque(false);
    JPanel detailTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    detailTop.setOpaque(false);
    JButton backDetailBtn = new JButton("< Back to Application Detail");
    backDetailBtn.setFocusPainted(false);
    backDetailBtn.setBorderPainted(false);
    backDetailBtn.setOpaque(false);
    backDetailBtn.setForeground(new Color(80, 80, 80));
    backDetailBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    backDetailBtn.addActionListener(e -> switchToPage(Page.LIST));
    detailTop.add(backDetailBtn);
    detailPanel.add(detailTop, BorderLayout.NORTH);
    detailTitle.setFont(Theme.FONT_BOLD_28.deriveFont(24f));
    detailTitle.setForeground(Theme.TEXT);
    detailSummary.setFont(Theme.FONT_PLAIN_14);
    detailSummary.setForeground(Theme.MUTED);
    JPanel heading = new JPanel();
    heading.setOpaque(false);
    heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
    heading.add(detailTitle);
    heading.add(detailSummary);
    detailPanel.add(heading, BorderLayout.BEFORE_FIRST_LINE);

    JPanel detailCards = new JPanel();
    detailCards.setOpaque(false);
    detailCards.setLayout(new BoxLayout(detailCards, BoxLayout.Y_AXIS));
    detailCards.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
    detailCards.add(makeInfoCard("Application Information"));
    detailCards.add(Box.createVerticalStrut(12));
    detailCards.add(makeInfoCard("Skills Matching Analysis"));
    detailCards.add(Box.createVerticalStrut(12));
    detailCards.add(makeInfoCard("Current TA Assignments & Workload"));
    detailCards.add(Box.createVerticalStrut(12));
    detailCards.add(makeInfoCard("Personal Information"));
    detailCards.add(Box.createVerticalStrut(12));
    detailCards.add(makeInfoCard("Experience & Background"));
    detailPanel.add(new JScrollPane(detailCards), BorderLayout.CENTER);

    JButton reviewNowBtn = styledBlackButton("Review Now");
    reviewNowBtn.addActionListener(e -> switchToPage(Page.REVIEW));
    JPanel reviewNowWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    reviewNowWrap.setOpaque(false);
    reviewNowWrap.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
    reviewNowWrap.add(reviewNowBtn);
    detailPanel.add(reviewNowWrap, BorderLayout.SOUTH);

    // -------- Review Page --------
    JPanel reviewPage = new JPanel(new BorderLayout());
    reviewPage.setOpaque(false);
    reviewPage.setBackground(Theme.APP_BG);
    JPanel reviewTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    reviewTop.setOpaque(false);
    JButton backReviewBtn = new JButton("< Back to Application Review");
    backReviewBtn.setFocusPainted(false);
    backReviewBtn.setBorderPainted(false);
    backReviewBtn.setOpaque(false);
    backReviewBtn.setForeground(new Color(80, 80, 80));
    backReviewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    backReviewBtn.addActionListener(e -> switchToPage(Page.LIST));
    reviewTop.add(backReviewBtn);
    reviewPage.add(reviewTop, BorderLayout.NORTH);

    JPanel reviewInner = new JPanel();
    reviewInner.setOpaque(false);
    reviewInner.setLayout(new BoxLayout(reviewInner, BoxLayout.Y_AXIS));
    reviewInner.setBorder(BorderFactory.createEmptyBorder(6, 16, 16, 16));

    JLabel reviewTitle = new JLabel("Review TA Application");
    reviewTitle.setFont(Theme.FONT_BOLD_28.deriveFont(26f));
    reviewTitle.setForeground(Theme.TEXT);
    JLabel reviewSub = new JLabel("Evaluate the applicant and make a decision on their application");
    reviewSub.setFont(Theme.FONT_PLAIN_14);
    reviewSub.setForeground(Theme.MUTED);
    reviewInner.add(reviewTitle);
    reviewInner.add(Box.createVerticalStrut(4));
    reviewInner.add(reviewSub);
    reviewInner.add(Box.createVerticalStrut(14));

    JPanel reviewRow = new JPanel(new GridLayout(1, 2, 14, 0));
    reviewRow.setOpaque(false);
    reviewRow.add(makeInfoCard("Course Requirements"));
    reviewRow.add(makeInfoCard("Applicant Qualifications"));
    reviewInner.add(reviewRow);
    reviewInner.add(Box.createVerticalStrut(12));
    // Review decision card (matches the original prototype layout)

    JPanel decisionBox = new JPanel();
    decisionBox.setOpaque(false);
    decisionBox.setLayout(new BoxLayout(decisionBox, BoxLayout.Y_AXIS));
    decisionBox.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
    decisionBox.setBackground(Theme.CARD_BG);

    JLabel decisionTitle = new JLabel("Review Decision");
    decisionTitle.setFont(Theme.FONT_BOLD_16);
    decisionTitle.setForeground(Theme.TEXT);
    decisionBox.add(decisionTitle);
    decisionBox.add(Box.createVerticalStrut(10));

    JLabel resultLabel = new JLabel("Review Result");
    resultLabel.setFont(Theme.FONT_PLAIN_14);
    resultLabel.setForeground(Theme.MUTED);
    decisionBox.add(resultLabel);
    decisionBox.add(Box.createVerticalStrut(8));

    ButtonGroup group = new ButtonGroup();
    group.add(approveRadio);
    group.add(rejectRadio);
    approveRadio.setOpaque(false);
    rejectRadio.setOpaque(false);

    JPanel approveChoice = new JPanel(new BorderLayout());
    approveChoice.setOpaque(false);
    approveChoice.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
    approveChoice.setBackground(Color.WHITE);
    approveChoice.setPreferredSize(new Dimension(520, 60));
    JPanel approveLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
    approveLeft.setOpaque(false);
    approveLeft.add(approveRadio);
    approveLeft.add(new JLabel("Approve Application"));
    approveChoice.add(approveLeft, BorderLayout.NORTH);
    decisionBox.add(approveChoice);
    decisionBox.add(Box.createVerticalStrut(10));

    JPanel rejectChoice = new JPanel(new BorderLayout());
    rejectChoice.setOpaque(false);
    rejectChoice.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
    rejectChoice.setBackground(Color.WHITE);
    rejectChoice.setPreferredSize(new Dimension(520, 60));
    JPanel rejectLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
    rejectLeft.setOpaque(false);
    rejectLeft.add(rejectRadio);
    rejectLeft.add(new JLabel("Reject Application"));
    rejectChoice.add(rejectLeft, BorderLayout.NORTH);
    decisionBox.add(rejectChoice);
    decisionBox.add(Box.createVerticalStrut(12));

    JLabel notesLabel = new JLabel("Review Notes");
    notesLabel.setFont(Theme.FONT_PLAIN_14);
    notesLabel.setForeground(Theme.TEXT);
    decisionBox.add(notesLabel);
    decisionBox.add(Box.createVerticalStrut(6));

    reviewNotesArea.setLineWrap(true);
    reviewNotesArea.setWrapStyleWord(true);
    reviewNotesArea.setRows(5);
    JScrollPane notesScroll = new JScrollPane(reviewNotesArea);
    notesScroll.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
    decisionBox.add(notesScroll);
    decisionBox.add(Box.createVerticalStrut(6));

    JLabel opt = new JLabel("Optional: Provide context for your decision");
    opt.setFont(Theme.FONT_PLAIN_14);
    opt.setForeground(Theme.MUTED);
    decisionBox.add(opt);

    reviewInner.add(decisionBox);
    reviewPage.add(new JScrollPane(reviewInner), BorderLayout.CENTER);

    // Submit row
    submitButtons(reviewPage);

    // -------- Records Page --------
    JPanel recordsPage = new JPanel(new BorderLayout());
    recordsPage.setOpaque(false);
    recordsPage.setBackground(Theme.APP_BG);

    JPanel recordsTop = new JPanel(new BorderLayout());
    recordsTop.setOpaque(false);
    JButton backToListFromRecords = new JButton("< Back to Application Review");
    backToListFromRecords.setFocusPainted(false);
    backToListFromRecords.setBorderPainted(false);
    backToListFromRecords.setOpaque(false);
    backToListFromRecords.setForeground(new Color(80, 80, 80));
    backToListFromRecords.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    backToListFromRecords.addActionListener(e -> switchToPage(Page.LIST));
    recordsTop.add(backToListFromRecords, BorderLayout.NORTH);

    JLabel recTitle = new JLabel("My Review Records");
    recTitle.setFont(Theme.FONT_BOLD_28.deriveFont(26f));
    recTitle.setForeground(Theme.TEXT);
    JLabel recSub = new JLabel("View your application review history and decisions");
    recSub.setFont(Theme.FONT_PLAIN_14);
    recSub.setForeground(Theme.MUTED);

    JPanel recHead = new JPanel();
    recHead.setOpaque(false);
    recHead.setLayout(new BoxLayout(recHead, BoxLayout.Y_AXIS));
    recHead.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    recHead.add(recTitle);
    recHead.add(Box.createVerticalStrut(4));
    recHead.add(recSub);
    recordsTop.add(recHead, BorderLayout.CENTER);
    recordsPage.add(recordsTop, BorderLayout.NORTH);

    JPanel recStatsRow = new JPanel(new GridLayout(1, 3, 14, 0));
    recStatsRow.setOpaque(false);
    recStatsRow.add(statCard(recordsTotal, "Total Reviews"));
    recStatsRow.add(statCard(recordsApproved, "Approved Applications"));
    recStatsRow.add(statCard(recordsRejected, "Rejected Applications"));

    JPanel recFilters = new JPanel(new GridLayout(1, 3, 16, 0));
    recFilters.setOpaque(false);
    recordsSearchField.setPreferredSize(new Dimension(300, 36));
    recordsSearchField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    recFilters.add(recordsSearchField);
    recFilters.add(recordsTimeCombo);
    recFilters.add(recordsResultCombo);

    recordsPage.add(recStatsRow, BorderLayout.BEFORE_FIRST_LINE);
    JPanel recTableWrap = new JPanel(new BorderLayout());
    recTableWrap.setOpaque(false);
    recordsSearchField.setToolTipText("Search by course or TA name");
    String[] recCols = new String[]{"Course Name / Code", "TA Name", "Student ID", "Review Date", "Result", "Reviewer", "Actions"};
    recordsModel = new DefaultTableModel(recCols, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    recordsTable = new JTable(recordsModel);
    recordsTable.setRowHeight(42);
    recTableWrap.add(new JScrollPane(recordsTable), BorderLayout.CENTER);
    recTableWrap.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
    recordsPage.add(recTableWrap, BorderLayout.CENTER);

    JButton exportBtn = styledBlackButton("Export Records");
    JPanel exportWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    exportWrap.setOpaque(false);
    exportWrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
    exportWrap.add(exportBtn);
    recordsPage.add(exportWrap, BorderLayout.SOUTH);

    pagePanel.add(listPage, Page.LIST.name());
    pagePanel.add(detailPanel, Page.DETAIL.name());
    pagePanel.add(reviewPage, Page.REVIEW.name());
    pagePanel.add(recordsPage, Page.RECORDS.name());
    add(pagePanel, BorderLayout.CENTER);

    // Initialize table visuals and refresh everything
    initTableRenderers();
    refresh();
    switchToPage(Page.LIST);
  }

  public void setCurrentMoUserId(String currentMoUserId) {
    this.currentMoUserId = currentMoUserId;
    refresh();
  }

  private void switchToPage(Page p) {
    cardLayout.show(pagePanel, p.name());
    if (p == Page.RECORDS) {
      refreshRecordsTable();
    }
  }

  private RoundedPanel makeInfoCard(String title) {
    RoundedPanel card = new RoundedPanel(10);
    card.setBorderColor(Theme.CARD_BORDER);
    card.setLayout(new BorderLayout());
    JPanel head = new JPanel(new BorderLayout());
    head.setOpaque(false);
    JLabel t = new JLabel(title);
    t.setFont(Theme.FONT_BOLD_16);
    t.setForeground(Theme.TEXT);
    head.add(t, BorderLayout.NORTH);
    card.add(head, BorderLayout.NORTH);
    JTextArea placeholder = new JTextArea();
    placeholder.setOpaque(false);
    placeholder.setEditable(false);
    placeholder.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    placeholder.setForeground(Theme.MUTED);
    placeholder.setText(" "); // will be populated on selection
    card.add(placeholder, BorderLayout.CENTER);
    return card;
  }

  private void submitButtons(JPanel reviewPage) {
    JPanel submitWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
    submitWrap.setOpaque(false);
    submitWrap.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
    submitReviewBtn.setBackground(Theme.PRIMARY);
    submitReviewBtn.setForeground(Color.WHITE);
    submitReviewBtn.setFocusPainted(false);
    submitReviewBtn.setBorderPainted(false);
    submitReviewBtn.setPreferredSize(new Dimension(140, 38));
    cancelReviewBtn.setPreferredSize(new Dimension(100, 38));
    cancelReviewBtn.setFocusPainted(false);
    cancelReviewBtn.setForeground(Theme.MUTED);
    cancelReviewBtn.setBackground(Color.WHITE);
    cancelReviewBtn.addActionListener(e -> switchToPage(Page.DETAIL));
    submitReviewBtn.addActionListener(e -> submitReview());
    submitWrap.add(submitReviewBtn);
    submitWrap.add(cancelReviewBtn);
    reviewPage.add(submitWrap, BorderLayout.SOUTH);
  }

  private void initTableRenderers() {
    DefaultTableCellRenderer center = new DefaultTableCellRenderer();
    center.setHorizontalAlignment(SwingConstants.CENTER);
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (i >= 3) table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    table.getColumnModel().getColumn(3).setCellRenderer(new MatchScoreRenderer());
    table.getColumnModel().getColumn(4).setCellRenderer(new MissingSkillsRenderer());
    table.getColumnModel().getColumn(5).setCellRenderer(new WorkloadRenderer());
    table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
    table.getColumnModel().getColumn(7).setCellRenderer(new ActionsRenderer());

    // Records actions column (last column)
    if (recordsTable != null && recordsTable.getColumnCount() >= 7) {
      recordsTable.getColumnModel().getColumn(6).setCellRenderer(new RecordsActionsRenderer());
    }
  }

  private static class WorkloadInfo {
    final double hours;
    final int courses;

    WorkloadInfo(double hours, int courses) {
      this.hours = hours;
      this.courses = courses;
    }
  }

  private static class StatusInfo {
    final String current;
    final String label;

    StatusInfo(String current, String label) {
      this.current = current;
      this.label = label;
    }
  }

  private void openDetail(String appId) {
    selectedApplicationId = appId;
    Map<String, Object> app = repo.getApplication(appId);
    if (app == null) {
      detailSummary.setText("Application: " + appId);
    } else {
      Map<String, Object> applicantSnapshot = asObj(app.get("applicantSnapshot"));
      String taName = applicantSnapshot == null ? "" : asString(applicantSnapshot.get("fullName"), "");
      detailSummary.setText("Application: " + appId + "   |   TA: " + taName);
    }
    switchToPage(Page.DETAIL);
  }

  private void openReview(String appId) {
    selectedApplicationId = appId;
    reviewNotesArea.setText("");
    Map<String, Object> app = repo.getApplication(appId);
    String cur = "";
    if (app != null) {
      Map<String, Object> st = asObj(app.get("status"));
      cur = st == null ? "" : asString(st.get("current"), "");
    }
    if ("accepted".equals(cur)) approveRadio.setSelected(true);
    else if ("rejected".equals(cur)) rejectRadio.setSelected(true);
    else approveRadio.setSelected(true);
    switchToPage(Page.REVIEW);
  }

  private void quickDecision(String appId, String decision) {
    repo.updateApplicationReview(appId, currentMoUserId, "", decision, "", "", "");
    refresh();
    switchToPage(Page.LIST);
  }

  private void submitReview() {
    if (selectedApplicationId == null) return;
    String decision = approveRadio.isSelected() ? "accepted" : "rejected";
    repo.updateApplicationReview(
        selectedApplicationId,
        currentMoUserId,
        reviewNotesArea.getText(),
        decision,
        "",
        "",
        ""
    );
    refresh();
    switchToPage(Page.LIST);
  }

  private class MatchScoreRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
    MatchScoreRenderer() {
      setOpaque(true);
      setHorizontalAlignment(SwingConstants.CENTER);
      setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
      setFont(getFont().deriveFont(Font.BOLD, 12f));
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
      int score = 0;
      if (value instanceof Integer i) score = i;
      else if (value != null) {
        try {
          score = Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {}
      }

      Color bg;
      if (score >= 80) bg = new Color(209, 255, 226);
      else if (score >= 60) bg = new Color(255, 242, 204);
      else bg = new Color(255, 214, 214);

      setBackground(isSelected ? new Color(180, 200, 255) : bg);
      setText(String.valueOf(score));
      return this;
    }
  }

  private class MissingSkillsRenderer extends DefaultTableCellRenderer {
    MissingSkillsRenderer() {
      setHorizontalAlignment(SwingConstants.LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
      panel.setOpaque(false);

      List<?> missing = value instanceof List<?> l ? l : null;
      boolean allMet = missing == null || missing.isEmpty();
      if (allMet) {
        JLabel tag = new JLabel("All skills met");
        tag.setOpaque(true);
        tag.setBackground(new Color(209, 255, 226));
        tag.setBorder(BorderFactory.createLineBorder(new Color(74, 222, 128)));
        tag.setFont(Theme.FONT_PLAIN_14.deriveFont(12f));
        tag.setHorizontalAlignment(SwingConstants.CENTER);
        tag.setPreferredSize(new Dimension(120, 22));
        panel.add(tag);
      } else {
        int shown = Math.min(2, missing.size());
        for (int i = 0; i < shown; i++) {
          Object s = missing.get(i);
          String name = s == null ? "" : String.valueOf(s);
          if (name.isBlank()) continue;
          JLabel tag = new JLabel(name);
          tag.setOpaque(true);
          tag.setBackground(new Color(255, 214, 214));
          tag.setBorder(BorderFactory.createLineBorder(new Color(239, 68, 68)));
          tag.setFont(Theme.FONT_PLAIN_14.deriveFont(12f));
          tag.setHorizontalAlignment(SwingConstants.CENTER);
          panel.add(tag);
        }
      }
      return panel;
    }
  }

  private class WorkloadRenderer extends DefaultTableCellRenderer {
    WorkloadRenderer() {
      setHorizontalAlignment(SwingConstants.LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setOpaque(false);

      WorkloadInfo info = value instanceof WorkloadInfo wi ? wi : null;
      double hours = info == null ? 0 : info.hours;
      int courses = info == null ? 0 : info.courses;

      JLabel top = new JLabel();
      JLabel bottom = new JLabel();
      if (hours <= 0.001) {
        top.setText("No assignments");
        top.setForeground(new Color(107, 114, 128));
        bottom.setText("");
      } else {
        top.setText(Math.round(hours) + " hrs/week");
        bottom.setText(courses + (courses == 1 ? " course" : " courses"));

        if (hours >= 15) {
          top.setForeground(new Color(234, 88, 12));
          bottom.setForeground(new Color(234, 88, 12));
          JLabel warn = new JLabel("!");
          warn.setOpaque(true);
          warn.setBackground(new Color(253, 186, 116));
          warn.setBorder(BorderFactory.createLineBorder(new Color(234, 88, 12)));
          warn.setPreferredSize(new Dimension(18, 18));
          panel.add(warn, BorderLayout.WEST);
        } else {
          top.setForeground(Color.BLACK);
          bottom.setForeground(new Color(107, 114, 128));
        }
      }

      JPanel texts = new JPanel();
      texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
      texts.setOpaque(false);
      texts.add(top);
      if (!bottom.getText().isBlank()) texts.add(bottom);
      panel.add(texts, BorderLayout.CENTER);
      return panel;
    }
  }

  private class StatusRenderer extends DefaultTableCellRenderer {
    StatusRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
      setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
      StatusInfo info = value instanceof StatusInfo si ? si : null;
      String cur = info == null ? "" : info.current;
      String label = info == null ? "" : info.label;

      Color bg;
      Color fg = new Color(17, 24, 39);
      if ("pending".equals(cur)) bg = new Color(254, 243, 199);
      else if ("under_review".equals(cur)) bg = new Color(210, 235, 255);
      else if ("accepted".equals(cur)) {
        bg = new Color(209, 255, 226);
        fg = new Color(22, 163, 74);
      } else if ("rejected".equals(cur)) {
        bg = new Color(255, 214, 214);
        fg = new Color(239, 68, 68);
      } else bg = Theme.CARD_BG;

      setBackground(isSelected ? new Color(180, 200, 255) : bg);
      setForeground(fg);

      String second = ("pending".equals(cur) || "under_review".equals(cur)) ? "Review" : "";
      if (!second.isBlank()) setText("<html><div style='text-align:center;'>" + label + "<br/>" + second + "</div></html>");
      else setText(label);
      return this;
    }
  }

  private class ActionsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
    ActionsRenderer() {
      setOpaque(false);
      setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
    }

    private JButton smallBtn(String text) {
      JButton b = new JButton(text);
      b.setFont(Theme.FONT_PLAIN_14.deriveFont(12f));
      b.setPreferredSize(new Dimension(28, 26));
      b.setFocusPainted(false);
      b.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
      b.setBackground(Color.WHITE);
      return b;
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
      removeAll();
      String appId = value == null ? "" : String.valueOf(value);

      JButton viewBtn = smallBtn("V");
      viewBtn.addActionListener(e -> openDetail(appId));

      JButton approveBtn = smallBtn("A");
      approveBtn.setForeground(new Color(22, 163, 74));
      approveBtn.addActionListener(e -> quickDecision(appId, "accepted"));

      JButton rejectBtn = smallBtn("X");
      rejectBtn.setForeground(new Color(239, 68, 68));
      rejectBtn.addActionListener(e -> quickDecision(appId, "rejected"));

      JButton reviewBtn = smallBtn("R");
      reviewBtn.addActionListener(e -> openReview(appId));

      add(viewBtn);
      add(approveBtn);
      add(rejectBtn);
      add(reviewBtn);
      return this;
    }
  }

  private class RecordsActionsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
    RecordsActionsRenderer() {
      setOpaque(false);
      setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
    }

    private JButton smallBtn(String text) {
      JButton b = new JButton(text);
      b.setFont(Theme.FONT_PLAIN_14.deriveFont(12f));
      b.setPreferredSize(new Dimension(28, 26));
      b.setFocusPainted(false);
      b.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER));
      b.setBackground(Color.WHITE);
      return b;
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
      removeAll();
      String appId = value == null ? "" : String.valueOf(value);
      JButton viewBtn = smallBtn("V");
      viewBtn.addActionListener(e -> openDetail(appId));
      add(viewBtn);
      return this;
    }
  }

  public void refresh() {
    // Refresh filter combos and table rows.
    List<String> appIdsAll = repo.getApplicationIdsForMo(currentMoUserId);
    statsTotal.setText(String.valueOf(appIdsAll.size()));
    int pending = 0;
    int approved = 0;
    int rejected = 0;
    for (String appId : appIdsAll) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      Map<String, Object> status = asObj(app.get("status"));
      String st = status == null ? "" : asString(status.get("current"), "");
      if ("pending".equals(st) || "under_review".equals(st)) pending++;
      else if ("accepted".equals(st)) approved++;
      else if ("rejected".equals(st)) rejected++;
    }
    statsPending.setText(String.valueOf(pending));
    statsApproved.setText(String.valueOf(approved));
    statsRejected.setText(String.valueOf(rejected));

    List<String> jobIds = repo.getManagedJobIdsForMo(currentMoUserId);
    Set<String> courseNames = new HashSet<>();
    for (String jobId : jobIds) {
      Map<String, Object> job = repo.getJob(jobId);
      if (job == null) continue;
      Map<String, Object> course = asObj(job.get("course"));
      if (course == null) continue;
      String code = course.get("courseCode") == null ? "" : String.valueOf(course.get("courseCode"));
      String name = course.get("courseName") == null ? "" : String.valueOf(course.get("courseName"));
      String label = code.isBlank() ? name : (code + " - " + name);
      if (!code.isBlank()) courseNames.add(code);
    }

    courseCombo.removeAllItems();
    courseCombo.addItem("All Courses");
    for (String jobId : jobIds) {
      Map<String, Object> job = repo.getJob(jobId);
      Map<String, Object> course = job == null ? null : asObj(job.get("course"));
      if (course == null) continue;
      String code = course.get("courseCode") == null ? "" : String.valueOf(course.get("courseCode"));
      String name = course.get("courseName") == null ? "" : String.valueOf(course.get("courseName"));
      if (code.isBlank()) continue;
      courseCombo.addItem(code + " (" + name + ")");
    }

    statusCombo.removeAllItems();
    statusCombo.addItem("All Status");
    statusCombo.addItem("Pending");
    statusCombo.addItem("Under Review");
    statusCombo.addItem("Approved");
    statusCombo.addItem("Rejected");

    refreshListTable();
    refreshRecordsTable();
  }

  private void refreshListTable() {
    // Clear
    tableModel.setRowCount(0);

    String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
    String courseSel = (String) courseCombo.getSelectedItem();
    String statusSel = (String) statusCombo.getSelectedItem();

    // Map status selection to app.status.current
    String statusValueFilter = null;
    if (statusSel != null) {
      if ("Pending".equals(statusSel)) statusValueFilter = "pending";
      else if ("Under Review".equals(statusSel)) statusValueFilter = "under_review";
      else if ("Approved".equals(statusSel)) statusValueFilter = "accepted";
      else if ("Rejected".equals(statusSel)) statusValueFilter = "rejected";
    }

    String courseCodeFilter = null;
    if (courseSel != null && !"All Courses".equals(courseSel)) {
      // stored format "CODE (Name)" => extract before space
      int idx = courseSel.indexOf(' ');
      courseCodeFilter = idx > 0 ? courseSel.substring(0, idx) : courseSel;
    }

    List<String> appIds = repo.getApplicationIdsForMo(currentMoUserId);
    for (String appId : appIds) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      String jobId = String.valueOf(app.get("jobId"));
      Map<String, Object> jobSnapshot = asObj(app.get("jobSnapshot"));
      String courseCode = jobSnapshot == null ? "" : asString(jobSnapshot.get("courseCode"), "");
      String courseName = jobSnapshot == null ? "" : asString(jobSnapshot.get("courseName"), "");
      String appliedCourse = courseCode.isBlank() ? courseName : courseCode;

      Map<String, Object> status = asObj(app.get("status"));
      String st = status == null ? "" : asString(status.get("current"), "");
      if (statusValueFilter != null && !statusValueFilter.equals(st)) continue;

      Map<String, Object> applicantSnapshot = asObj(app.get("applicantSnapshot"));
      String taName = applicantSnapshot == null ? "" : asString(applicantSnapshot.get("fullName"), "");
      String studentId = applicantSnapshot == null ? "" : asString(applicantSnapshot.get("studentId"), "");

      if (!search.isEmpty()) {
        if (!(taName.toLowerCase().contains(search) || studentId.toLowerCase().contains(search))) continue;
      }
      if (courseCodeFilter != null && !courseCodeFilter.equals(courseCode)) continue;

      double matchScore = computeMatchScore(app, jobId);
      List<String> missingSkills = computeMissingSkills(app, jobId);

      double workloadHours = computeWorkloadHours(taUserIdFromApp(app));
      int workloadCourses = computeWorkloadCourses(taUserIdFromApp(app));

      String statusLabel = statusConfig.labelFor(st);

      Object[] row = new Object[]{
          taName,
          studentId,
          appliedCourse,
          (int) Math.round(matchScore),
          missingSkills,
          new WorkloadInfo(workloadHours, workloadCourses),
          new StatusInfo(st, statusLabel),
          appId
      };
      tableModel.addRow(row);
    }
  }

  private void refreshRecordsTable() {
    if (recordsModel == null) return;
    recordsModel.setRowCount(0);

    List<String> appIds = repo.getApplicationIdsForMo(currentMoUserId);
    List<String> reviewed = new ArrayList<>();
    Map<String, String> reviewDateByApp = new HashMap<>();

    for (String appId : appIds) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      Map<String, Object> review = asObj(app.get("review"));
      if (review == null) continue;
      String reviewedBy = asString(review.get("reviewedBy"), "");
      if (!currentMoUserId.equals(reviewedBy)) continue;

      Map<String, Object> status = asObj(app.get("status"));
      String cur = status == null ? "" : asString(status.get("current"), "");
      if (!("accepted".equals(cur) || "rejected".equals(cur))) continue;

      String reviewAt = asString(review.get("reviewedAt"), "");
      reviewed.add(appId);
      reviewDateByApp.put(appId, reviewAt);
    }

    // Sort by review date desc (ISO strings compare lexicographically)
    reviewed.sort((a, b) -> {
      String da = reviewDateByApp.getOrDefault(a, "");
      String db = reviewDateByApp.getOrDefault(b, "");
      return db.compareTo(da);
    });

    int total = 0;
    int approved = 0;
    int rejected = 0;

    String search = recordsSearchField.getText() == null ? "" : recordsSearchField.getText().trim().toLowerCase();
    for (String appId : reviewed) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;

      Map<String, Object> jobSnapshot = asObj(app.get("jobSnapshot"));
      String courseTitle = jobSnapshot == null ? "" : asString(jobSnapshot.get("title"), "");
      String courseCode = jobSnapshot == null ? "" : asString(jobSnapshot.get("courseCode"), "");
      String courseLabel = courseTitle.isBlank() ? courseCode : (courseTitle + " " + courseCode);

      Map<String, Object> applicantSnapshot = asObj(app.get("applicantSnapshot"));
      String taName = applicantSnapshot == null ? "" : asString(applicantSnapshot.get("fullName"), "");
      String studentId = applicantSnapshot == null ? "" : asString(applicantSnapshot.get("studentId"), "");

      Map<String, Object> status = asObj(app.get("status"));
      String cur = status == null ? "" : asString(status.get("current"), "");
      String resultLabel = statusConfig.labelFor(cur);

      Map<String, Object> review = asObj(app.get("review"));
      String reviewAt = review == null ? "" : asString(review.get("reviewedAt"), "");
      String reviewerId = review == null ? "" : asString(review.get("reviewedBy"), "");
      String reviewer = repo.moFullName(reviewerId);

      if (!search.isBlank()) {
        String hay = (courseLabel + " " + taName).toLowerCase();
        if (!hay.contains(search)) continue;
      }

      total++;
      if ("accepted".equals(cur)) approved++;
      if ("rejected".equals(cur)) rejected++;

      recordsModel.addRow(new Object[]{
          courseLabel,
          taName,
          studentId,
          reviewAt,
          resultLabel,
          reviewer,
          appId
      });
    }

    recordsTotal.setText(String.valueOf(total));
    recordsApproved.setText(String.valueOf(approved));
    recordsRejected.setText(String.valueOf(rejected));
  }

  private String taUserIdFromApp(Map<String, Object> app) {
    return asString(app.get("userId"), "");
  }

  private double computeMatchScore(Map<String, Object> app, String jobId) {
    Map<String, Object> job = repo.getJob(jobId);
    if (job == null) return 0.0;
    Map<String, Object> jobContent = asObj(job.get("content"));
    if (jobContent == null) return 0.0;
    List<Object> required = asList(jobContent.get("preferredSkills"));
    if (required == null || required.isEmpty()) return 0.0;

    Map<String, Object> ta = repo.getTaUser(asString(app.get("userId"), ""));
    if (ta == null) return 0.0;
    Map<String, Object> taSkills = asObj(ta.get("skills"));
    List<String> skillNames = new ArrayList<>();
    if (taSkills != null) {
      addSkillNames(skillNames, taSkills.get("programming"));
      addSkillNames(skillNames, taSkills.get("teaching"));
      addSkillNames(skillNames, taSkills.get("communication"));
    }
    int matched = 0;
    for (Object reqObj : required) {
      String req = String.valueOf(reqObj);
      if (skillNames.stream().anyMatch(s -> s.equalsIgnoreCase(req))) matched++;
    }
    return matched * 100.0 / required.size();
  }

  private List<String> computeMissingSkills(Map<String, Object> app, String jobId) {
    Map<String, Object> job = repo.getJob(jobId);
    if (job == null) return List.of();
    Map<String, Object> jobContent = asObj(job.get("content"));
    List<Object> required = asList(jobContent == null ? null : jobContent.get("preferredSkills"));
    if (required == null) return List.of();

    Map<String, Object> ta = repo.getTaUser(asString(app.get("userId"), ""));
    if (ta == null) return List.of();
    Map<String, Object> taSkills = asObj(ta.get("skills"));
    List<String> skillNames = new ArrayList<>();
    if (taSkills != null) {
      addSkillNames(skillNames, taSkills.get("programming"));
      addSkillNames(skillNames, taSkills.get("teaching"));
      addSkillNames(skillNames, taSkills.get("communication"));
    }

    List<String> missing = new ArrayList<>();
    for (Object reqObj : required) {
      String req = String.valueOf(reqObj);
      boolean ok = skillNames.stream().anyMatch(s -> s.equalsIgnoreCase(req));
      if (!ok) missing.add(req);
    }
    return missing;
  }

  @SuppressWarnings("unchecked")
  private void addSkillNames(List<String> out, Object arrObj) {
    // arrObj is an array of { skillId, name, proficiency }
    List<Object> arr = asList(arrObj);
    if (arr == null) return;
    for (Object skillObj : arr) {
      Map<String, Object> skill = asObj(skillObj);
      if (skill == null) continue;
      String name = asString(skill.get("name"), "");
      if (!name.isBlank()) out.add(name);
    }
  }

  private double computeWorkloadHours(String taUserId) {
    double sum = 0.0;
    for (String appId : repo.getApplicationIdsForMo(currentMoUserId)) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      String appTa = asString(app.get("userId"), "");
      if (!taUserId.equals(appTa)) continue;
      Map<String, Object> st = asObj(app.get("status"));
      String cur = st == null ? "" : asString(st.get("current"), "");
      if (!("pending".equals(cur) || "under_review".equals(cur) || "accepted".equals(cur))) continue;
      String jobId = asString(app.get("jobId"), "");
      Map<String, Object> job = repo.getJob(jobId);
      Map<String, Object> employment = asObj(job == null ? null : job.get("employment"));
      if (employment == null) continue;
      Object wh = employment.get("weeklyHours");
      sum += asDouble(wh, 0.0);
    }
    return sum;
  }

  private int computeWorkloadCourses(String taUserId) {
    int count = 0;
    for (String appId : repo.getApplicationIdsForMo(currentMoUserId)) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      String appTa = asString(app.get("userId"), "");
      if (!taUserId.equals(appTa)) continue;
      Map<String, Object> st = asObj(app.get("status"));
      String cur = st == null ? "" : asString(st.get("current"), "");
      if (!("pending".equals(cur) || "under_review".equals(cur) || "accepted".equals(cur))) continue;
      count++;
    }
    return count;
  }

  private static RoundedPanel statCard(JLabel valueLabel, String label) {
    RoundedPanel p = new RoundedPanel(10);
    p.setBorderColor(Theme.CARD_BORDER);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

    JLabel l = new JLabel(label);
    l.setFont(Theme.FONT_PLAIN_14);
    l.setForeground(Theme.MUTED);

    valueLabel.setFont(Theme.FONT_BOLD_22);
    p.add(valueLabel);
    p.add(Box.createVerticalStrut(4));
    p.add(l);
    return p;
  }

  private JButton styledBlackButton(String text) {
    JButton btn = new JButton(text);
    btn.setBackground(Theme.PRIMARY);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setPreferredSize(new Dimension(140, 44));
    return btn;
  }

  private static Map<String, Object> asObj(Object o) {
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    return null;
  }

  private static String asString(Object o, String dflt) {
    return o == null ? dflt : String.valueOf(o);
  }

  private static String asString(Object o) {
    return o == null ? "" : String.valueOf(o);
  }

  private static double asDouble(Object o, double dflt) {
    if (o instanceof Number n) return n.doubleValue();
    if (o == null) return dflt;
    try {
      return Double.parseDouble(String.valueOf(o));
    } catch (Exception e) {
      return dflt;
    }
  }

  private static List<Object> asList(Object o) {
    if (o instanceof List<?> l) return (List<Object>) l;
    return null;
  }
}

