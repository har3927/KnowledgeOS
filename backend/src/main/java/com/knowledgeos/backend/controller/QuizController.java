package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate/{topicId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a quiz for a topic")
    public Dtos.QuizDto generateQuiz(@PathVariable Long topicId) {
        return quizService.generateQuiz(topicId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID")
    public Dtos.QuizDto getQuiz(@PathVariable Long id) {
        return quizService.getQuiz(id);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit quiz answers")
    public Dtos.QuizResultDto submitQuiz(@PathVariable Long id, @RequestBody Dtos.QuizSubmitRequest request) {
        return quizService.submitQuiz(id, request);
    }
}
