package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "Topics")
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    @Operation(summary = "Get topics with filters, pagination, and sorting")
    public Dtos.PageResponse<Dtos.TopicDto> getTopics(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return topicService.getTopics(categoryId, difficulty, search, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get topic by ID")
    public Dtos.TopicDto getById(@PathVariable Long id) {
        return topicService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a topic")
    public Dtos.TopicDto create(@Valid @RequestBody Dtos.TopicCreateRequest request) {
        return topicService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a topic")
    public Dtos.TopicDto update(@PathVariable Long id, @RequestBody Dtos.TopicUpdateRequest request) {
        return topicService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a topic")
    public void delete(@PathVariable Long id) {
        topicService.delete(id);
    }
}
