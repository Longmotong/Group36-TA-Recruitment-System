package com.mojobsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates TA job description drafts via OpenAI-compatible APIs.
 * Supports both /chat/completions and /responses endpoints.
 */
public class JobDescriptionAiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public record JobDescriptionInput(
            String jobTitle,
            String moduleCode,
            String moduleName,
            String department,
            String instructorName,
            int weeklyHours,
            int quota,
            String locationMode,
            String employmentType,
            List<String> requiredSkills,
            String additionalRequirements,
            String userPrompt
    ) {
    }

    public String generateDescription(JobDescriptionInput input) throws IOException, InterruptedException {
        String apiKey = env("OPENAI_API_KEY");
        if (apiKey.isBlank() || apiKey.contains("你的")) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }
        String endpoint = env("OPENAI_BASE_URL");
        if (endpoint.isBlank()) {
            endpoint = "https://api.openai.com/v1/chat/completions";
        }
        String model = env("OPENAI_MODEL");
        if (model.isBlank()) {
            model = "gpt-4o-mini";
        }

        boolean responsesApi = endpoint.contains("/responses");
        ObjectNode body = responsesApi
                ? buildResponsesBody(model, input)
                : buildChatBody(model, input);

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(35))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("AI API error " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        String content = responsesApi ? extractFromResponses(root) : extractFromChat(root);
        if (content.isBlank()) {
            throw new IOException("AI response content is empty");
        }
        return content.trim();
    }

    private static ObjectNode buildChatBody(String model, JobDescriptionInput input) {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.25);
        body.put("max_tokens", 220);
        ArrayNode messages = body.putArray("messages");

        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content",
                "You write concise university TA job descriptions. " +
                        "Return plain text only. Keep it short and practical.");

        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", buildPrompt(input));
        return body;
    }

    private static ObjectNode buildResponsesBody(String model, JobDescriptionInput input) {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.25);
        body.put("max_output_tokens", 220);
        ArrayNode in = body.putArray("input");

        ObjectNode system = in.addObject();
        system.put("role", "system");
        ArrayNode systemContent = system.putArray("content");
        ObjectNode s1 = systemContent.addObject();
        s1.put("type", "input_text");
        s1.put("text",
                "You write concise university TA job descriptions. " +
                        "Return plain text only. Keep it short and practical.");

        ObjectNode user = in.addObject();
        user.put("role", "user");
        ArrayNode userContent = user.putArray("content");
        ObjectNode u1 = userContent.addObject();
        u1.put("type", "input_text");
        u1.put("text", buildPrompt(input));
        return body;
    }

    private static String extractFromChat(JsonNode root) throws IOException {
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IOException("AI response missing choices");
        }
        JsonNode content = choices.get(0).path("message").path("content");
        if (content.isTextual()) {
            return content.asText("");
        }
        if (content.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode n : content) {
                String t = n.path("text").asText("");
                if (!t.isBlank()) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(t);
                }
            }
            return sb.toString();
        }
        return "";
    }

    private static String extractFromResponses(JsonNode root) throws IOException {
        String outputText = root.path("output_text").asText("");
        if (!outputText.isBlank()) {
            return outputText;
        }
        JsonNode output = root.path("output");
        if (!output.isArray() || output.isEmpty()) {
            throw new IOException("AI response missing output");
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode c : content) {
                String t = c.path("text").asText("");
                if (!t.isBlank()) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(t);
                }
            }
        }
        return sb.toString();
    }

    private static String buildPrompt(JobDescriptionInput input) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a TA job description based on these details.\n");
        sb.append("Write in ONE short narrative paragraph.\n");
        sb.append("Target length: 80-140 words.\n");
        sb.append("Most important rule: prioritize the MO's extra prompt above all other details.\n");
        sb.append("Do not add extra sections, titles, bullet points, or generic filler.\n\n");
        sb.append("Structured details (for context):\n");
        sb.append("- Job title: ").append(nz(input.jobTitle())).append('\n');
        sb.append("- Module: ").append(nz(input.moduleCode())).append(" ").append(nz(input.moduleName())).append('\n');
        sb.append("- Department: ").append(nz(input.department())).append('\n');
        sb.append("- Instructor: ").append(nz(input.instructorName())).append('\n');
        sb.append("- Weekly hours: ").append(input.weeklyHours() > 0 ? input.weeklyHours() : "not specified").append('\n');
        sb.append("- Quota: ").append(input.quota() > 0 ? input.quota() : "not specified").append('\n');
        sb.append("- Location mode: ").append(nz(input.locationMode())).append('\n');
        sb.append("- Employment type: ").append(nz(input.employmentType())).append('\n');
        sb.append("- Required skills: ").append(input.requiredSkills() == null || input.requiredSkills().isEmpty()
                ? "not specified"
                : String.join(", ", input.requiredSkills())).append('\n');
        sb.append("- Additional requirements: ").append(nz(input.additionalRequirements())).append('\n');
        if (input.userPrompt() != null && !input.userPrompt().isBlank()) {
            sb.append("- Extra prompt from MO (HIGHEST PRIORITY): ").append(input.userPrompt().trim()).append('\n');
        }
        sb.append("\nFinal constraints:\n");
        sb.append("1) Follow MO extra prompt first; if conflict, MO prompt wins.\n");
        sb.append("2) Keep wording specific to this role/module.\n");
        sb.append("3) Keep output short and direct.\n");
        return sb.toString();
    }

    private static String env(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        String fallback = loadDotEnvValues().get(key);
        return fallback == null ? "" : fallback.trim();
    }

    private static Map<String, String> loadDotEnvValues() {
        Path userDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path[] candidates = new Path[]{
                userDir.resolve(".vscode").resolve(".env.local"),
                userDir.resolve(".env.local"),
                userDir.resolve(".env"),
                userDir.resolve("MO_System").resolve(".vscode").resolve(".env.local"),
                userDir.resolve("MO_System").resolve(".env.local"),
                userDir.resolve("MO_System").resolve(".env")
        };

        for (Path p : candidates) {
            try {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                return parseDotEnv(Files.readAllLines(p));
            } catch (Exception ignored) {
                // Keep trying other candidate files.
            }
        }
        return Map.of();
    }

    private static Map<String, String> parseDotEnv(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (String raw : lines) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String k = line.substring(0, idx).trim();
            String v = line.substring(idx + 1).trim();
            if (v.length() >= 2 && ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'")))) {
                v = v.substring(1, v.length() - 1);
            }
            if (!k.isEmpty()) {
                values.put(k, v);
            }
        }
        return values;
    }

    private static String nz(String v) {
        return (v == null || v.isBlank()) ? "not specified" : v.trim();
    }
}
