package com.mojobsystem.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojobsystem.model.job.Job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Writes {@code data/jobs/{jobId}.json} in the same nested shape as the bundled sample jobs.
 */
final class JobRichFileWriter {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ISO_LOCAL_DATE;

    private JobRichFileWriter() {
    }

    static ObjectNode toJsonNode(Job job, ObjectMapper mapper, String defaultMoUserId) {
        String jobId = job.getId();
        String now = LocalDateTime.now().format(DT);
        String today = LocalDate.now().format(DAY);

        String deadlineDay = job.getDeadline() == null || job.getDeadline().isBlank()
                ? LocalDate.now().plusWeeks(2).format(DAY)
                : job.getDeadline().trim();

        ObjectNode root = mapper.createObjectNode();
        root.put("jobId", jobId);
        root.put("title", nullToEmpty(job.getTitle()));

        ObjectNode course = mapper.createObjectNode();
        course.put("courseCode", nullToEmpty(job.getModuleCode()));
        course.put("courseName", nullToEmpty(job.getModuleName()));
        course.put("term", nullToEmpty(job.getCourseTerm()));
        course.put("year", job.getCourseYear() > 0 ? job.getCourseYear() : LocalDate.now().getYear());
        root.set("course", course);

        root.put("department", nullToEmpty(job.getDepartment()));

        ObjectNode instructor = mapper.createObjectNode();
        instructor.put("name", nullToEmpty(job.getInstructorName()));
        instructor.put("email", nullToEmpty(job.getInstructorEmail()));
        root.set("instructor", instructor);

        ObjectNode employment = mapper.createObjectNode();
        employment.put("jobType", "TA");
        employment.put("employmentType", nullToEmpty(job.getEmploymentType()));
        employment.put("weeklyHours", job.getWeeklyHours());
        employment.put("locationMode", nullToEmpty(job.getLocationMode()));
        employment.put("locationDetail", "");
        root.set("employment", employment);

        ObjectNode dates = mapper.createObjectNode();
        dates.put("postedAt", now);
        dates.put("deadline", deadlineDay + "T23:59:59");
        dates.put("startDate", today);
        dates.put("endDate", LocalDate.now().plusMonths(3).format(DAY));
        root.set("dates", dates);

        ObjectNode content = mapper.createObjectNode();
        String desc = nullToEmpty(job.getDescription());
        content.put("summary", summarize(desc));
        content.put("description", desc);
        ArrayNode responsibilities = mapper.createArrayNode();
        responsibilities.add("Support teaching activities for this module");
        content.set("responsibilities", responsibilities);

        ArrayNode requirements = mapper.createArrayNode();
        requirements.add("TA quota: " + job.getQuota());
        String add = job.getAdditionalRequirements();
        if (add != null && !add.isBlank()) {
            for (String line : add.split("\\R")) {
                String t = line.trim();
                if (!t.isEmpty()) {
                    requirements.add(t);
                }
            }
        }
        content.set("requirements", requirements);

        ArrayNode preferred = mapper.createArrayNode();
        if (job.getRequiredSkills() != null) {
            for (String s : job.getRequiredSkills()) {
                preferred.add(s);
            }
        }
        content.set("preferredSkills", preferred);
        root.set("content", content);

        ObjectNode ownership = mapper.createObjectNode();
        ownership.put("createdBy", defaultMoUserId);
        ArrayNode managed = mapper.createArrayNode();
        managed.add(defaultMoUserId);
        ownership.set("managedBy", managed);
        ownership.put("lastEditedBy", defaultMoUserId);
        root.set("ownership", ownership);

        ObjectNode publication = mapper.createObjectNode();
        boolean draft = "Draft".equalsIgnoreCase(job.getStatus());
        publication.put("status", draft ? "draft" : "published");
        if (draft) {
            publication.putNull("publishedAt");
        } else {
            publication.put("publishedAt", now);
        }
        publication.put("publishedBy", defaultMoUserId);
        root.set("publication", publication);

        ObjectNode lifecycle = mapper.createObjectNode();
        lifecycle.put("status", lifecycleStatus(job.getStatus()));
        lifecycle.put("isDeleted", false);
        lifecycle.putNull("deletedAt");
        lifecycle.putNull("deletedBy");
        lifecycle.put("closeReason", "");
        root.set("lifecycle", lifecycle);

        ObjectNode stats = mapper.createObjectNode();
        stats.put("applicationCount", job.getApplicantsCount());
        stats.put("pendingCount", 0);
        stats.put("acceptedCount", 0);
        stats.put("rejectedCount", 0);
        root.set("stats", stats);

        ObjectNode meta = mapper.createObjectNode();
        meta.put("createdAt", now);
        meta.put("updatedAt", now);
        root.set("meta", meta);

        return root;
    }

    private static String lifecycleStatus(String status) {
        if (status == null) {
            return "open";
        }
        String s = status.trim().toLowerCase(Locale.ENGLISH);
        if ("closed".equals(s)) {
            return "closed";
        }
        if ("draft".equals(s)) {
            return "open";
        }
        return "open";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String summarize(String description) {
        if (description == null || description.isBlank()) {
            return "TA recruitment for this module.";
        }
        String oneLine = description.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 160 ? oneLine : oneLine.substring(0, 157) + "...";
    }
}
