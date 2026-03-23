package com.ebuko.moapp.ui;

import com.ebuko.moapp.data.DataRepository;
import com.ebuko.moapp.data.StatusConfig;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
  @FunctionalInterface
  public interface JobSelectionListener {
    void onJobSelected(String jobIdOrNull);
  }

  private final DataRepository repo;
  private final StatusConfig statusConfig;
  private final JobSelectionListener jobSelectionListener;

  private String currentMoUserId;

  private final JLabel activeCoursesValue = new JLabel("-");
  private final JLabel managedJobsValue = new JLabel("-");
  private final JLabel pendingReviewsValue = new JLabel("-");

  private final DefaultListModel<JobRow> jobListModel = new DefaultListModel<>();
  private final JList<JobRow> jobList = new JList<>(jobListModel);

  public DashboardPanel(
      DataRepository repo,
      StatusConfig statusConfig,
      String currentMoUserId,
      JobSelectionListener jobSelectionListener
  ) {
    this.repo = repo;
    this.statusConfig = statusConfig;
    this.currentMoUserId = currentMoUserId;
    this.jobSelectionListener = jobSelectionListener;

    setLayout(new BorderLayout());
    JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 16, 16));
    summaryPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));

    activeCoursesValue.setFont(activeCoursesValue.getFont().deriveFont(Font.BOLD, 28f));
    managedJobsValue.setFont(managedJobsValue.getFont().deriveFont(Font.BOLD, 28f));
    pendingReviewsValue.setFont(pendingReviewsValue.getFont().deriveFont(Font.BOLD, 28f));

    JPanel activeCoursesCard = new JPanel(new BorderLayout());
    activeCoursesCard.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    activeCoursesCard.add(new JLabel("Active Courses", SwingConstants.CENTER), BorderLayout.NORTH);
    activeCoursesCard.add(activeCoursesValue, BorderLayout.CENTER);

    JPanel openJobsCard = new JPanel(new BorderLayout());
    openJobsCard.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    openJobsCard.add(new JLabel("Open Job Postings", SwingConstants.CENTER), BorderLayout.NORTH);
    openJobsCard.add(managedJobsValue, BorderLayout.CENTER);

    JPanel pendingCard = new JPanel(new BorderLayout());
    pendingCard.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    pendingCard.add(new JLabel("Pending Reviews", SwingConstants.CENTER), BorderLayout.NORTH);
    pendingCard.add(pendingReviewsValue, BorderLayout.CENTER);

    summaryPanel.add(activeCoursesCard);
    summaryPanel.add(openJobsCard);
    summaryPanel.add(pendingCard);

    JPanel jobsPanel = new JPanel(new BorderLayout());
    jobsPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
    jobsPanel.add(new JLabel("Open Job Postings (click to filter review list)"), BorderLayout.NORTH);
    jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    jobList.addListSelectionListener(e -> {
      if (e.getValueIsAdjusting()) return;
      JobRow selected = jobList.getSelectedValue();
      if (selected == null) return;
      jobSelectionListener.onJobSelected(selected.jobId);
      // Keep the UI intuitive: move to review module is controlled by left nav buttons.
    });

    JScrollPane scroller = new JScrollPane(jobList);
    jobsPanel.add(scroller, BorderLayout.CENTER);

    JButton clearFilter = new JButton("Show all jobs");
    clearFilter.addActionListener(e -> {
      jobList.clearSelection();
      jobSelectionListener.onJobSelected(null);
    });
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
    bottom.add(clearFilter);
    jobsPanel.add(bottom, BorderLayout.SOUTH);

    add(summaryPanel, BorderLayout.NORTH);
    add(jobsPanel, BorderLayout.CENTER);
  }

  public void setCurrentMoUserId(String currentMoUserId) {
    this.currentMoUserId = currentMoUserId;
  }

  public void refresh() {
    jobListModel.clear();

    List<String> jobIds = repo.getManagedJobIdsForMo(currentMoUserId);
    managedJobsValue.setText(String.valueOf(jobIds.size())); // Open Job Postings

    // Active Courses: unique courseCode among managed jobs.
    java.util.Set<String> courseCodes = new java.util.HashSet<>();
    for (String jobId : jobIds) {
      Map<String, Object> job = repo.getJob(jobId);
      if (job == null) continue;
      Map<String, Object> course = asObj(job.get("course"));
      String cc = course == null ? "" : asString(course.get("courseCode"), "");
      if (!cc.isBlank()) courseCodes.add(cc);
    }
    activeCoursesValue.setText(String.valueOf(courseCodes.size()));

    // Compute pending reviews dynamically from application statuses.
    int pending = 0;
    for (String appId : repo.getApplicationIdsForMo(currentMoUserId)) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      Map<String, Object> status = asObj(app.get("status"));
      String st = status == null ? "" : asString(status.get("current"));
      if ("pending".equals(st) || "under_review".equals(st)) pending++;
    }
    pendingReviewsValue.setText(String.valueOf(pending));

    for (String jobId : jobIds) {
      Map<String, Object> job = repo.getJob(jobId);
      if (job == null) continue;
      Map<String, Object> course = asObj(job.get("course"));
      Map<String, Object> dates = asObj(job.get("dates"));
      Map<String, Object> stats = asObj(job.get("stats"));
      JobRow row = new JobRow(jobId,
          asString(job.get("title"), jobId),
          course == null ? "" : asString(course.get("courseCode"), ""),
          course == null ? "" : asString(course.get("term"), ""),
          course == null ? "" : asString(course.get("year"), ""),
          dates == null ? "" : asString(dates.get("deadline"), ""),
          stats == null ? 0 : asInt(stats.get("pendingCount")),
          stats == null ? 0 : asInt(stats.get("acceptedCount")),
          stats == null ? 0 : asInt(stats.get("rejectedCount"))
      );
      jobListModel.addElement(row);
    }
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

  private static String asString(Object o) {
    return o == null ? "" : String.valueOf(o);
  }

  private static int asInt(Object o) {
    if (o instanceof Number n) return n.intValue();
    try {
      return o == null ? 0 : Integer.parseInt(String.valueOf(o));
    } catch (Exception ignored) {
      return 0;
    }
  }

  private static class JobRow {
    final String jobId;
    final String title;
    final String courseCode;
    final String term;
    final String year;
    final String deadline;
    final int pendingCount;
    final int acceptedCount;
    final int rejectedCount;

    JobRow(
        String jobId,
        String title,
        String courseCode,
        String term,
        String year,
        String deadline,
        int pendingCount,
        int acceptedCount,
        int rejectedCount
    ) {
      this.jobId = jobId;
      this.title = title;
      this.courseCode = courseCode;
      this.term = term;
      this.year = year;
      this.deadline = deadline;
      this.pendingCount = pendingCount;
      this.acceptedCount = acceptedCount;
      this.rejectedCount = rejectedCount;
    }

    @Override
    public String toString() {
      String course = courseCode.isBlank() ? "" : (courseCode + " ");
      String termYear = (term + " " + year).trim();
      return title + " [" + course + termYear + "]  deadline=" + deadline;
    }
  }
}

