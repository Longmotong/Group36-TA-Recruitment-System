package com.ebuko.moapp.ui;

import com.ebuko.moapp.data.DataRepository;
import com.ebuko.moapp.data.StatusConfig;
import com.ebuko.moapp.ui.theme.LinkLabel;
import com.ebuko.moapp.ui.theme.RoundedPanel;
import com.ebuko.moapp.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DashboardPanelV2 extends JPanel {
  private final DataRepository repo;
  private final StatusConfig statusConfig;

  private String currentMoUserId;

  private final JLabel activeCoursesValue = new JLabel("-");
  private final JLabel openJobsValue = new JLabel("-");
  private final JLabel pendingReviewsValue = new JLabel("-");

  public DashboardPanelV2(
      DataRepository repo,
      StatusConfig statusConfig,
      String currentMoUserId,
      Runnable onGoApplicationReview,
      Runnable onGoJobManagement
  ) {
    super(new BorderLayout());
    this.repo = repo;
    this.statusConfig = statusConfig;
    this.currentMoUserId = currentMoUserId;

    setBackground(Theme.APP_BG);

    JPanel header = new JPanel();
    header.setOpaque(false);
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

    JLabel title = new JLabel("MO Dashboard");
    title.setFont(Theme.FONT_BOLD_28);
    title.setForeground(Theme.TEXT);
    JLabel subtitle = new JLabel("Welcome back! Please select a module to continue.");
    subtitle.setFont(Theme.FONT_PLAIN_14);
    subtitle.setForeground(Theme.MUTED);

    header.add(title);
    header.add(Box.createVerticalStrut(6));
    header.add(subtitle);
    header.add(Box.createVerticalStrut(14));

    add(header, BorderLayout.NORTH);

    // Module cards
    JPanel moduleRow = new JPanel(new GridLayout(1, 2, 20, 0));
    moduleRow.setOpaque(false);
    moduleRow.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

    RoundedPanel jobCard = new RoundedPanel(14);
    jobCard.setLayout(new BorderLayout());
    jobCard.setBorderColor(Theme.CARD_BORDER);
    jobCard.setPreferredSize(new Dimension(500, 190));
    JLabel jobIcon = new JLabel("\u25A1"); // simple square icon
    jobIcon.setForeground(Theme.MUTED);
    jobIcon.setFont(Theme.FONT_BOLD_16);

    JPanel jobTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
    jobTop.setOpaque(false);
    jobTop.add(jobIcon);

    JLabel jobCardTitle = new JLabel("Job Management Module");
    jobCardTitle.setFont(Theme.FONT_BOLD_22);
    JPanel jobTopWrap = new JPanel();
    jobTopWrap.setLayout(new BoxLayout(jobTopWrap, BoxLayout.Y_AXIS));
    jobTopWrap.setOpaque(false);
    jobTopWrap.add(jobCardTitle);
    JLabel jobTopDesc = new JLabel("Manage course info, requirements, and job postings");
    jobTopDesc.setFont(Theme.FONT_PLAIN_14);
    jobTopDesc.setForeground(Theme.MUTED);
    jobTopWrap.add(jobTopDesc);

    jobTopWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
    jobTop.add(jobTopWrap);
    jobCard.add(jobTop, BorderLayout.NORTH);

    // bullets
    JPanel bullets = new JPanel();
    bullets.setOpaque(false);
    bullets.setLayout(new BoxLayout(bullets, BoxLayout.Y_AXIS));
    bullets.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
    bullets.add(bullet("\u2022 Manage course info, requirements, and job postings"));
    bullets.add(bullet("\u2022 Create and edit course information"));
    bullets.add(bullet("\u2022 Set TA requirements and qualifications"));
    bullets.add(bullet("\u2022 Post and manage job openings"));
    jobCard.add(bullets, BorderLayout.CENTER);

    JButton jobGoBtn = styledBlackButton("Go to Job Management");
    jobGoBtn.addActionListener(e -> onGoJobManagement.run());
    JPanel jobBtnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
    jobBtnWrap.setOpaque(false);
    jobBtnWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 16, 0));
    jobBtnWrap.add(jobGoBtn);
    jobCard.add(jobBtnWrap, BorderLayout.SOUTH);

    RoundedPanel appCard = new RoundedPanel(14);
    appCard.setLayout(new BorderLayout());
    appCard.setPreferredSize(new Dimension(500, 190));
    JPanel appTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
    appTop.setOpaque(false);

    JLabel appIcon = new JLabel("\u25A1");
    appIcon.setForeground(Theme.MUTED);
    appIcon.setFont(Theme.FONT_BOLD_16);
    appTop.add(appIcon);

    JPanel appTopWrap = new JPanel();
    appTopWrap.setLayout(new BoxLayout(appTopWrap, BoxLayout.Y_AXIS));
    appTopWrap.setOpaque(false);
    JLabel appCardTitle = new JLabel("Application Review Module");
    appCardTitle.setFont(Theme.FONT_BOLD_22);
    appTopWrap.add(appCardTitle);
    JLabel appTopDesc = new JLabel("View TA applications, review, and check allocation results");
    appTopDesc.setFont(Theme.FONT_PLAIN_14);
    appTopDesc.setForeground(Theme.MUTED);
    appTopWrap.add(appTopDesc);
    appTopWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
    appTop.add(appTopWrap);
    appCard.add(appTop, BorderLayout.NORTH);

    JPanel appBullets = new JPanel();
    appBullets.setOpaque(false);
    appBullets.setLayout(new BoxLayout(appBullets, BoxLayout.Y_AXIS));
    appBullets.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
    appBullets.add(bullet("\u2022 Browse and filter TA applications"));
    appBullets.add(bullet("\u2022 Review and approve/reject applications"));
    appBullets.add(bullet("\u2022 View TA allocation results"));
    appCard.add(appBullets, BorderLayout.CENTER);

    JButton appGoBtn = styledBlackButton("Go to Application Review");
    appGoBtn.addActionListener(e -> onGoApplicationReview.run());
    JPanel appBtnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
    appBtnWrap.setOpaque(false);
    appBtnWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 16, 0));
    appBtnWrap.add(appGoBtn);
    appCard.add(appBtnWrap, BorderLayout.SOUTH);

    moduleRow.add(jobCard);
    moduleRow.add(appCard);

    add(moduleRow, BorderLayout.CENTER);

    // Quick Overview section at bottom
    RoundedPanel quick = new RoundedPanel(14);
    quick.setBorderColor(Theme.CARD_BORDER);
    quick.setLayout(new BoxLayout(quick, BoxLayout.Y_AXIS));
    quick.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

    JLabel quickTitle = new JLabel("Quick Overview");
    quickTitle.setFont(Theme.FONT_BOLD_16);
    quickTitle.setForeground(Theme.TEXT);
    quick.add(quickTitle);
    quick.add(Box.createVerticalStrut(14));

    JPanel statRow = new JPanel(new GridLayout(1, 3, 14, 0));
    statRow.setOpaque(false);

    activeCoursesValue.setFont(Theme.FONT_BOLD_22);
    openJobsValue.setFont(Theme.FONT_BOLD_22);
    pendingReviewsValue.setFont(Theme.FONT_BOLD_22);

    statRow.add(statCard(activeCoursesValue, "Active Courses"));
    statRow.add(statCard(openJobsValue, "Open Job Postings"));
    statRow.add(statCard(pendingReviewsValue, "Pending Reviews"));

    quick.add(statRow);

    JPanel quickWrap = new JPanel(new BorderLayout());
    quickWrap.setOpaque(false);
    quickWrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
    quickWrap.add(quick, BorderLayout.CENTER);
    add(quickWrap, BorderLayout.SOUTH);

    refresh();
  }

  public void setCurrentMoUserId(String currentMoUserId) {
    this.currentMoUserId = currentMoUserId;
  }

  public void refresh() {
    // Active Courses = unique courseCode among managed jobs
    List<String> jobIds = repo.getManagedJobIdsForMo(currentMoUserId);
    Set<String> courseCodes = new HashSet<>();
    int openJobs = 0;
    for (String jobId : jobIds) {
      Map<String, Object> job = repo.getJob(jobId);
      if (job == null) continue;
      Map<String, Object> course = asObj(job.get("course"));
      String cc = course == null ? "" : asString(course.get("courseCode"), "");
      if (!cc.isBlank()) courseCodes.add(cc);

      Map<String, Object> lifecycle = asObj(job.get("lifecycle"));
      String life = lifecycle == null ? "" : asString(lifecycle.get("status"), "");
      if ("open".equals(life)) openJobs++;
    }
    activeCoursesValue.setText(String.valueOf(courseCodes.size()));
    openJobsValue.setText(String.valueOf(openJobs));

    int pending = 0;
    for (String appId : repo.getApplicationIdsForMo(currentMoUserId)) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      Map<String, Object> status = asObj(app.get("status"));
      String st = status == null ? "" : asString(status.get("current"), "");
      if ("pending".equals(st) || "under_review".equals(st)) pending++;
    }
    pendingReviewsValue.setText(String.valueOf(pending));
  }

  private static JPanel statCard(JLabel valueLabel, String label) {
    RoundedPanel p = new RoundedPanel(10);
    p.setBorderColor(Theme.CARD_BORDER);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel l = new JLabel(label);
    l.setFont(Theme.FONT_PLAIN_14);
    l.setForeground(Theme.MUTED);
    l.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.add(valueLabel);
    p.add(Box.createVerticalStrut(6));
    p.add(l);
    return p;
  }

  private static JLabel bullet(String text) {
    JLabel l = new JLabel(text);
    l.setFont(Theme.FONT_PLAIN_14);
    l.setForeground(Theme.TEXT);
    return l;
  }

  private static JButton styledBlackButton(String text) {
    JButton btn = new JButton(text);
    btn.setBackground(Theme.PRIMARY);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setPreferredSize(new Dimension(420, 44));
    btn.setFont(Theme.FONT_PLAIN_14.deriveFont(Font.BOLD));
    return btn;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asObj(Object o) {
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    return null;
  }

  private static String asString(Object o, String dflt) {
    if (o == null) return dflt;
    return String.valueOf(o);
  }
}

