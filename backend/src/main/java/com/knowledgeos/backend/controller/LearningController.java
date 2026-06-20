package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.LearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Tag(name = "Learning")
public class LearningController {

    private final LearningService learningService;

    @PostMapping("/topics/{topicId}/start")
    @Operation(summary = "Start learning a topic")
    public Dtos.ProgressDto startTopic(@PathVariable Long topicId) {
        return learningService.startTopic(topicId);
    }

    @PostMapping("/topics/{topicId}/complete")
    @Operation(summary = "Mark topic as complete")
    public Dtos.ProgressDto completeTopic(@PathVariable Long topicId) {
        return learningService.completeTopic(topicId);
    }

    @GetMapping("/progress")
    @Operation(summary = "Get user progress")
    public List<Dtos.ProgressDto> getProgress() {
        return learningService.getProgress();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data")
    public Dtos.DashboardDto getDashboard() {
        return learningService.getDashboard();
    }

    @GetMapping("/progress/summary")
    @Operation(summary = "Get progress summary")
    public Dtos.ProgressSummaryDto getProgressSummary() {
        return learningService.getProgressSummary();
    }
}
