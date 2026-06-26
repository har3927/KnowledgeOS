package com.knowledgeos.backend.service;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import com.knowledgeos.backend.exception.ResourceNotFoundException;
import com.knowledgeos.backend.mapper.EntityMapper;
import com.knowledgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningService {

    private final UserProgressRepository progressRepository;
    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final RevisionScheduleRepository revisionRepository;
    private final DailyRecommendationRepository recommendationRepository;
    private final UserContextService userContext;
    private final EntityMapper mapper;
    private final SpacedRepetitionService spacedRepetitionService;
    private final RecommendationService recommendationService;

    @Transactional
    public Dtos.ProgressDto startTopic(Long topicId) {
        User user = userContext.getCurrentUser();
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        UserProgress progress = progressRepository.findByUserIdAndTopicId(user.getId(), topicId)
                .orElse(UserProgress.builder().user(user).topic(topic).build());

        progress.setStatus(ProgressStatus.IN_PROGRESS);
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
        }
        return mapper.toProgressDto(progressRepository.save(progress));
    }

    @Transactional
    public Dtos.ProgressDto completeTopic(Long topicId, Dtos.TopicCompleteRequest request) {
        User user = userContext.getCurrentUser();
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        UserProgress progress = progressRepository.findByUserIdAndTopicId(user.getId(), topicId)
                .orElse(UserProgress.builder().user(user).topic(topic).build());

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedAt(Instant.now());
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
        }

        if (request != null) {
            progress.setWarmUpText(request.getWarmUpText());
            progress.setQuizScore(request.getQuizScore());
            progress.setFeynmanSubmission(request.getFeynmanSubmission());
            progress.setFeynmanScore(request.getFeynmanScore());
            progress.setFeynmanFeedback(request.getFeynmanFeedback());
            progress.setScore(request.getFeynmanScore() != null ? request.getFeynmanScore() : request.getQuizScore());
        }

        UserProgress saved = progressRepository.save(progress);
        spacedRepetitionService.scheduleRevisions(user, topic);

        return mapper.toProgressDto(saved);
    }

    public List<Dtos.ProgressDto> getProgress() {
        return progressRepository.findByUserIdWithTopic(userContext.getCurrentUserId()).stream()
                .map(mapper::toProgressDto)
                .toList();
    }

    public Dtos.DashboardDto getDashboard() {
        Long userId = userContext.getCurrentUserId();
        long completed = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.COMPLETED);
        long inProgress = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS);
        long revisionsDue = revisionRepository.findDueToday(userId, LocalDate.now()).size();

        Dtos.RecommendationDto recommendation = recommendationService.getTodayRecommendation(userId);

        List<Dtos.RevisionDto> dueRevisions = revisionRepository.findDueToday(userId, LocalDate.now()).stream()
                .map(mapper::toRevisionDto)
                .toList();

        List<Dtos.ProgressDto> continueLearning = progressRepository
                .findByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS).stream()
                .map(mapper::toProgressDto)
                .limit(5)
                .toList();

        List<Dtos.CategoryProgressDto> categoryProgress = buildCategoryProgress(userId);
        List<Dtos.WeeklyActivityDto> weeklyActivity = buildWeeklyActivity(userId);

        return Dtos.DashboardDto.builder()
                .topicsCompleted(completed)
                .topicsInProgress(inProgress)
                .revisionsDue(revisionsDue)
                .currentStreak(calculateStreak(userId))
                .todayRecommendation(recommendation)
                .dueRevisions(dueRevisions)
                .continueLearning(continueLearning)
                .categoryProgress(categoryProgress)
                .weeklyActivity(weeklyActivity)
                .build();
    }

    public Dtos.ProgressSummaryDto getProgressSummary() {
        Long userId = userContext.getCurrentUserId();
        long total = topicRepository.count();
        long completed = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.COMPLETED);
        long inProgress = progressRepository.countByUserIdAndStatus(userId, ProgressStatus.IN_PROGRESS);

        return Dtos.ProgressSummaryDto.builder()
                .completionPercentage(total > 0 ? (completed * 100.0 / total) : 0)
                .totalTopics(total)
                .completedTopics(completed)
                .inProgressTopics(inProgress)
                .currentStreak(calculateStreak(userId))
                .categoryBreakdown(buildCategoryProgress(userId))
                .quizPerformance(List.of())
                .build();
    }

    private List<Dtos.CategoryProgressDto> buildCategoryProgress(Long userId) {
        Map<Long, Long> completedByCategory = progressRepository.findByUserIdWithTopic(userId).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .collect(Collectors.groupingBy(p -> p.getTopic().getCategory().getId(), Collectors.counting()));

        return categoryRepository.findAll().stream().map(cat -> {
            long total = topicRepository.findByCategoryId(cat.getId()).size();
            long done = completedByCategory.getOrDefault(cat.getId(), 0L);
            return Dtos.CategoryProgressDto.builder()
                    .categoryName(cat.getName())
                    .completed(done)
                    .total(total)
                    .percentage(total > 0 ? done * 100.0 / total : 0)
                    .build();
        }).toList();
    }

    private List<Dtos.WeeklyActivityDto> buildWeeklyActivity(Long userId) {
        List<UserProgress> all = progressRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        List<Dtos.WeeklyActivityDto> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = all.stream()
                    .filter(p -> p.getCompletedAt() != null)
                    .filter(p -> p.getCompletedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(day))
                    .count();
            result.add(Dtos.WeeklyActivityDto.builder()
                    .day(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .topicsCompleted(count)
                    .build());
        }
        return result;
    }

    private long calculateStreak(Long userId) {
        List<UserProgress> completed = progressRepository.findByUserId(userId).stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED && p.getCompletedAt() != null)
                .sorted(Comparator.comparing(UserProgress::getCompletedAt).reversed())
                .toList();

        if (completed.isEmpty()) return 0;

        Set<LocalDate> completionDates = completed.stream()
                .map(p -> p.getCompletedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());

        long streak = 0;
        LocalDate date = LocalDate.now();
        while (completionDates.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }
}
