package com.knowledgeos.backend.service;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import com.knowledgeos.backend.exception.ResourceNotFoundException;
import com.knowledgeos.backend.mapper.EntityMapper;
import com.knowledgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicPrerequisiteRepository prerequisiteRepository;
    private final CategoryService categoryService;
    private final UserProgressRepository progressRepository;
    private final UserContextService userContext;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public Dtos.PageResponse<Dtos.TopicDto> getTopics(Long categoryId, String difficulty,
                                                       String search, Pageable pageable) {
        Difficulty diff = difficulty != null && !difficulty.isBlank()
                ? Difficulty.valueOf(difficulty.toUpperCase()) : null;
        String searchPattern = (search != null && !search.isBlank())
                ? "%" + search.toLowerCase() + "%" : null;
        Page<Topic> page = topicRepository.findWithFilters(categoryId, diff, searchPattern, pageable);
        Long userId = userContext.getCurrentUserId();
        Map<Long, String> statusMap = progressRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getTopic().getId(), p -> p.getStatus().name()));

        List<Dtos.TopicDto> content = page.getContent().stream()
                .map(t -> mapper.toTopicDto(t, List.of(), statusMap.getOrDefault(t.getId(), "NOT_STARTED")))
                .toList();

        return Dtos.PageResponse.<Dtos.TopicDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public Dtos.TopicDto getById(Long id) {
        Topic topic = findTopic(id);
        List<Topic> prerequisites = prerequisiteRepository.findByTopicId(id).stream()
                .map(TopicPrerequisite::getPrerequisiteTopic)
                .toList();
        String status = progressRepository.findByUserIdAndTopicId(userContext.getCurrentUserId(), id)
                .map(p -> p.getStatus().name())
                .orElse("NOT_STARTED");
        return mapper.toTopicDto(topic, prerequisites, status);
    }

    @Transactional
    public Dtos.TopicDto create(Dtos.TopicCreateRequest request) {
        Category category = categoryService.findById(request.getCategoryId());
        Topic topic = Topic.builder()
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()))
                .estimatedMinutes(request.getEstimatedMinutes())
                .content(request.getContent())
                .build();
        return mapper.toTopicDto(topicRepository.save(topic));
    }

    @Transactional
    public Dtos.TopicDto update(Long id, Dtos.TopicUpdateRequest request) {
        Topic topic = findTopic(id);
        if (request.getCategoryId() != null) {
            topic.setCategory(categoryService.findById(request.getCategoryId()));
        }
        if (request.getTitle() != null) topic.setTitle(request.getTitle());
        if (request.getDescription() != null) topic.setDescription(request.getDescription());
        if (request.getDifficulty() != null) topic.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        if (request.getEstimatedMinutes() != null) topic.setEstimatedMinutes(request.getEstimatedMinutes());
        if (request.getContent() != null) topic.setContent(request.getContent());
        return mapper.toTopicDto(topicRepository.save(topic));
    }

    @Transactional
    public void delete(Long id) {
        if (!topicRepository.existsById(id)) {
            throw new ResourceNotFoundException("Topic not found: " + id);
        }
        topicRepository.deleteById(id);
    }

    public Topic findTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + id));
    }
}
