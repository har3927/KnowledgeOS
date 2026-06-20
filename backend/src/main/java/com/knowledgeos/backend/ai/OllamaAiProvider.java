package com.knowledgeos.backend.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgeos.backend.config.OllamaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class OllamaAiProvider implements AiProvider {

    private static final String SYSTEM_PROMPT = """
            You are KnowledgeOS AI tutor — a helpful, concise learning assistant.
            Give clear educational answers. Use bullet points when summarizing.
            For quiz requests, return ONLY valid JSON with no markdown or extra text.
            """;

    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public String generateExplanation(String topicTitle, String topicContent) {
        return chat("""
            Explain the following topic clearly for a learner. Use headings and examples.
            Topic: %s
            Content: %s
            """.formatted(topicTitle, truncate(topicContent)));
    }

    @Override
    public List<QuizQuestionData> generateQuiz(String topicTitle, String topicContent, int questionCount) {
        String prompt = """
            Generate exactly %d multiple-choice quiz questions for "%s".
            Content: %s
            Return ONLY a JSON array. Each object must have:
            "question" (string), "options" (array of 4 strings), "answer" (exact text of the correct option).
            No markdown, no code fences, no explanation — JSON only.
            """.formatted(questionCount, topicTitle, truncate(topicContent));

        String response = chat(prompt);
        try {
            String json = extractJson(response);
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            List<QuizQuestionData> result = new ArrayList<>();
            for (Map<String, Object> item : raw) {
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) item.get("options");
                result.add(new QuizQuestionData(
                        (String) item.get("question"),
                        options,
                        (String) item.get("answer")
                ));
            }
            if (!result.isEmpty()) {
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama quiz JSON, using fallback", e);
        }
        return fallbackQuiz(topicTitle);
    }

    @Override
    public String recommendNextTopic(List<String> candidateTopics, List<String> completedTopics) {
        return chat("""
            Completed topics: %s
            Candidate next topics: %s
            Recommend the single best next topic and explain why in 2-3 sentences.
            """.formatted(completedTopics, candidateTopics));
    }

    @Override
    public String generateLearningPath(String goal, String level, List<String> availableTopics) {
        return chat("""
            Create a learning path for goal: "%s" at %s level.
            Available topics: %s
            Return an ordered list of topic names with brief rationale for each step.
            """.formatted(goal, level, availableTopics));
    }

    @Override
    public String answerQuestion(String question, String context) {
        return chat("""
            Context: %s
            Question: %s
            """.formatted(context != null && !context.isBlank() ? context : "General learning", question));
    }

    @Override
    public String summarizeTopic(String topicTitle, String topicContent) {
        return chat("""
            Summarize this topic in bullet points for quick revision.
            Topic: %s
            Content: %s
            """.formatted(topicTitle, truncate(topicContent)));
    }

    private String chat(String userPrompt) {
        if (!properties.isEnabled()) {
            return fallbackResponse(userPrompt);
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", properties.getModel());
            body.put("stream", false);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userPrompt)
            ));

            String url = properties.getBaseUrl().replaceAll("/$", "") + "/api/chat";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Ollama API error: {} - {}", response.statusCode(), response.body());
                return fallbackResponse(userPrompt);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("message").path("content").asText("");
            if (content.isBlank()) {
                log.warn("Ollama returned empty content");
                return fallbackResponse(userPrompt);
            }
            return content;
        } catch (Exception e) {
            log.error("Ollama API call failed — is Ollama running? Model pulled?", e);
            return fallbackResponse(userPrompt);
        }
    }

    private String truncate(String content) {
        if (content == null) return "";
        return content.length() > 6000 ? content.substring(0, 6000) + "..." : content;
    }

    private String extractJson(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        start = text.indexOf('{');
        end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return "[" + text.substring(start, end + 1) + "]";
        }
        return text;
    }

    private String fallbackResponse(String prompt) {
        if (prompt.contains("quiz") || prompt.contains("Quiz") || prompt.contains("JSON")) {
            return "[]";
        }
        return "Local AI is unavailable. Ensure Ollama is running and the model is pulled: ollama pull "
                + properties.getModel();
    }

    private List<QuizQuestionData> fallbackQuiz(String topicTitle) {
        return List.of(
                new QuizQuestionData(
                        "What is the primary focus of " + topicTitle + "?",
                        List.of("Core concepts", "Unrelated topics", "Only syntax", "None of the above"),
                        "Core concepts"
                ),
                new QuizQuestionData(
                        "Which approach is recommended when learning " + topicTitle + "?",
                        List.of("Practice with examples", "Memorize only", "Skip fundamentals", "Avoid exercises"),
                        "Practice with examples"
                )
        );
    }
}
