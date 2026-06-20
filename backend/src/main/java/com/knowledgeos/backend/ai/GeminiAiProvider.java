package com.knowledgeos.backend.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgeos.backend.config.GeminiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Disabled — use {@link OllamaAiProvider} instead. Kept for optional future cloud fallback. */
@Slf4j
// @Service
@RequiredArgsConstructor
public class GeminiAiProvider implements AiProvider {

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public String generateExplanation(String topicTitle, String topicContent) {
        return callGemini("""
            You are an expert tutor. Explain the following topic clearly and concisely for a learner.
            Topic: %s
            Content: %s
            Provide a structured explanation with key concepts and examples.
            """.formatted(topicTitle, topicContent));
    }

    @Override
    public List<QuizQuestionData> generateQuiz(String topicTitle, String topicContent, int questionCount) {
        String prompt = """
            Generate exactly %d multiple-choice quiz questions for the topic "%s".
            Content: %s
            Return ONLY valid JSON array with objects: {"question":"...","options":["A","B","C","D"],"answer":"exact option text"}
            """.formatted(questionCount, topicTitle, topicContent);

        String response = callGemini(prompt);
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
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse quiz JSON, using fallback", e);
            return fallbackQuiz(topicTitle);
        }
    }

    @Override
    public String recommendNextTopic(List<String> candidateTopics, List<String> completedTopics) {
        return callGemini("""
            Given completed topics: %s
            And candidate next topics: %s
            Recommend the single best next topic and explain why in 2-3 sentences.
            """.formatted(completedTopics, candidateTopics));
    }

    @Override
    public String generateLearningPath(String goal, String level, List<String> availableTopics) {
        return callGemini("""
            Create a learning path for goal: "%s" at %s level.
            Available topics: %s
            Return an ordered list of topic names with brief rationale.
            """.formatted(goal, level, availableTopics));
    }

    @Override
    public String answerQuestion(String question, String context) {
        return callGemini("""
            You are a helpful AI tutor for KnowledgeOS learning platform.
            Context: %s
            Question: %s
            Provide a clear, educational answer.
            """.formatted(context != null ? context : "General learning", question));
    }

    @Override
    public String summarizeTopic(String topicTitle, String topicContent) {
        return callGemini("""
            Summarize the following topic in bullet points for quick revision.
            Topic: %s
            Content: %s
            """.formatted(topicTitle, topicContent));
    }

    private String callGemini(String prompt) {
        if (!properties.isEnabled() || properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.debug("Gemini not configured, returning fallback response");
            return fallbackResponse(prompt);
        }

        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            );

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + properties.getModel() + ":generateContent?key=" + properties.getApiKey();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error: {} - {}", response.statusCode(), response.body());
                return fallbackResponse(prompt);
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText(fallbackResponse(prompt));
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return fallbackResponse(prompt);
        }
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
        if (prompt.contains("quiz") || prompt.contains("Quiz")) {
            return "[]";
        }
        return "AI tutor is not configured. Set GEMINI_API_KEY environment variable to enable AI features. " +
               "Meanwhile, review the topic content and practice with the available quizzes.";
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
