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

        return chat(prompt);
    }

    private String buildMatchAnalysisPrompt(String jobTitle, String jobDescription, String courseCode,
                                           String department, String requiredSkills,
                                           String userSkills, String userGPA, String userExperience) {
        return String.format(
                "You are a professional TA (Teaching Assistant) matching analysis expert. Please analyze the match between the TA applicant and the position based on the following information.\n\n" +
                "[Position Information]\n" +
                "- Position Title: %s\n" +
                "- Course Code: %s\n" +
                "- Department: %s\n" +
                "- Job Description: %s\n" +
                "- Required Skills: %s\n\n" +
                "[Applicant Information]\n" +
                "- Skills: %s\n" +
                "- GPA: %s\n" +
                "- Relevant Experience: %s\n\n" +
                "Please analyze from the following aspects and provide a detailed matching report:\n\n" +
                "1. Skills Match (30%%): Evaluate how well the applicant's skills match the position requirements\n" +
                "2. Academic Background (25%%): Evaluate GPA and relevant course background\n" +
                "3. Experience Match (25%%): Evaluate relevant teaching or research experience\n" +
                "4. Overall Recommendation (20%%): Provide comprehensive evaluation and suggestions\n\n" +
                "Please respond in English with a clear format that is easy to read.",
                nullToEmpty(jobTitle),
                nullToEmpty(courseCode),
                nullToEmpty(department),
                nullToEmpty(jobDescription),
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
