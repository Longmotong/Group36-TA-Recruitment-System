package appreview.ui;

import appreview.model.ApplicationRecord;
import appreview.model.JobRecord;
import appreview.model.ReviewRecord;
import appreview.model.TaProfile;
import appreview.service.ApplicationReviewService;
import appreview.util.ConsolePrinter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console menu implementation for MO review module.
 */
public class ConsoleApp {
    private final ApplicationReviewService service;
    private final Scanner scanner;

    public ConsoleApp(ApplicationReviewService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Start interactive loop.
     */
    public void start() {
        while (true) {
            showDashboard();
            String cmd = read("Select option: ");
            if ("1".equals(cmd)) {
                System.out.println("Job Management feature placeholder. Press Enter to continue.");
                read("");
            } else if ("2".equals(cmd)) {
                taApplicationsPage();
            } else if ("3".equals(cmd)) {
                System.out.println("Logout success. Bye.");
                return;
            } else {
                System.out.println("Invalid input.");
            }
        }
    }

    private void showDashboard() {
        System.out.println("\n================ MO Dashboard ================");
        System.out.println("Top Navigation: Home | Job Management | Application Review | Logout");
        System.out.println("Active Courses: " + service.getActiveCourses());
        System.out.println("Open Job Postings: " + service.getOpenJobs());
        System.out.println("Pending Reviews: " + service.getPendingReviews());
        System.out.println("---------------------------------------------");
        System.out.println("1) Go to Job Management (placeholder)");
        System.out.println("2) Go to Application Review");
        System.out.println("3) Logout");
    }

    private void taApplicationsPage() {
        String keyword = "";
        String courseFilter = "all";
        String statusFilter = "all";
        while (true) {
            System.out.println("\n================ TA Applications ================");
            Map<String, Long> stats = service.getApplicationStats();
            System.out.println("Total: " + stats.get("total")
                    + " | Pending: " + stats.get("pending")
                    + " | Approved: " + stats.get("approved")
                    + " | Rejected: " + stats.get("rejected"));
            System.out.println("Current Filters => keyword: " + blank(keyword, "none")
                    + ", course: " + courseFilter + ", status: " + statusFilter);
            List<ApplicationRecord> rows = service.filterApplications(keyword, courseFilter, statusFilter);
            printApplicationTable(rows);
            System.out.println("1) Search TA name/studentId");
            System.out.println("2) Filter by course code/name");
            System.out.println("3) Filter by status (all/pending/approved/rejected)");
            System.out.println("4) View Application Detail");
            System.out.println("5) Quick Approve");
            System.out.println("6) Quick Reject");
            System.out.println("7) Full Review");
            System.out.println("8) View My Review Records");
            System.out.println("9) Back Home");
            String cmd = read("Choose: ");
            if ("1".equals(cmd)) {
                keyword = read("Enter keyword: ");
            } else if ("2".equals(cmd)) {
                courseFilter = read("Enter course filter (or all): ");
            } else if ("3".equals(cmd)) {
                statusFilter = read("Enter status (all/pending/approved/rejected): ");
            } else if ("4".equals(cmd)) {
                ApplicationRecord app = inputApp();
                if (app != null) {
                    detailPage(app);
                }
            } else if ("5".equals(cmd)) {
                ApplicationRecord app = inputApp();
                if (app != null) {
                    decide(app, true, true);
                }
            } else if ("6".equals(cmd)) {
                ApplicationRecord app = inputApp();
                if (app != null) {
                    decide(app, false, true);
                }
            } else if ("7".equals(cmd)) {
                ApplicationRecord app = inputApp();
                if (app != null) {
                    reviewPage(app);
                }
            } else if ("8".equals(cmd)) {
                reviewRecordsPage();
            } else if ("9".equals(cmd)) {
                return;
            } else {
                System.out.println("Invalid input.");
            }
        }
    }

    private void detailPage(ApplicationRecord app) {
        while (true) {
            ApplicationRecord latest = service.findApplicationById(app.applicationId);
            if (latest == null) {
                return;
            }
            JobRecord job = service.findJob(latest.jobId);
            TaProfile ta = service.findTaByUserId(latest.userId);
            int[] scoreInfo = service.scoreAndMissings(latest);
            int workload = service.getCurrentWorkloadHours(latest.studentId);
            List<ApplicationRecord> assignments = service.getCurrentAssignments(latest.studentId);

            System.out.println("\n============== TA Application Detail ==============");
            System.out.println("Application: " + latest.applicationId);
            System.out.println("Applied Course: " + latest.courseName + " (" + latest.courseCode + ")");
            System.out.println("Application Date: " + latest.applicationDate);
            System.out.println("Current Status: " + latest.statusLabel);
            System.out.println("-- Skills Matching Analysis --");
            System.out.println("Match Score: " + scoreInfo[0] + "/100 (matched " + scoreInfo[1] + ", missing " + scoreInfo[2] + ")");
            if (job != null && ta != null) {
                System.out.println("Required Skills: " + String.join(", ", job.preferredSkills));
                System.out.println("Applicant Skills: " + String.join(", ", ta.skills));
            }
            if (scoreInfo[2] > 0) {
                System.out.println(ConsolePrinter.color(ConsolePrinter.RED, "Warning: Missing skills exist."));
            } else {
                System.out.println(ConsolePrinter.color(ConsolePrinter.GREEN, "✓ All skills met."));
            }
            System.out.println("-- Current TA Assignments & Workload --");
            String workloadColor = workload >= 15 ? ConsolePrinter.YELLOW : (workload > 0 ? ConsolePrinter.BLUE : ConsolePrinter.CYAN);
            System.out.println(ConsolePrinter.color(workloadColor, "Total Weekly Workload: " + workload + " hrs/week"));
            if (assignments.isEmpty()) {
                System.out.println("No current approved assignments.");
            } else {
                for (ApplicationRecord a : assignments) {
                    System.out.println("- " + a.courseName + " (" + a.courseCode + "), " + a.weeklyHours + " hrs/week");
                }
            }
            if (workload >= 15) {
                System.out.println(ConsolePrinter.color(ConsolePrinter.YELLOW, "Conflict Warning: workload reaches or exceeds 15 hours."));
            }
            System.out.println("-- Personal Information --");
            if (ta != null) {
                System.out.println("Name: " + ta.fullName + ", Student ID: " + ta.studentId + ", Major: " + ta.major + ", GPA: " + ta.gpa);
                System.out.println("Contact: " + ta.phone + " | " + ta.email);
                System.out.println("-- Experience & Background --");
                System.out.println(blank(latest.experience, ta.experienceSummary));
                System.out.println("CV Path: " + blank(latest.cvPath, ta.cvPath));
            }
            System.out.println("1) Review Application");
            System.out.println("2) Review Now");
            System.out.println("3) Back to TA Applications");
            String cmd = read("Choose: ");
            if ("1".equals(cmd) || "2".equals(cmd)) {
                reviewPage(latest);
                return;
            }
            if ("3".equals(cmd)) {
                return;
            }
        }
    }

    private void reviewPage(ApplicationRecord app) {
        ApplicationRecord latest = service.findApplicationById(app.applicationId);
        if (latest == null) {
            return;
        }
        JobRecord job = service.findJob(latest.jobId);
        TaProfile ta = service.findTaByUserId(latest.userId);
        System.out.println("\n============== Review TA Application ==============");
        System.out.println("Application Context: " + latest.courseName + " | " + latest.taName + " | " + latest.studentId);
        System.out.println("--- Course Requirements ---");
        if (job != null) {
            System.out.println("Required Skills: " + String.join(", ", job.preferredSkills));
            System.out.println("Weekly Workload: " + job.weeklyHours + " hours/week");
            System.out.println("TAs Needed: N/A");
            System.out.println("Preferred Qualifications: " + String.join("; ", job.requirements));
            System.out.println("Responsibilities: " + String.join("; ", job.responsibilities));
        }
        System.out.println("--- Applicant Qualifications ---");
        if (ta != null) {
            System.out.println("Skills: " + String.join(", ", ta.skills));
            System.out.println("Academic: " + ta.year + ", GPA " + ta.gpa + ", Major " + ta.major);
            System.out.println("Experience: " + blank(latest.experience, ta.experienceSummary));
        }
        System.out.println("Review Decision: 1) Approve 2) Reject 3) Cancel");
        String decision = read("Choose: ");
        if ("3".equals(decision)) {
            System.out.println("Cancelled. Back to detail page.");
            return;
        }
        if (!"1".equals(decision) && !"2".equals(decision)) {
            System.out.println("Invalid decision.");
            return;
        }
        String notes = read("Review Notes (optional): ");
        decide(latest, "1".equals(decision), false, notes);
    }

    private void reviewRecordsPage() {
        String keyword = "";
        String result = "all";
        int dayRange = 0;
        while (true) {
            System.out.println("\n============== My Review Records ==============");
            Map<String, Long> stats = service.reviewStats();
            System.out.println("Total Reviews: " + stats.get("total")
                    + " | Approved: " + stats.get("approved")
                    + " | Rejected: " + stats.get("rejected"));
            System.out.println("Filters => keyword: " + blank(keyword, "none")
                    + ", time: " + (dayRange == 0 ? "all" : dayRange + "d")
                    + ", result: " + result);
            List<ReviewRecord> rows = service.filterReviews(keyword, result, dayRange);
            for (ReviewRecord r : rows) {
                System.out.println(r.applicationId + " | " + r.courseName + "(" + r.courseCode + ") | "
                        + r.taName + " | " + r.studentId + " | " + r.reviewDate + " | "
                        + r.result + " | " + r.reviewer);
            }
            System.out.println("1) Search keyword");
            System.out.println("2) Filter time (0=all,7,30)");
            System.out.println("3) Filter result (all/approved/rejected)");
            System.out.println("4) View record detail by applicationId");
            System.out.println("5) Export records");
            System.out.println("6) Back to TA Applications");
            String cmd = read("Choose: ");
            if ("1".equals(cmd)) {
                keyword = read("keyword: ");
            } else if ("2".equals(cmd)) {
                try {
                    dayRange = Integer.parseInt(read("days(0/7/30): "));
                } catch (Exception ex) {
                    dayRange = 0;
                }
            } else if ("3".equals(cmd)) {
                result = read("result: ");
            } else if ("4".equals(cmd)) {
                ApplicationRecord app = inputApp();
                if (app != null) {
                    detailPage(app);
                }
            } else if ("5".equals(cmd)) {
                exportRecords(rows);
            } else if ("6".equals(cmd)) {
                return;
            }
        }
    }

    private void exportRecords(List<ReviewRecord> rows) {
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Path dir = Paths.get("export");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path csv = dir.resolve("review_records_" + ts + ".csv");
            Path txt = dir.resolve("review_records_" + ts + ".txt");
            Path json = dir.resolve("review_records_" + ts + ".json");

            StringBuilder c = new StringBuilder("applicationId,courseName,courseCode,taName,studentId,reviewDate,result,reviewer,notes\n");
            StringBuilder t = new StringBuilder();
            StringBuilder j = new StringBuilder("[\n");
            for (int i = 0; i < rows.size(); i++) {
                ReviewRecord r = rows.get(i);
                c.append(safe(r.applicationId)).append(",").append(safe(r.courseName)).append(",")
                        .append(safe(r.courseCode)).append(",").append(safe(r.taName)).append(",")
                        .append(safe(r.studentId)).append(",").append(safe(r.reviewDate)).append(",")
                        .append(safe(r.result)).append(",").append(safe(r.reviewer)).append(",")
                        .append(safe(r.notes)).append("\n");
                t.append(r.applicationId).append(" | ").append(r.courseName).append(" | ")
                        .append(r.taName).append(" | ").append(r.result).append(" | ").append(r.reviewDate).append("\n");
                j.append("  {\"applicationId\":\"").append(esc(r.applicationId)).append("\",")
                        .append("\"courseName\":\"").append(esc(r.courseName)).append("\",")
                        .append("\"courseCode\":\"").append(esc(r.courseCode)).append("\",")
                        .append("\"taName\":\"").append(esc(r.taName)).append("\",")
                        .append("\"studentId\":\"").append(esc(r.studentId)).append("\",")
                        .append("\"reviewDate\":\"").append(esc(r.reviewDate)).append("\",")
                        .append("\"result\":\"").append(esc(r.result)).append("\",")
                        .append("\"reviewer\":\"").append(esc(r.reviewer)).append("\",")
                        .append("\"notes\":\"").append(esc(r.notes)).append("\"}");
                if (i < rows.size() - 1) {
                    j.append(",");
                }
                j.append("\n");
            }
            j.append("]\n");
            Files.write(csv, c.toString().getBytes(StandardCharsets.UTF_8));
            Files.write(txt, t.toString().getBytes(StandardCharsets.UTF_8));
            Files.write(json, j.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Export success: " + csv + ", " + txt + ", " + json);
        } catch (Exception ex) {
            System.out.println("Export failed: " + ex.getMessage());
        }
    }

    private void printApplicationTable(List<ApplicationRecord> rows) {
        System.out.println("applicationId | TA Name | Student ID | Applied Course | Match | Missing Skills | Workload | Status");
        for (ApplicationRecord a : rows) {
            int[] scoreInfo = service.scoreAndMissings(a);
            String scoreText = String.valueOf(scoreInfo[0]);
            if (scoreInfo[0] >= 80) {
                scoreText = ConsolePrinter.color(ConsolePrinter.GREEN, scoreText);
            } else if (scoreInfo[0] >= 60) {
                scoreText = ConsolePrinter.color(ConsolePrinter.YELLOW, scoreText);
            } else {
                scoreText = ConsolePrinter.color(ConsolePrinter.RED, scoreText);
            }
            String missing = scoreInfo[2] > 0
                    ? ConsolePrinter.color(ConsolePrinter.RED, "Missing " + scoreInfo[2])
                    : ConsolePrinter.color(ConsolePrinter.GREEN, "✓ All skills met");
            int hours = service.getCurrentWorkloadHours(a.studentId);
            int assignCount = service.getCurrentAssignments(a.studentId).size();
            String workload = hours + " hrs/week, " + assignCount + " course(s)";
            if (hours >= 15) {
                workload = ConsolePrinter.color(ConsolePrinter.YELLOW, workload);
            }
            System.out.println(a.applicationId + " | " + a.taName + " | " + a.studentId + " | "
                    + a.courseCode + " | " + scoreText + " | " + missing + " | "
                    + workload + " | " + a.statusLabel);
        }
    }

    private ApplicationRecord inputApp() {
        String id = read("Enter applicationId (e.g., app_000001): ");
        ApplicationRecord app = service.findApplicationById(id);
        if (app == null) {
            System.out.println("Application not found.");
        }
        return app;
    }

    private void decide(ApplicationRecord app, boolean approve, boolean quick) {
        decide(app, approve, quick, "");
    }

    private void decide(ApplicationRecord app, boolean approve, boolean quick, String notes) {
        try {
            service.quickDecision(app, approve, notes);
            System.out.println((quick ? "Quick " : "") + (approve ? "approve" : "reject") + " success.");
        } catch (IOException ex) {
            System.out.println("Write failed: " + ex.getMessage());
        }
    }

    private String read(String prompt) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        return scanner.nextLine().trim();
    }

    private static String blank(String val, String fallback) {
        return val == null || val.trim().isEmpty() ? fallback : val;
    }

    private static String safe(String s) {
        return "\"" + esc(s) + "\"";
    }

    private static String esc(String s) {
        return (s == null ? "" : s).replace("\"", "\"\"");
    }
}
