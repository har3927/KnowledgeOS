package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.AiService;
import com.knowledgeos.backend.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI")
public class AiController {

    private final AiService aiService;
    private final QuizService quizService;

    @PostMapping("/tutor")
    @Operation(summary = "Ask the AI tutor")
    public Dtos.TutorResponse askTutor(@RequestBody Dtos.TutorRequest request) {
        return aiService.askTutor(request);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get conversation history")
    public List<Dtos.ConversationDto> getConversations() {
        return aiService.getConversationHistory();
    }


    @GetMapping("/topics/{topicId}/summary")
    @Operation(summary = "Generate topic summary")
    public Map<String, String> summarizeTopic(@PathVariable Long topicId) {
        return Map.of("summary", aiService.summarizeTopic(topicId));
    }

    @GetMapping("/topics/{topicId}/explanation")
    @Operation(summary = "Generate topic explanation")
    public Map<String, String> generateExplanation(@PathVariable Long topicId) {
        return Map.of("explanation", aiService.generateExplanation(topicId));
    }

    @PostMapping("/quiz/{topicId}")
    @Operation(summary = "Generate quiz via AI")
    public Dtos.QuizDto generateQuiz(@PathVariable Long topicId) {
        return quizService.generateQuiz(topicId);
    }

    @PostMapping("/topics/{topicId}/feynman-eval")
    @Operation(summary = "Evaluate user's Feynman explanation")
    public Map<String, Object> evaluateFeynman(@PathVariable Long topicId, @RequestBody Map<String, String> payload) {
        String explanation = payload.get("explanation");
        return aiService.evaluateFeynman(topicId, explanation);
    }
}
