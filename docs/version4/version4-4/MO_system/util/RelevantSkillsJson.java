package MO_system.util;

import MO_system.model.review.ApplicationItem;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses application relevantSkills from JSON (legacy strings or structured objects). */
public final class RelevantSkillsJson {

    private static final Pattern LEVEL_SUFFIX = Pattern.compile(
            "\\s*\\(\\s*(Beginner|Intermediate|Advanced)\\s*\\)\\s*$",
            Pattern.CASE_INSENSITIVE);

    private RelevantSkillsJson() {
    }

    public static List<ApplicationItem.RelevantSkill> parseArray(JsonNode array) {
        List<ApplicationItem.RelevantSkill> result = new ArrayList<>();
        if (array == null || !array.isArray()) {
            return result;
        }
        for (JsonNode n : array) {
            if (n == null || n.isNull()) {
                continue;
            }
            if (n.isTextual()) {
                result.add(parseDisplayString(n.asText()));
            } else if (n.isObject()) {
                String name = text(n, "name");
                String proficiency = text(n, "proficiency");
                result.add(new ApplicationItem.RelevantSkill(name, proficiency));
            }
        }
        return result;
    }

    public static String formatDisplay(ApplicationItem.RelevantSkill skill) {
        if (skill == null || skill.getName() == null || skill.getName().isBlank()) {
            return "";
        }
        String name = skill.getName().trim();
        String prof = skill.getProficiency();
        if (prof != null && !prof.isBlank()) {
            return name + " (" + prof.trim() + ")";
        }
        return name;
    }

    public static String formatList(List<ApplicationItem.RelevantSkill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (ApplicationItem.RelevantSkill sk : skills) {
            String line = formatDisplay(sk);
            if (!line.isEmpty()) {
                parts.add(line);
            }
        }
        return String.join(", ", parts);
    }

    public static String nameForMatching(ApplicationItem.RelevantSkill skill) {
        if (skill == null) {
            return "";
        }
        if (skill.getName() != null && !skill.getName().isBlank()) {
            return skill.getName().trim();
        }
        return stripLevelSuffix(formatDisplay(skill));
    }

    private static ApplicationItem.RelevantSkill parseDisplayString(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ApplicationItem.RelevantSkill("", null);
        }
        String trimmed = raw.trim();
        Matcher m = LEVEL_SUFFIX.matcher(trimmed);
        if (m.find()) {
            String name = trimmed.substring(0, m.start()).trim();
            String level = m.group(1);
            if (level != null && !level.isEmpty()) {
                level = level.substring(0, 1).toUpperCase() + level.substring(1).toLowerCase();
            }
            return new ApplicationItem.RelevantSkill(name, level);
        }
        return new ApplicationItem.RelevantSkill(trimmed, null);
    }

    private static String stripLevelSuffix(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        Matcher m = LEVEL_SUFFIX.matcher(raw.trim());
        if (m.find()) {
            return raw.substring(0, m.start()).trim();
        }
        return raw.trim();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("");
    }
}
