package com.ebuko.moapp.ui;

import com.ebuko.moapp.data.DataRepository;
import com.ebuko.moapp.data.StatusConfig;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationReviewPanel extends JPanel {
  private final DataRepository repo;
  private final StatusConfig statusConfig;

  private String currentMoUserId;
  private String jobFilterId = null;

  private Runnable onReviewSaved = null;

  private final JComboBox<JobFilterItem> jobCombo;
  private final JTextField searchField;
  private final JComboBox<StatusFilterItem> statusFilterCombo;

  private final JLabel statsTotalValue = new JLabel("-");
  private final JLabel statsPendingValue = new JLabel("-");
  private final JLabel statsApprovedValue = new JLabel("-");
  private final JLabel statsRejectedValue = new JLabel("-");

  private final DefaultListModel<ApplicationRow> listModel = new DefaultListModel<>();
  private final JList<ApplicationRow> appList = new JList<>(listModel);

  private final JPanel detailPanel = new JPanel(new BorderLayout());

  private final JLabel headline = new JLabel("Select an application to review");

  private final JTextArea applicantNotes = new JTextArea();

  private final JTextArea reviewerNotes = new JTextArea();
  private final JComboBox<String> decisionCombo;
  private final JTextArea decisionReason = new JTextArea();
  private final JTextArea statusMessage = new JTextArea();
  private final JTextArea nextSteps = new JTextArea();

  private final JButton saveBtn = new JButton("Save Review");

  private ApplicationRow selectedRow = null;

  public ApplicationReviewPanel(DataRepository repo, StatusConfig statusConfig, String currentMoUserId) {
    super(new BorderLayout());
    this.repo = repo;
    this.statusConfig = statusConfig;
    this.currentMoUserId = currentMoUserId;

    // Top filters
    JPanel topWrapper = new JPanel();
    topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
    topWrapper.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));

    // Stats bar (top)
    JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
    statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    statsPanel.setOpaque(false);

    statsPanel.add(makeStatCard("Total", statsTotalValue));
    statsPanel.add(makeStatCard("Pending", statsPendingValue));
    statsPanel.add(makeStatCard("Approved", statsApprovedValue));
    statsPanel.add(makeStatCard("Rejected", statsRejectedValue));

    JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

    jobCombo = new JComboBox<>();
    jobCombo.addActionListener(e -> {
      JobFilterItem item = (JobFilterItem) jobCombo.getSelectedItem();
      if (item == null) return;
      jobFilterId = item.jobId;
      refresh();
    });

    searchField = new JTextField();
    searchField.setPreferredSize(new Dimension(220, 28));
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        refresh();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        refresh();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        refresh();
      }
    });

    statusFilterCombo = new JComboBox<>(
        new StatusFilterItem[]{
            new StatusFilterItem(null, "All"),
            new StatusFilterItem("under_review", "Under Review"),
            new StatusFilterItem("accepted", "Approved"),
            new StatusFilterItem("rejected", "Rejected")
        }
    );
    statusFilterCombo.addActionListener(e -> refresh());

    filterPanel.add(new JLabel("Search (name/id):"));
    filterPanel.add(searchField);
    filterPanel.add(new JLabel("Status:"));
    filterPanel.add(statusFilterCombo);
    filterPanel.add(new JLabel("Job:"));
    filterPanel.add(jobCombo);

    // Split: list + detail
    JPanel split = new JPanel(new BorderLayout());
    split.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

    appList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    appList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        ApplicationRow row = appList.getSelectedValue();
        selectedRow = row;
        renderDetail(row);
      }
    });
    appList.setCellRenderer(new ApplicationCellRenderer());

    JScrollPane listScroller = new JScrollPane(appList);
    listScroller.setPreferredSize(new Dimension(420, 520));

    // Detail panel contents
    JPanel detailInner = new JPanel();
    detailInner.setLayout(new BoxLayout(detailInner, BoxLayout.Y_AXIS));
    detailInner.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220)),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)
    ));

    headline.setFont(headline.getFont().deriveFont(Font.BOLD, 16f));
    detailInner.add(headline);
    detailInner.add(Box.createVerticalStrut(10));

    applicantNotes.setLineWrap(true);
    applicantNotes.setWrapStyleWord(true);
    applicantNotes.setEditable(false);
    applicantNotes.setBackground(new Color(250, 250, 250));
    detailInner.add(wrapWithLabel("Applicant + Job Summary", new JScrollPane(applicantNotes)));
    detailInner.add(Box.createVerticalStrut(10));

    reviewerNotes.setLineWrap(true);
    reviewerNotes.setWrapStyleWord(true);
    reviewerNotes.setRows(3);

    decisionReason.setLineWrap(true);
    decisionReason.setWrapStyleWord(true);
    decisionReason.setRows(2);

    statusMessage.setLineWrap(true);
    statusMessage.setWrapStyleWord(true);
    statusMessage.setRows(2);

    nextSteps.setLineWrap(true);
    nextSteps.setWrapStyleWord(true);
    nextSteps.setRows(2);

    decisionCombo = new JComboBox<>(new String[]{"", "accepted", "rejected"});

    saveBtn.addActionListener(e -> {
      if (selectedRow == null) return;
      String decision = (String) decisionCombo.getSelectedItem();
      repo.updateApplicationReview(
          selectedRow.applicationId,
          currentMoUserId,
          reviewerNotes.getText(),
          decision,
          decisionReason.getText(),
          statusMessage.getText(),
          nextSteps.getText()
      );
      refresh();
      if (onReviewSaved != null) onReviewSaved.run();
    });

    detailInner.add(Box.createVerticalStrut(12));
    detailInner.add(wrapWithLabel("Reviewer Notes", new JScrollPane(reviewerNotes)));
    detailInner.add(Box.createVerticalStrut(8));

    JPanel decisionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    decisionRow.add(new JLabel("Decision:"));
    decisionRow.add(decisionCombo);
    detailInner.add(decisionRow);

    detailInner.add(Box.createVerticalStrut(8));
    detailInner.add(wrapWithLabel("Decision Reason", new JScrollPane(decisionReason)));
    detailInner.add(Box.createVerticalStrut(8));
    detailInner.add(wrapWithLabel("Status Message", new JScrollPane(statusMessage)));
    detailInner.add(Box.createVerticalStrut(8));
    detailInner.add(wrapWithLabel("Next Steps", new JScrollPane(nextSteps)));

    detailInner.add(Box.createVerticalStrut(10));
    detailInner.add(saveBtn);

    detailPanel.add(detailInner, BorderLayout.CENTER);

    split.add(listScroller, BorderLayout.WEST);
    split.add(detailPanel, BorderLayout.CENTER);

    add(filterPanel, BorderLayout.NORTH);
    add(split, BorderLayout.CENTER);
  }

  public void setOnReviewSaved(Runnable onReviewSaved) {
    this.onReviewSaved = onReviewSaved;
  }

  public void setCurrentMoUserId(String currentMoUserId) {
    this.currentMoUserId = currentMoUserId;
  }

  public void setJobFilter(String jobFilterId) {
    this.jobFilterId = jobFilterId;
  }

  public void refresh() {
    // Refresh job combo options based on the current MO's managed jobs.
    List<String> jobIds = repo.getManagedJobIdsForMo(currentMoUserId);
    jobCombo.removeAllItems();
    jobCombo.addItem(new JobFilterItem(null, "All managed jobs"));
    for (String jobId : jobIds) {
      Map<String, Object> job = repo.getJob(jobId);
      String title = job == null ? jobId : asString(job.get("title"), jobId);
      jobCombo.addItem(new JobFilterItem(jobId, title));
    }
    // Apply external filter (from dashboard click).
    for (int i = 0; i < jobCombo.getItemCount(); i++) {
      JobFilterItem item = jobCombo.getItemAt(i);
      if ((jobFilterId == null && item.jobId == null) || (jobFilterId != null && jobFilterId.equals(item.jobId))) {
        jobCombo.setSelectedIndex(i);
        break;
      }
    }

    StatusFilterItem statusFilter = (StatusFilterItem) statusFilterCombo.getSelectedItem();
    String statusFilterValue = statusFilter == null ? null : statusFilter.statusValue;
    String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

    // Stats (based on current job filter, before search/status filters).
    int total = 0;
    int pending = 0; // includes "pending" + "under_review"
    int approved = 0;
    int rejected = 0;
    List<String> allAppIds = repo.getApplicationIdsForMo(currentMoUserId);
    for (String appId : allAppIds) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      String jobId = asString(app.get("jobId"), "");
      if (jobFilterId != null && !jobFilterId.equals(jobId)) continue;

      Map<String, Object> status = asObj(app.get("status"));
      String st = status == null ? "" : asString(status.get("current"), "");
      total++;
      if ("accepted".equals(st)) approved++;
      else if ("rejected".equals(st)) rejected++;
      else pending++;
    }
    statsTotalValue.setText(String.valueOf(total));
    statsPendingValue.setText(String.valueOf(pending));
    statsApprovedValue.setText(String.valueOf(approved));
    statsRejectedValue.setText(String.valueOf(rejected));

    // Build application list.
    listModel.clear();
    for (String appId : allAppIds) {
      Map<String, Object> app = repo.getApplication(appId);
      if (app == null) continue;
      String jobId = asString(app.get("jobId"), "");
      if (jobFilterId != null && !jobFilterId.equals(jobId)) continue;

      Map<String, Object> status = asObj(app.get("status"));
      String st = status == null ? "" : asString(status.get("current"), "");

      // Status filter
      if (statusFilterValue != null) {
        if ("under_review".equals(statusFilterValue)) {
          if (!("pending".equals(st) || "under_review".equals(st))) continue;
        } else if (!statusFilterValue.equals(st)) {
          continue;
        }
      }

      Map<String, Object> applicantSnapshot = asObj(app.get("applicantSnapshot"));
      String applicantName =
          applicantSnapshot == null ? "" : asString(applicantSnapshot.get("fullName"), "");
      String studentId =
          applicantSnapshot == null ? "" : asString(applicantSnapshot.get("studentId"), "");

      // Search filter (TA name / studentId)
      if (!search.isEmpty()) {
        boolean hit = applicantName.toLowerCase().contains(search) || studentId.toLowerCase().contains(search);
        if (!hit) continue;
      }

      Map<String, Object> jobSnapshot = asObj(app.get("jobSnapshot"));
      String jobTitle = jobSnapshot == null ? jobId : asString(jobSnapshot.get("title"), jobId);
      String lastUpdated = status == null ? "" : asString(status.get("lastUpdated"), "");

      double matchScore = computeMatchScore(app, jobId);
      String missingSkillsText = computeMissingSkillsText(app, jobId);

      listModel.addElement(
          new ApplicationRow(appId, applicantName, studentId, jobTitle, st, lastUpdated, matchScore, missingSkillsText)
      );
    }

    // Sort list by lastUpdated desc (if empty, keep as end).
    List<ApplicationRow> rows = new ArrayList<>();
    for (int i = 0; i < listModel.size(); i++) rows.add(listModel.get(i));
    rows.sort((a, b) -> {
      // Lexicographical works for ISO timestamps.
      String ta = a.lastUpdated == null ? "" : a.lastUpdated;
      String tb = b.lastUpdated == null ? "" : b.lastUpdated;
      return tb.compareTo(ta);
    });
    listModel.clear();
    for (ApplicationRow r : rows) listModel.addElement(r);

    if (!rows.isEmpty()) {
      appList.setSelectedIndex(0);
      renderDetail(appList.getSelectedValue());
    } else {
      selectedRow = null;
      renderDetail(null);
    }
  }

  private void renderDetail(ApplicationRow row) {
    if (row == null) {
      headline.setText("Select an application to review");
      applicantNotes.setText("");
      reviewerNotes.setText("");
      decisionCombo.setSelectedIndex(0);
      decisionReason.setText("");
      statusMessage.setText("");
      nextSteps.setText("");
      saveBtn.setEnabled(false);
      return;
    }

    saveBtn.setEnabled(true);
    Map<String, Object> app = repo.getApplication(row.applicationId);
    if (app == null) return;

    String taUserId = asString(app.get("userId"), "");
    Map<String, Object> ta = repo.getTaUser(taUserId);

    Map<String, Object> appApplicantSnapshot = asObj(app.get("applicantSnapshot"));
    Map<String, Object> taProfile = ta == null ? null : asObj(ta.get("profile"));
    Map<String, Object> taAccount = ta == null ? null : asObj(ta.get("account"));
    Map<String, Object> taAcademic = ta == null ? null : asObj(ta.get("academic"));

    String applicantFullName =
        taProfile != null ? asString(taProfile.get("fullName"), "") :
            (appApplicantSnapshot == null ? "" : asString(appApplicantSnapshot.get("fullName"), ""));
    String studentId =
        taProfile != null ? asString(taProfile.get("studentId"), "") :
            (appApplicantSnapshot == null ? "" : asString(appApplicantSnapshot.get("studentId"), ""));
    String email =
        taAccount != null ? asString(taAccount.get("email"), "") :
            (appApplicantSnapshot == null ? "" : asString(appApplicantSnapshot.get("email"), ""));
    String department =
        taProfile != null ? asString(taProfile.get("department"), "") :
            (appApplicantSnapshot == null ? "" : asString(appApplicantSnapshot.get("department"), ""));
    double gpa =
        taAcademic != null ? asDouble(taAcademic.get("gpa"), 0.0) :
            (appApplicantSnapshot == null ? 0.0 : asDouble(appApplicantSnapshot.get("gpa"), 0.0));

    // Skills from TA
    String skillsText = "";
    if (ta != null) {
      Map<String, Object> taSkills = asObj(ta.get("skills"));
      List<Object> programming = taSkills == null ? null : asList(taSkills.get("programming"));
      StringBuilder sb = new StringBuilder();
      if (programming != null && !programming.isEmpty()) {
        sb.append("Programming skills: ");
        for (int i = 0; i < programming.size(); i++) {
          Object sObj = programming.get(i);
          Map<String, Object> s = asObj(sObj);
          if (i > 0) sb.append(", ");
          sb.append(asString(s == null ? null : s.get("name"), ""));
          String prof = asString(s == null ? null : s.get("proficiency"), "");
          if (!prof.isBlank()) sb.append(" (").append(prof).append(")");
        }
      }
      List<Object> teaching = taSkills == null ? null : asList(taSkills.get("teaching"));
      if (teaching != null && !teaching.isEmpty()) {
        if (!sb.toString().isEmpty()) sb.append("\n");
        sb.append("Teaching skills: ");
        for (int i = 0; i < teaching.size(); i++) {
          Object sObj = teaching.get(i);
          Map<String, Object> s = asObj(sObj);
          if (i > 0) sb.append(", ");
          sb.append(asString(s == null ? null : s.get("name"), ""));
          String prof = asString(s == null ? null : s.get("proficiency"), "");
          if (!prof.isBlank()) sb.append(" (").append(prof).append(")");
        }
      }
      skillsText = sb.toString();
    }

    Map<String, Object> jobSnapshot = asObj(app.get("jobSnapshot"));
    String jobTitle = jobSnapshot == null ? row.jobTitle : asString(jobSnapshot.get("title"), row.jobTitle);
    String courseCode = jobSnapshot == null ? "" : asString(jobSnapshot.get("courseCode"), "");
    String courseName = jobSnapshot == null ? "" : asString(jobSnapshot.get("courseName"), "");
    String deadline = jobSnapshot == null ? "" : asString(jobSnapshot.get("deadline"), "");

    Map<String, Object> applicationForm = asObj(app.get("applicationForm"));
    String motivation = applicationForm == null ? "" : asString(applicationForm.get("motivationCoverLetter"), "");
    String availability = applicationForm == null ? "" : asString(applicationForm.get("availability"), "");
    List<Object> relSkills = applicationForm == null ? null : asList(applicationForm.get("relevantSkills"));
    List<String> relSkillsList = new ArrayList<>();
    if (relSkills != null) {
      for (Object s : relSkills) relSkillsList.add(asString(s, ""));
    }

    Map<String, Object> status = asObj(app.get("status"));
    String st = status == null ? "" : asString(status.get("current"), "");
    String label = statusConfig.labelFor(st);

    headline.setText("Application: " + row.applicationId + "   |   Status: " + label);

    applicantNotes.setText(
        "Applicant: " + applicantFullName + " (" + studentId + ")\n" +
            "Email: " + email + "\n" +
            "Department: " + department + "   GPA: " + (gpa == 0.0 ? "" : gpa) + "\n" +
            (skillsText.isBlank() ? "" : (skillsText + "\n")) +
            "\n" +
            "Match Score: " + Math.round(row.matchScore) + "/100\n" +
            (row.missingSkillsText == null || row.missingSkillsText.isBlank() ? "" : (row.missingSkillsText + "\n\n")) +
            "Job: " + jobTitle + "\n" +
            "Course: " + (courseCode.isBlank() ? "" : courseCode + " ") + courseName + "\n" +
            "Deadline: " + deadline + "\n" +
            "\n" +
            "Relevant Skills: " + String.join(", ", relSkillsList) + "\n" +
            "Availability: " + availability + "\n" +
            "Motivation:\n" + (motivation == null ? "" : motivation)
    );

    Map<String, Object> review = asObj(app.get("review"));
    String reviewer = review == null ? "" : asString(review.get("reviewerNotes"), "");
    reviewerNotes.setText(reviewer);

    String decision = status == null ? "" : asString(status.get("current"), "");
    if ("accepted".equals(decision)) decisionCombo.setSelectedItem("accepted");
    else if ("rejected".equals(decision)) decisionCombo.setSelectedItem("rejected");
    else decisionCombo.setSelectedIndex(0);

    decisionReason.setText(review == null ? "" : asString(review.get("decisionReason"), ""));
    statusMessage.setText(review == null ? "" : asString(review.get("statusMessage"), ""));
    nextSteps.setText(review == null ? "" : asString(review.get("nextSteps"), ""));
  }

  @SuppressWarnings("unchecked")
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
    } catch (Exception ignored) {
      return dflt;
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Object> asList(Object o) {
    if (o instanceof List<?> l) return (List<Object>) l;
    return null;
  }

  private static JPanel wrapWithLabel(String label, JComponent content) {
    JPanel panel = new JPanel(new BorderLayout(0, 6));
    panel.setOpaque(false);
    panel.add(new JLabel(label), BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  private static class StatusFilterItem {
    final String statusValue; // null = All
    final String label;

    StatusFilterItem(String statusValue, String label) {
      this.statusValue = statusValue;
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private static JPanel makeStatCard(String label, JLabel valueLabel) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setOpaque(false);

    JLabel l1 = new JLabel(label);
    JLabel l2 = valueLabel;
    l1.setFont(l1.getFont().deriveFont(Font.PLAIN, 12f));
    l2.setFont(l2.getFont().deriveFont(Font.BOLD, 18f));
    l2.setForeground(new Color(30, 30, 30));

    card.add(l1);
    card.add(Box.createVerticalStrut(4));
    card.add(l2);
    return card;
  }

  // A lightweight "match score" based on job preferred skills vs TA skills.
  // This is computed from existing JSON fields; no extra data is fabricated.
  private double computeMatchScore(Map<String, Object> app, String jobId) {
    Map<String, Object> job = repo.getJob(jobId);
    if (job == null) return 0.0;
    Map<String, Object> jobContent = asObj(job.get("content"));
    if (jobContent == null) return 0.0;
    List<Object> required = asList(jobContent.get("preferredSkills"));
    if (required == null || required.isEmpty()) return 0.0;

    String taUserId = asString(app.get("userId"), "");
    Map<String, Object> ta = repo.getTaUser(taUserId);
    if (ta == null) return 0.0;
    Map<String, Object> taSkills = asObj(ta.get("skills"));
    List<String> skillNames = new ArrayList<>();
    if (taSkills != null) {
      addSkillNamesFromArray(skillNames, taSkills.get("programming"));
      addSkillNamesFromArray(skillNames, taSkills.get("teaching"));
      addSkillNamesFromArray(skillNames, taSkills.get("communication"));
    }

    int matched = 0;
    for (Object reqObj : required) {
      String req = asString(reqObj);
      if (req.isBlank()) continue;
      if (containsSkill(skillNames, req)) matched++;
    }
    return matched * 100.0 / required.size();
  }

  private String computeMissingSkillsText(Map<String, Object> app, String jobId) {
    Map<String, Object> job = repo.getJob(jobId);
    if (job == null) return "";
    Map<String, Object> jobContent = asObj(job.get("content"));
    if (jobContent == null) return "";
    List<Object> required = asList(jobContent.get("preferredSkills"));
    if (required == null || required.isEmpty()) return "";

    String taUserId = asString(app.get("userId"), "");
    Map<String, Object> ta = repo.getTaUser(taUserId);
    if (ta == null) return "";
    Map<String, Object> taSkills = asObj(ta.get("skills"));
    List<String> skillNames = new ArrayList<>();
    if (taSkills != null) {
      addSkillNamesFromArray(skillNames, taSkills.get("programming"));
      addSkillNamesFromArray(skillNames, taSkills.get("teaching"));
      addSkillNamesFromArray(skillNames, taSkills.get("communication"));
    }

    List<String> missing = new ArrayList<>();
    for (Object reqObj : required) {
      String req = asString(reqObj);
      if (req.isBlank()) continue;
      if (!containsSkill(skillNames, req)) missing.add(req);
    }
    if (missing.isEmpty()) return "All skills met";
    int limit = Math.min(3, missing.size());
    return "Missing: " + String.join(", ", missing.subList(0, limit));
  }

  @SuppressWarnings("unchecked")
  private void addSkillNamesFromArray(List<String> out, Object arrObj) {
    List<Object> arr = asList(arrObj);
    if (arr == null) return;
    for (Object skillObj : arr) {
      Map<String, Object> skill = asObj(skillObj);
      if (skill == null) continue;
      String name = asString(skill.get("name"));
      if (!name.isBlank()) out.add(name);
    }
  }

  private boolean containsSkill(List<String> skillNames, String requiredSkill) {
    String reqLower = requiredSkill.toLowerCase();
    for (String s : skillNames) {
      String sl = s.toLowerCase();
      if (sl.contains(reqLower) || reqLower.contains(sl)) return true;
    }
    return false;
  }

  private static class JobFilterItem {
    final String jobId; // null = All
    final String title;

    JobFilterItem(String jobId, String title) {
      this.jobId = jobId;
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  private class ApplicationRow {
    final String applicationId;
    final String applicantName;
    final String studentId;
    final String jobTitle;
    final String statusCurrent;
    final String lastUpdated;
    final double matchScore;
    final String missingSkillsText;

    ApplicationRow(
        String applicationId,
        String applicantName,
        String studentId,
        String jobTitle,
        String statusCurrent,
        String lastUpdated,
        double matchScore,
        String missingSkillsText
    ) {
      this.applicationId = applicationId;
      this.applicantName = applicantName;
      this.studentId = studentId;
      this.jobTitle = jobTitle;
      this.statusCurrent = statusCurrent;
      this.lastUpdated = lastUpdated;
      this.matchScore = matchScore;
      this.missingSkillsText = missingSkillsText;
    }

    @Override
    public String toString() {
      String st = statusConfig.labelFor(statusCurrent);
      int score = (int) Math.round(matchScore);
      return applicantName + " (" + studentId + ")"
          + "  |  " + st
          + "  |  " + jobTitle
          + "  |  Match " + score;
    }
  }

  private class ApplicationCellRenderer extends JLabel implements ListCellRenderer<ApplicationRow> {
    private final Map<String, Color> colorMap = Map.of(
        "yellow", new Color(255, 244, 200),
        "blue", new Color(210, 235, 255),
        "green", new Color(210, 255, 210),
        "red", new Color(255, 210, 210)
    );

    ApplicationCellRenderer() {
      setOpaque(true);
      setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ApplicationRow> list, ApplicationRow value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value == null) return this;
      setText(value.toString());

      StatusConfig.Entry entry = statusConfig.get(value.statusCurrent);
      Color bg = entry == null ? Color.WHITE : colorMap.getOrDefault(entry.color, Color.WHITE);

      if (isSelected) {
        setBackground(new Color(180, 200, 255));
        setForeground(Color.BLACK);
      } else {
        setBackground(bg);
        setForeground(Color.BLACK);
      }
      return this;
    }
  }
}

