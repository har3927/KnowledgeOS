package com.knowledgeos.backend.service;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import com.knowledgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository pathRepository;
    private final LearningPathTopicRepository pathTopicRepository;
    private final UserProgressRepository progressRepository;
    private final UserContextService userContext;

    public List<Dtos.LearningPathDto> getAllPaths() {
        Long userId = userContext.getCurrentUserId();
        Map<Long, String> statusMap = progressRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getTopic().getId(), p -> p.getStatus().name()));

        return pathRepository.findAll().stream().map(path -> {
            List<LearningPathTopic> pathTopics = pathTopicRepository.findByLearningPathIdOrderBySequenceNo(path.getId());
            long completed = pathTopics.stream()
                    .filter(pt -> "COMPLETED".equals(statusMap.get(pt.getTopic().getId())))
                    .count();

            List<Dtos.LearningPathTopicDto> topics = pathTopics.stream().map(pt ->
                    Dtos.LearningPathTopicDto.builder()
                            .topicId(pt.getTopic().getId())
                            .title(pt.getTopic().getTitle())
                            .sequenceNo(pt.getSequenceNo())
                            .status(statusMap.getOrDefault(pt.getTopic().getId(), "NOT_STARTED"))
                            .build()
            ).toList();

            return Dtos.LearningPathDto.builder()
                    .id(path.getId())
                    .name(path.getName())
                    .description(path.getDescription())
                    .topics(topics)
                    .progressPercentage(pathTopics.isEmpty() ? 0 : completed * 100.0 / pathTopics.size())
                    .build();
        }).toList();
    }
}
