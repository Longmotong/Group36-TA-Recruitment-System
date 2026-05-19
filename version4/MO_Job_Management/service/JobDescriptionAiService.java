package MO_system.service;

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
 * Generates TA job description drafts via Doubao (Volcengine Ark) chat/completions API.
 * Request/response format is aligned with TA module's Doubao integration.
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

    private static final String DOUBAO_ENDPOINT =
            "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DOUBAO_MODEL = "doubao-seed-1-8-251228";

    public String generateDescription(JobDescriptionInput input) throws IOException, InterruptedException {
        ResolvedAiConfig cfg = resolveConfigForDoubao();
        ObjectNode body = buildChatBody(cfg.model(), input);

        HttpRequest request = HttpRequest.newBuilder(URI.create(cfg.endpoint()))
                .timeout(Duration.ofSeconds(35))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + cfg.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body() == null ? "" : response.body();

        if (statusCode == 401) {
            throw new IOException("Invalid API key. Please check ARK_API_KEY configuration.");
        } else if (statusCode == 429) {
            throw new IOException("Request rate limited. Please try again later.");
        } else if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("API request failed, status code: " + statusCode + ", response: " + responseBody);
        }

        JsonNode root = MAPPER.readTree(responseBody);
        String content = extractFromChat(root);
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

    private static String extractFromChat(JsonNode root) throws IOException {
        if (root.has("error")) {
            String msg = root.path("error").path("message").asText("").trim();
            if (!msg.isBlank()) {
                throw new IOException("AI API error: " + msg);
            }
            throw new IOException("AI API returned an error");
        }

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

    private record ResolvedAiConfig(String apiKey, String endpoint, String model) {
    }

    private static ResolvedAiConfig resolveConfigForDoubao() {
        Map<String, String> fileValues = loadAllConfigFiles();

        String arkKey = firstNonBlank(
                System.getProperty("mo.doubao.api.key"),
                System.getProperty("doubao.api.key"),
                System.getenv("MO_ARK_API_KEY"),
                System.getenv("ARK_API_KEY"),
                System.getenv("DOUBAO_API_KEY"),
                fileValues.get("MO_ARK_API_KEY"),
                fileValues.get("ARK_API_KEY"));
        if (!isUsableApiKey(arkKey)) {
            throw new IllegalStateException(
                    "Missing MO AI API key. Set MO_ARK_API_KEY (preferred) or ARK_API_KEY in config/ai_config.txt, "
                            + "or use -Dmo.doubao.api.key=YOUR_API_KEY");
        }

        String arkModel = firstNonBlank(
                System.getenv("ARK_MODEL"),
                fileValues.get("ARK_MODEL"),
                DOUBAO_MODEL);
        String arkUrl = firstNonBlank(
                System.getenv("ARK_BASE_URL"),
                fileValues.get("ARK_BASE_URL"),
                DOUBAO_ENDPOINT);

        return new ResolvedAiConfig(arkKey, arkUrl, arkModel);
    }

    private static boolean isUsableApiKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String k = key.trim();
        if (k.toLowerCase().contains("your") || "your_api_key_here".equalsIgnoreCase(k)) {
            return false;
        }
        // Reject mistaken file paths stored as API keys.
        if (k.contains(".env") && (k.contains("\\") || k.contains("/") || k.contains(":"))) {
            return false;
        }
        return true;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    private static Map<String, String> loadAllConfigFiles() {
        Map<String, String> merged = new HashMap<>();
        Path userDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path[] candidates = new Path[]{
                userDir.resolve("config").resolve("ai_config.txt"),
                userDir.resolve(".vscode").resolve(".env.local"),
                userDir.resolve(".env.local"),
                userDir.resolve(".env"),
                userDir.resolve("MO_System").resolve("config").resolve("ai_config.txt"),
                userDir.resolve("MO_System").resolve(".vscode").resolve(".env.local"),
                userDir.resolve("MO_System").resolve(".env.local"),
                userDir.resolve("MO_System").resolve(".env")
        };

        for (Path p : candidates) {
            try {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                merged.putAll(parseDotEnv(Files.readAllLines(p)));
            } catch (Exception ignored) {
                // Keep trying other candidate files.
            }
        }
        return merged;
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
