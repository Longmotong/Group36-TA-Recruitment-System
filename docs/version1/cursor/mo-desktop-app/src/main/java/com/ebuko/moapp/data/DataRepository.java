package com.ebuko.moapp.data;

import com.ebuko.moapp.json.MiniJson;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataRepository {
  private final Path dataRoot;
  private final StatusConfig statusConfig;

  private final Map<String, Map<String, Object>> moUsers = new HashMap<>();
  private final Map<String, Map<String, Object>> taUsers = new HashMap<>();
  private final Map<String, Map<String, Object>> jobs = new HashMap<>();
  private final Map<String, Map<String, Object>> applications = new HashMap<>();
  private final Map<String, Path> applicationFiles = new HashMap<>();

  private final Map<String, List<String>> moToJobIds = new HashMap<>();
  private final Map<String, List<String>> moToApplicationIds = new HashMap<>();

  public DataRepository() {
    this.dataRoot = DataPaths.resolveDataRoot();
    try {
      this.statusConfig = StatusConfig.load(dataRoot);
      loadUsers();
      loadJobs();
      loadApplications();
      loadIndexes();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public StatusConfig getStatusConfig() {
    return statusConfig;
  }

  public Path getDataRoot() {
    return dataRoot;
  }

  public List<String> listMoUserIds() {
    return new ArrayList<>(moUsers.keySet());
  }

  public String moFullName(String moUserId) {
    Map<String, Object> node = moUsers.get(moUserId);
    if (node == null) return moUserId;
    Object profile = node.get("profile");
    if (!(profile instanceof Map<?, ?> p)) return moUserId;
    Object fullName = p.get("fullName");
    return fullName == null ? moUserId : String.valueOf(fullName);
  }

  public List<String> getManagedJobIdsForMo(String moUserId) {
    return moToJobIds.getOrDefault(moUserId, List.of());
  }

  public List<String> getApplicationIdsForMo(String moUserId) {
    return moToApplicationIds.getOrDefault(moUserId, List.of());
  }

  public Map<String, Object> getJob(String jobId) {
    return jobs.get(jobId);
  }

  public Map<String, Object> getApplication(String appId) {
    return applications.get(appId);
  }

  public Map<String, Object> getTaUser(String taUserId) {
    return taUsers.get(taUserId);
  }

  public void updateApplicationReview(
      String applicationId,
      String moUserId,
      String reviewerNotes,
      String decision,
      String decisionReason,
      String statusMessage,
      String nextSteps
  ) {
    Map<String, Object> app = applications.get(applicationId);
    if (app == null) return;

    Path f = applicationFiles.get(applicationId);
    if (f == null) return;

    String now = Instant.now().toString();
    Map<String, Object> status = getOrCreateObject(app, "status");
    Map<String, Object> review = getOrCreateObject(app, "review");
    List<Object> timeline = getOrCreateList(app, "timeline");

    String notes = reviewerNotes == null ? "" : reviewerNotes;
    String reason = decisionReason == null ? "" : decisionReason;
    String msg = statusMessage == null ? "" : statusMessage;
    String steps = nextSteps == null ? "" : nextSteps;

    if (decision != null && !decision.isBlank()) {
      status.put("current", decision);
      status.put("label", statusConfig.labelFor(decision));
      status.put("lastUpdated", now);
      status.put("updatedBy", moUserId);

      review.put("reviewerNotes", notes);
      review.put("decision", decision);
      review.put("decisionReason", reason);
      review.put("statusMessage", msg);
      review.put("nextSteps", steps);
      review.put("reviewedBy", moUserId);
      review.put("reviewedAt", now);

      Map<String, Object> step = new LinkedHashMap<>();
      step.put("timelineId", "tl_" + System.currentTimeMillis());
      step.put("stepKey", "reviewed");
      step.put("stepLabel", "Review Updated");
      step.put("status", "completed");
      step.put("timestamp", now);
      step.put("updatedBy", moUserId);
      step.put("note", "Decision set to " + decision);
      timeline.add(step);
    } else {
      // If no decision picked, still update reviewer notes.
      review.put("reviewerNotes", notes);
      status.put("lastUpdated", now);
      status.put("updatedBy", moUserId);
    }

    try {
      String out = MiniJson.stringify(app, true);
      Files.writeString(f, out, java.nio.charset.StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void loadUsers() throws IOException {
    Path moDir = dataRoot.resolve("users").resolve("mo");
    if (Files.isDirectory(moDir)) {
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(moDir, "*.json")) {
        for (Path p : ds) {
          String text = Files.readString(p, java.nio.charset.StandardCharsets.UTF_8);
          @SuppressWarnings("unchecked")
          Map<String, Object> node = (Map<String, Object>) MiniJson.parse(text);
          String userId = (String) node.get("userId");
          if (userId != null) moUsers.put(userId, node);
        }
      }
    }

    Path taDir = dataRoot.resolve("users").resolve("ta");
    if (Files.isDirectory(taDir)) {
      try (DirectoryStream<Path> ds = Files.newDirectoryStream(taDir, "*.json")) {
        for (Path p : ds) {
          String text = Files.readString(p, java.nio.charset.StandardCharsets.UTF_8);
          @SuppressWarnings("unchecked")
          Map<String, Object> node = (Map<String, Object>) MiniJson.parse(text);
          String userId = (String) node.get("userId");
          if (userId != null) taUsers.put(userId, node);
        }
      }
    }
  }

  private void loadJobs() throws IOException {
    Path jobsDir = dataRoot.resolve("jobs");
    if (!Files.isDirectory(jobsDir)) return;
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(jobsDir, "*.json")) {
      for (Path p : ds) {
        String text = Files.readString(p, java.nio.charset.StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> node = (Map<String, Object>) MiniJson.parse(text);
        String jobId = (String) node.get("jobId");
        if (jobId != null) jobs.put(jobId, node);
      }
    }
  }

  private void loadApplications() throws IOException {
    Path appsDir = dataRoot.resolve("applications");
    if (!Files.isDirectory(appsDir)) return;
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(appsDir, "app_*.json")) {
      for (Path p : ds) {
        String text = Files.readString(p, java.nio.charset.StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> node = (Map<String, Object>) MiniJson.parse(text);
        String appId = (String) node.get("applicationId");
        if (appId != null) {
          applications.put(appId, node);
          applicationFiles.put(appId, p);
        }
      }
    }
  }

  private void loadIndexes() throws IOException {
    String moJobsText = Files.readString(
        dataRoot.resolve("indexes").resolve("mo_jobs_index.json"),
        java.nio.charset.StandardCharsets.UTF_8
    );
    String moAppsText = Files.readString(
        dataRoot.resolve("indexes").resolve("mo_applications_index.json"),
        java.nio.charset.StandardCharsets.UTF_8
    );

    @SuppressWarnings("unchecked")
    Map<String, Object> moJobsRoot = (Map<String, Object>) MiniJson.parse(moJobsText);
    @SuppressWarnings("unchecked")
    Map<String, Object> moAppsRoot = (Map<String, Object>) MiniJson.parse(moAppsText);

    Object moJobsObj = moJobsRoot.get("moJobs");
    if (moJobsObj instanceof Map<?, ?> moJobsMap) {
      for (Map.Entry<?, ?> e : moJobsMap.entrySet()) {
        String moUserId = String.valueOf(e.getKey());
        List<String> ids = new ArrayList<>();
        Object arr = e.getValue();
        if (arr instanceof List<?> list) {
          for (Object id : list) ids.add(String.valueOf(id));
        }
        moToJobIds.put(moUserId, ids);
      }
    }

    Object moAppsObj = moAppsRoot.get("moApplications");
    if (moAppsObj instanceof Map<?, ?> moAppsMap) {
      for (Map.Entry<?, ?> e : moAppsMap.entrySet()) {
        String moUserId = String.valueOf(e.getKey());
        List<String> ids = new ArrayList<>();
        Object arr = e.getValue();
        if (arr instanceof List<?> list) {
          for (Object id : list) ids.add(String.valueOf(id));
        }
        moToApplicationIds.put(moUserId, ids);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getOrCreateObject(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
    Map<String, Object> created = new LinkedHashMap<>();
    parent.put(key, created);
    return created;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> getOrCreateList(Map<String, Object> parent, String key) {
    Object o = parent.get(key);
    if (o instanceof List<?> l) return (List<Object>) l;
    List<Object> created = new ArrayList<>();
    parent.put(key, created);
    return created;
  }
}

