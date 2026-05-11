package com.mojobsystem.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojobsystem.model.Job;

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

    static ObjectNode toJsonNode(Job job, ObjectMapper mapper, JsonNode existingRoot, String defaultMoUserId) {
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

        ObjectNode existingOwnership = existingObject(existingRoot, "ownership");
        ObjectNode ownership = mapper.createObjectNode();
        ownership.put("createdBy", text(existingOwnership, "createdBy", defaultMoUserId));
        ArrayNode managed = mapper.createArrayNode();
        JsonNode existingManaged = existingOwnership == null ? null : existingOwnership.get("managedBy");
        if (existingManaged != null && existingManaged.isArray() && !existingManaged.isEmpty()) {
            for (JsonNode n : existingManaged) {
                managed.add(n.asText());
            }
        } else {
            managed.add(defaultMoUserId);
        }
        ownership.set("managedBy", managed);
        ownership.put("lastEditedBy", defaultMoUserId);
        root.set("ownership", ownership);

        ObjectNode existingPublication = existingObject(existingRoot, "publication");
        ObjectNode publication = mapper.createObjectNode();
        boolean draft = "Draft".equalsIgnoreCase(job.getStatus());
        publication.put("status", draft ? "draft" : "published");
        if (draft) {
            publication.putNull("publishedAt");
        } else {
            String publishedAt = text(existingPublication, "publishedAt", now);
            publication.put("publishedAt", publishedAt.isBlank() ? now : publishedAt);
        }
        publication.put("publishedBy", text(existingPublication, "publishedBy", defaultMoUserId));
        root.set("publication", publication);

        ObjectNode existingLifecycle = existingObject(existingRoot, "lifecycle");
        ObjectNode lifecycle = mapper.createObjectNode();
        lifecycle.put("status", lifecycleStatus(job.getStatus()));
        lifecycle.put("isDeleted", bool(existingLifecycle, "isDeleted", false));
        putNullableText(lifecycle, "deletedAt", text(existingLifecycle, "deletedAt", ""));
        putNullableText(lifecycle, "deletedBy", text(existingLifecycle, "deletedBy", ""));
        lifecycle.put("closeReason", text(existingLifecycle, "closeReason", ""));
        root.set("lifecycle", lifecycle);

        ObjectNode existingStats = existingObject(existingRoot, "stats");
        ObjectNode stats = mapper.createObjectNode();
        int existingAppCount = intValue(existingStats, "applicationCount", -1);
        stats.put("applicationCount", Math.max(job.getApplicantsCount(), Math.max(existingAppCount, 0)));
        stats.put("pendingCount", intValue(existingStats, "pendingCount", 0));
        stats.put("acceptedCount", intValue(existingStats, "acceptedCount", 0));
        stats.put("rejectedCount", intValue(existingStats, "rejectedCount", 0));
        root.set("stats", stats);

        ObjectNode existingMeta = existingObject(existingRoot, "meta");
        ObjectNode meta = mapper.createObjectNode();
        meta.put("createdAt", text(existingMeta, "createdAt", now));
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
            return "draft";
        }
        return "open";
    }

    private static ObjectNode existingObject(JsonNode parent, String field) {
        if (parent == null || parent.isMissingNode()) {
            return null;
        }
        JsonNode n = parent.get(field);
        return (n instanceof ObjectNode obj) ? obj : null;
    }

    private static String text(ObjectNode parent, String field, String fallback) {
        if (parent == null) {
            return fallback;
        }
        JsonNode n = parent.get(field);
        return (n == null || n.isNull()) ? fallback : n.asText(fallback);
    }

    private static int intValue(ObjectNode parent, String field, int fallback) {
        if (parent == null) {
            return fallback;
        }
        JsonNode n = parent.get(field);
        return (n == null || n.isNull()) ? fallback : n.asInt(fallback);
    }

    private static boolean bool(ObjectNode parent, String field, boolean fallback) {
        if (parent == null) {
            return fallback;
        }
        JsonNode n = parent.get(field);
        return (n == null || n.isNull()) ? fallback : n.asBoolean(fallback);
    }

    private static void putNullableText(ObjectNode node, String field, String value) {
        if (value == null || value.isBlank()) {
            node.putNull(field);
        } else {
            node.put(field, value);
        }
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
