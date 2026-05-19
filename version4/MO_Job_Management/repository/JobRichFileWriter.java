package MO_system.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import MO_system.model.job.Job;

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
        return toJsonNode(job, mapper, defaultMoUserId, null);
    }

    static ObjectNode toJsonNode(Job job, ObjectMapper mapper, String defaultMoUserId, JsonNode existingRoot) {
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

        ObjectNode previousOwnership = objectNode(existingRoot, "ownership");
        String createdBy = firstNonBlank(
                text(previousOwnership, "createdBy"),
                defaultMoUserId
        );
        String lastEditedBy = firstNonBlank(
                defaultMoUserId,
                text(previousOwnership, "lastEditedBy"),
                createdBy
        );
        ObjectNode ownership = mapper.createObjectNode();
        ownership.put("createdBy", createdBy);
        ArrayNode managed = mapper.createArrayNode();
        JsonNode existingManaged = previousOwnership == null ? null : previousOwnership.get("managedBy");
        if (existingManaged != null && existingManaged.isArray()) {
            for (JsonNode n : existingManaged) {
                String mo = n == null ? "" : n.asText("");
                if (!mo.isBlank()) {
                    managed.add(mo);
                }
            }
        }
        if (managed.isEmpty() && defaultMoUserId != null && !defaultMoUserId.isBlank()) {
            managed.add(defaultMoUserId);
        }
        ownership.set("managedBy", managed);
        ownership.put("lastEditedBy", lastEditedBy);
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

        ObjectNode previousStats = objectNode(existingRoot, "stats");
        ObjectNode stats = mapper.createObjectNode();
        int appCount = job.getApplicantsCount() > 0 ? job.getApplicantsCount() : intValue(previousStats, "applicationCount", 0);
        stats.put("applicationCount", appCount);
        stats.put("pendingCount", intValue(previousStats, "pendingCount", 0));
        stats.put("acceptedCount", intValue(previousStats, "acceptedCount", 0));
        stats.put("rejectedCount", intValue(previousStats, "rejectedCount", 0));
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

    private static ObjectNode objectNode(JsonNode parent, String field) {
        if (parent == null || parent.isMissingNode() || field == null || field.isBlank()) {
            return null;
        }
        JsonNode n = parent.get(field);
        return n instanceof ObjectNode o ? o : null;
    }

    private static String text(ObjectNode parent, String field) {
        if (parent == null || field == null || field.isBlank()) {
            return "";
        }
        JsonNode n = parent.get(field);
        return n == null || n.isNull() ? "" : n.asText("");
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    private static int intValue(ObjectNode parent, String field, int def) {
        if (parent == null || field == null || field.isBlank()) {
            return def;
        }
        JsonNode n = parent.get(field);
        if (n == null || n.isNull()) {
            return def;
        }
        if (n.isInt() || n.isLong()) {
            return n.asInt(def);
        }
        try {
            return Integer.parseInt(n.asText(""));
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    private static String summarize(String description) {
        if (description == null || description.isBlank()) {
            return "TA recruitment for this module.";
        }
        String oneLine = description.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 160 ? oneLine : oneLine.substring(0, 157) + "...";
    }
}
