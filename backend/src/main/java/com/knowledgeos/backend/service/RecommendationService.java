package com.knowledgeos.backend.service;

import com.knowledgeos.backend.ai.AiProvider;
import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import com.knowledgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final TopicRepository topicRepository;
    private final TopicPrerequisiteRepository prerequisiteRepository;
    private final UserProgressRepository progressRepository;
    private final DailyRecommendationRepository recommendationRepository;
    private final UserContextService userContext;
    private final AiProvider aiProvider;

    @Transactional
    public Dtos.RecommendationDto getTodayRecommendation(Long userId) {
        Optional<DailyRecommendation> existing = recommendationRepository
                .findByUserIdAndRecommendationDate(userId, LocalDate.now());
        if (existing.isPresent()) {
            DailyRecommendation rec = existing.get();
            Topic topic = rec.getTopic();
            return Dtos.RecommendationDto.builder()
                    .topicId(topic.getId())
                    .topicTitle(topic.getTitle())
                    .categoryName(topic.getCategory().getName())
                    .difficulty(topic.getDifficulty().name())
                    .reason(rec.getReason())
                    .build();
        }

        Dtos.RecommendationDto recommendation = computeRecommendation(userId);
        if (recommendation != null && recommendation.getTopicId() != null) {
            Topic topic = topicRepository.findById(recommendation.getTopicId()).orElse(null);
            if (topic != null) {
                DailyRecommendation rec = DailyRecommendation.builder()
                        .user(userContext.getCurrentUser())
                        .topic(topic)
                        .recommendationDate(LocalDate.now())
                        .reason(recommendation.getReason())
                        .build();
                recommendationRepository.save(rec);
            }
        }
        return recommendation;
    }

    private Dtos.RecommendationDto computeRecommendation(Long userId) {
        Set<Long> completedIds = progressRepository.findByUserId(userId).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .map(p -> p.getTopic().getId())
                .collect(Collectors.toSet());

        Set<Long> inProgressIds = progressRepository.findByUserId(userId).stream()
                .filter(p -> p.getStatus() == ProgressStatus.IN_PROGRESS)
                .map(p -> p.getTopic().getId())
                .collect(Collectors.toSet());

        if (!inProgressIds.isEmpty()) {
            Long topicId = inProgressIds.iterator().next();
            Topic topic = topicRepository.findById(topicId).orElse(null);
            if (topic != null) {
                return Dtos.RecommendationDto.builder()
                        .topicId(topic.getId())
                        .topicTitle(topic.getTitle())
                        .categoryName(topic.getCategory().getName())
                        .difficulty(topic.getDifficulty().name())
                        .reason("Continue your in-progress topic")
                        .build();
            }
        }

        Map<Long, Set<Long>> prereqMap = prerequisiteRepository.findAllWithTopics().stream()
                .collect(Collectors.groupingBy(
                        tp -> tp.getTopic().getId(),
                        Collectors.mapping(tp -> tp.getPrerequisiteTopic().getId(), Collectors.toSet())
                ));

        List<Topic> candidates = topicRepository.findAll().stream()
                .filter(t -> !completedIds.contains(t.getId()))
                .filter(t -> prereqMap.getOrDefault(t.getId(), Set.of()).stream().allMatch(completedIds::contains))
                .toList();

        if (candidates.isEmpty()) {
            return Dtos.RecommendationDto.builder()
                    .reason("All topics completed! Great job!")
                    .build();
        }

        String aiReason = null;
        if (candidates.size() > 1) {
            List<String> candidateTitles = candidates.stream().map(Topic::getTitle).toList();
            List<String> completedTitles = completedIds.stream()
                    .map(id -> topicRepository.findById(id).map(Topic::getTitle).orElse(""))
                    .filter(s -> !s.isBlank())
                    .toList();
            aiReason = aiProvider.recommendNextTopic(candidateTitles, completedTitles);
        }

        Topic recommended = candidates.getFirst();
        return Dtos.RecommendationDto.builder()
                .topicId(recommended.getId())
                .topicTitle(recommended.getTitle())
                .categoryName(recommended.getCategory().getName())
                .difficulty(recommended.getDifficulty().name())
                .reason(aiReason != null ? aiReason : "Next available topic based on prerequisites")
                .build();
    }
}
