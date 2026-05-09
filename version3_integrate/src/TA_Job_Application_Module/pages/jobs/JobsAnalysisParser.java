package TA_Job_Application_Module.pages.jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JobsAnalysisParser {
    private JobsAnalysisParser() {
    }

    public static double extractMatchScore(String analysis) {
        if (analysis == null) {
            return 0;
        }
        Pattern pattern = Pattern.compile("\"matchScore\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(analysis);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public static double extractMatchScoreByType(String analysis, String type) {
        if (analysis == null) {
            return 0;
        }
        Pattern pattern = Pattern.compile("\"" + type + "\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(analysis);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public static String[] extractStrengths(String analysis) {
        return extractJsonArray(analysis, "strengths");
    }

    public static String[] extractWeaknesses(String analysis) {
        return extractJsonArray(analysis, "weaknesses");
    }

    public static String[] extractRecommendations(String analysis) {
        return extractJsonArray(analysis, "recommendations");
    }

    public static String extractBriefAnalysis(String analysis) {
        if (analysis == null || analysis.isEmpty()) {
            return "Analysis in progress...";
        }
        String[] lines = analysis.split("\n");
        StringBuilder brief = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && count < 2) {
                brief.append(trimmed).append(" ");
                count++;
            }
        }
        String result = brief.toString().trim();
        if (result.length() > 150) {
            result = result.substring(0, 147) + "...";
        }
        return result.isEmpty() ? "Tap to view full analysis" : result;
    }

    private static String[] extractJsonArray(String analysis, String fieldName) {
        if (analysis == null || analysis.isEmpty()) {
            return new String[0];
        }

        try {
            JsonObject root = JsonParser.parseString(analysis).getAsJsonObject();
            if (root.has(fieldName)) {
                JsonArray arr = root.getAsJsonArray(fieldName);
                String[] result = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    result[i] = arr.get(i).getAsString();
                }
                return result;
            }
        } catch (Exception ignored) {
            String patternStr = "\"" + fieldName + "\"\\s*:\\s*\\[([^\\]]*)\\]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(analysis);
            if (matcher.find()) {
                String arr = matcher.group(1);
                List<String> items = new ArrayList<>();
                int depth = 0;
                int start = 0;
                for (int i = 0; i < arr.length(); i++) {
                    char c = arr.charAt(i);
                    if (c == '"' && (i == 0 || arr.charAt(i - 1) != '\\')) {
                        depth++;
                    }
                    if (c == ',' && depth % 2 == 0) {
                        String item = arr.substring(start, i).trim().replace("\"", "");
                        if (!item.isEmpty()) {
                            items.add(item);
                        }
                        start = i + 1;
                    }
                }
                String lastItem = arr.substring(start).trim().replace("\"", "");
                if (!lastItem.isEmpty()) {
                    items.add(lastItem);
                }
                return items.toArray(new String[0]);
            }
        }
        return new String[0];
    }
}
