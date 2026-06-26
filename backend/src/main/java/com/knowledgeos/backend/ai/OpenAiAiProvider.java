package com.knowledgeos.backend.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgeos.backend.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class OpenAiAiProvider implements AiProvider {

    private static final String SYSTEM_PROMPT = """
            You are KnowledgeOS AI tutor — a helpful, concise learning assistant.
            Give clear educational answers. Use bullet points when summarizing.
            For quiz and topic requests, return ONLY valid JSON with no markdown or extra text.
            """;

    private final OpenAiProperties properties;
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
            log.warn("Failed to parse OpenAi quiz JSON, using fallback", e);
        }
        return fallbackQuiz(topicTitle);
    }

    @Override
    public String recommendNextTopic(List<String> candidateTopics, List<String> completedTopics) {
        return chat("""
            Completed topics: %s
            Candidate next topics: %s
            Recommend the single best next topic from the candidate next topics.
            
            Return ONLY a valid JSON object with the following fields:
            - "topic": "Exact title of the recommended topic"
            - "reason": "2-3 sentences explaining why this topic is recommended next"
            
            No markdown wrapper, no backticks, no code fences. Valid JSON object only.
            """.formatted(completedTopics, candidateTopics));
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

    @Override
    public TopicData generateRandomTopic(String categoryHint, List<String> existingTopicTitles) {
        String prompt = """
            Generate a completely new, unique, and interesting learning topic about technology, software engineering, programming, databases, cloud, or system design.
            Category Hint (if any): %s
            Do NOT generate any of the following existing topics: %s
            
            Return ONLY a valid JSON object with the following fields:
            - "title": "Topic Title"
            - "description": "Short 1-2 sentence description"
            - "difficulty": "BEGINNER" or "INTERMEDIATE" or "ADVANCED"
            - "estimatedMinutes": integer representing study time (e.g. 45)
            - "categoryName": must be exactly "Technology"
            - "content": "Detailed markdown explanation of the topic (at least 3-4 paragraphs with headers/examples)"
            
            No markdown wrapper, no backticks, no code fences. Valid JSON object only.
            """.formatted(categoryHint != null ? categoryHint : "any random technical interest", existingTopicTitles);

        String response = chat(prompt);
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, TopicData.class);
        } catch (Exception e) {
            log.error("Failed to parse AI generated random topic: {}", response, e);
            return new TopicData(
                    "Introduction to Cryptography",
                    "Learn the basics of securing information using hashing and encryption.",
                    "Cryptography is the practice and study of techniques for secure communication in the presence of adversarial behavior. It covers symmetric key encryption, public key cryptography, and secure hash algorithms (like SHA-256). These form the backbone of modern internet security, SSL certificates, and digital signatures.",
                    "BEGINNER",
                    30,
                    "Technology"
            );
        }
    }

    private String chat(String userPrompt) {
        if (!properties.isEnabled()) {
            return fallbackResponse(userPrompt);
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", properties.getModel());
            body.put("messages", List.of(
                    Map.of("role", "system", "content", SYSTEM_PROMPT),
                    Map.of("role", "user", "content", userPrompt)
            ));

            String url = properties.getBaseUrl().replaceAll("/$", "") + "/chat/completions";
            
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                builder.header("Authorization", "Bearer " + properties.getApiKey());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenAi API error: {} - {}", response.statusCode(), response.body());
                return fallbackResponse(userPrompt);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                log.warn("OpenAi returned empty content");
                return fallbackResponse(userPrompt);
            }
            return content;
        } catch (Exception e) {
            log.error("OpenAi API call failed", e);
            return fallbackResponse(userPrompt);
        }
    }

    @Override
    public String evaluateFeynmanSummary(String topicTitle, String topicContent, String userExplanation) {
        return chat("""
            You are the AI learning tutor. The user has just studied the topic "%s" and tried to explain it in their own words (Feynman Technique).
            
            Topic Content:
            %s
            
            User's Explanation:
            %s
            
            Evaluate their explanation. Check for accuracy and completeness. 
            Highlight what they explained well, and point out any misconceptions or missing key concepts.
            At the end of your evaluation, output a score on a separate line in the format: "Score: X/100" (e.g. Score: 85/100).
            """.formatted(topicTitle, truncate(topicContent), userExplanation));
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
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String fallbackResponse(String prompt) {
        if (prompt.contains("quiz") || prompt.contains("Quiz") || prompt.contains("JSON")) {
            return "[]";
        }
        return "OpenAI Provider is disabled or unconfigured.";
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
