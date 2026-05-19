package TA_Job_Application_Module;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DoubaoAIService {
    // 正确的火山引擎文本对话API端点
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    
    private String apiKey;

    public DoubaoAIService(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String analyzeJobMatch(String jobTitle, String jobDescription, String courseCode,
                                  String department, String requiredSkills,
                                  String userSkills, String userGPA, String userExperience) throws Exception {

        String prompt = buildMatchAnalysisPrompt(jobTitle, jobDescription, courseCode,
                department, requiredSkills, userSkills, userGPA, userExperience);

        return chatWithLimit(prompt);
    }

    public String chatWithLimit(String userMessage) throws Exception {
        URL url = URI.create(API_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);

        String requestBody = String.format(
                "{\n" +
                "    \"model\": \"doubao-seed-1-8-251228\",\n" +
                "    \"max_tokens\": 2000,\n" +
                "    \"messages\": [\n" +
                "        {\"role\": \"user\", \"content\": %s}\n" +
                "    ]\n" +
                "}",
                escapeJson(userMessage)
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode == 401) {
            throw new Exception("API密钥无效，请检查配置。");
        } else if (responseCode == 429) {
            throw new Exception("请求频率受限，请稍后再试。");
        } else if (responseCode != 200) {
            throw new Exception("API请求失败，错误码：" + responseCode + "，响应：" + responseBody);
        }

        return parseChatResponse(responseBody);
    }

    private String buildMatchAnalysisPrompt(String jobTitle, String jobDescription, String courseCode,
                                           String department, String requiredSkills,
                                           String userSkills, String userGPA, String userExperience) {
        return String.format(
                "You are a TA matching expert. Analyze the match between applicant and position. " +
                "Respond ONLY in this exact JSON format (no other text):\n" +
                "{\"matchScore\": 75, \"skillsMatch\": 80, \"gpaMatch\": 70, \"experienceMatch\": 60, " +
                "\"summary\": \"The applicant has a moderate match with the position based on skill alignment, GPA, and relevant experience.\", " +
                "\"strengths\": [\"Strong proficiency in Python with 3 years of hands-on experience in data structures and algorithms\", \"Excellent academic performance with a 3.8 GPA in computer science coursework\", \"Relevant teaching experience as a peer tutor for introductory programming courses\"], " +
                "\"weaknesses\": [\"Limited exposure to specialized frameworks required by the course curriculum\", \"No prior formal teaching experience in a university setting\"], " +
                "\"recommendations\": [\"Consider taking additional courses in the specific frameworks used in this course to strengthen your application\", \"Gain some teaching experience by applying for teaching assistant positions in introductory courses first\", \"Build a portfolio of relevant projects to demonstrate practical application of your skills in this subject area\"]}\n\n" +
                "Position: %s (%s) - %s\n" +
                "Requirements: %s\n" +
                "Applicant Skills: %s | GPA: %s | Experience: %s\n\n" +
                "Rules:\n" +
                "- matchScore = weighted average (Skills 40%%, GPA 25%%, Experience 25%%, Other 10%%)\n" +
                "- Focus on explicit skill/keyword matches between requirements and user skills\n" +
                "- summary: Write 2-3 sentences explaining the overall match conclusion\n" +
                "- strengths: Write each as a FULL COMPLETE SENTENCE that describes one specific strength, minimum 15 words per sentence\n" +
                "- weaknesses: Write each as a FULL COMPLETE SENTENCE that describes one specific weakness, minimum 12 words per sentence\n" +
                "- recommendations: Write each as a FULL COMPLETE SENTENCE starting with an action verb, minimum 15 words per sentence\n" +
                "- IMPORTANT: Every item in strengths/weaknesses/recommendations MUST end with a period and be a grammatically complete sentence\n" +
                "- Return ONLY the JSON, no markdown or explanation",
                nullToEmpty(jobTitle),
                nullToEmpty(courseCode),
                nullToEmpty(department),
                nullToEmpty(requiredSkills),
                nullToEmpty(userSkills),
                nullToEmpty(userGPA),
                nullToEmpty(userExperience)
        );
    }

    private String nullToEmpty(String s) {
        return s == null || s.trim().isEmpty() ? "未提供" : s;
    }

    public String chat(String userMessage) throws Exception {
        URL url = URI.create(API_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        // 使用正确的Chat Completions API格式
        String requestBody = String.format(
                "{\n" +
                "    \"model\": \"doubao-seed-1-8-251228\",\n" +
                "    \"messages\": [\n" +
                "        {\"role\": \"user\", \"content\": %s}\n" +
                "    ]\n" +
                "}",
                escapeJson(userMessage)
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        String responseBody = readResponse(conn);

        if (responseCode == 401) {
            throw new Exception("API密钥无效，请检查配置。");
        } else if (responseCode == 429) {
            throw new Exception("请求频率受限，请稍后再试。");
        } else if (responseCode != 200) {
            throw new Exception("API请求失败，错误码：" + responseCode + "，响应：" + responseBody);
        }

        return parseChatResponse(responseBody);
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        java.io.InputStream inputStream = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        if (inputStream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "\"\"";
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private String parseChatResponse(String jsonResponse) {
        // 如果返回的是HTML而不是JSON，说明API调用失败
        if (jsonResponse.trim().startsWith("<")) {
            return "API请求失败：返回了HTML页面，可能端点或API Key有误。\n\n" +
                   "请检查：\n" +
                   "1. API Key是否正确\n" +
                   "2. 模型名称是否有效\n" +
                   "3. API配额是否充足\n\n" +
                   "原始响应(前500字符)：\n" + jsonResponse.substring(0, Math.min(500, jsonResponse.length()));
        }

        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(jsonResponse).getAsJsonObject();

            // 检查错误
            if (root.has("error")) {
                com.google.gson.JsonObject error = root.getAsJsonObject("error");
                if (error.has("message")) {
                    return "API错误: " + error.get("message").getAsString();
                }
                return "API返回错误";
            }

            // 标准Chat Completions格式：choices[0].message.content
            if (root.has("choices")) {
                com.google.gson.JsonArray choices = root.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    com.google.gson.JsonObject choice = choices.get(0).getAsJsonObject();
                    if (choice.has("message")) {
                        com.google.gson.JsonObject message = choice.getAsJsonObject("message");
                        if (message.has("content")) {
                            return message.get("content").getAsString();
                        }
                    }
                }
            }

        } catch (Exception e) {
            return "解析响应失败：" + e.getMessage() + "\n原始响应(前1000字符)：\n" +
                   jsonResponse.substring(0, Math.min(1000, jsonResponse.length()));
        }
        return "未获取到有效响应\n原始响应(前500字符)：\n" + jsonResponse.substring(0, Math.min(500, jsonResponse.length()));
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("your_api_key_here");
    }
}
