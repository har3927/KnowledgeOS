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
public class KnowledgeGraphService {

    private final TopicRepository topicRepository;
    private final TopicPrerequisiteRepository prerequisiteRepository;
    private final UserProgressRepository progressRepository;
    private final UserContextService userContext;

    public Dtos.GraphDto getGraph() {
        List<Topic> topics = topicRepository.findAll();
        Long userId = userContext.getCurrentUserId();
        Map<Long, String> statusMap = progressRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(p -> p.getTopic().getId(), p -> p.getStatus().name()));

        List<Dtos.GraphNodeDto> nodes = topics.stream().map(t ->
                Dtos.GraphNodeDto.builder()
                        .id(String.valueOf(t.getId()))
                        .label(t.getTitle())
                        .category(t.getCategory().getName())
                        .difficulty(t.getDifficulty().name())
                        .status(statusMap.getOrDefault(t.getId(), "NOT_STARTED"))
                        .build()
        ).toList();

        List<Dtos.GraphEdgeDto> edges = prerequisiteRepository.findAllWithTopics().stream().map(tp ->
                Dtos.GraphEdgeDto.builder()
                        .id("e" + tp.getId())
                        .source(String.valueOf(tp.getPrerequisiteTopic().getId()))
                        .target(String.valueOf(tp.getTopic().getId()))
                        .build()
        ).toList();

        return Dtos.GraphDto.builder().nodes(nodes).edges(edges).build();
    }
}
