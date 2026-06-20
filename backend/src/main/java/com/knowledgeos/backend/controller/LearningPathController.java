package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.LearningPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
@Tag(name = "Learning Paths")
public class LearningPathController {

    private final LearningPathService learningPathService;

    @GetMapping
    @Operation(summary = "Get all learning paths")
    public List<Dtos.LearningPathDto> getAll() {
        return learningPathService.getAllPaths();
    }
}
