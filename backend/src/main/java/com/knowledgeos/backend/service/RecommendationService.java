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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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
                    .reason(parseRecommendation(rec.getReason()).reason())
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

        ParsedRecommendation parsed = parseRecommendation(aiReason);
        Topic recommended = null;

        if (parsed.topicTitle() != null && !parsed.topicTitle().isBlank()) {
            String titleToFind = parsed.topicTitle().trim().toLowerCase();
            for (Topic t : candidates) {
                if (t.getTitle().trim().toLowerCase().equals(titleToFind)) {
                    recommended = t;
                    break;
                }
            }
        }

        if (recommended == null && aiReason != null) {
            for (Topic t : candidates) {
                if (aiReason.toLowerCase().contains(t.getTitle().toLowerCase())) {
                    recommended = t;
                    break;
                }
            }
        }

        if (recommended == null) {
            recommended = candidates.getFirst();
        }

        return Dtos.RecommendationDto.builder()
                .topicId(recommended.getId())
                .topicTitle(recommended.getTitle())
                .categoryName(recommended.getCategory().getName())
                .difficulty(recommended.getDifficulty().name())
                .reason(parsed.reason())
                .build();
    }

    private record ParsedRecommendation(String topicTitle, String reason) {}

    private ParsedRecommendation parseRecommendation(String rawReason) {
        if (rawReason == null || rawReason.isBlank()) {
            return new ParsedRecommendation(null, "Next available topic based on prerequisites");
        }
        
        String clean = rawReason.trim();
        int braceStart = clean.indexOf('{');
        int braceEnd = clean.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            String jsonPart = clean.substring(braceStart, braceEnd + 1);
            try {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(jsonPart);
                String topic = node.has("topic") ? node.get("topic").asText() : null;
                String reason = null;
                if (node.has("reason")) {
                    reason = node.get("reason").asText();
                } else if (node.has("explanation")) {
                    reason = node.get("explanation").asText();
                }
                if (reason != null && !reason.isBlank()) {
                    return new ParsedRecommendation(topic, reason);
                }
            } catch (Exception e) {
                log.warn("Failed to parse recommendation JSON: {}", jsonPart, e);
            }
        }
        
        // Fallback: remove code fences
        String cleanedReason = rawReason.replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();
        
        return new ParsedRecommendation(null, cleanedReason);
    }
}
