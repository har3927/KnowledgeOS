package com.knowledgeos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryCreateRequest {
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopicDto {
        private Long id;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String description;
        private String difficulty;
        private Integer estimatedMinutes;
        private String content;
        private Instant createdAt;
        private List<TopicSummaryDto> prerequisites;
        private String progressStatus;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopicSummaryDto {
        private Long id;
        private String title;
        private String difficulty;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopicCreateRequest {
        @NotNull(message = "Category ID is required")
        private Long categoryId;
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        @NotBlank(message = "Difficulty is required")
        private String difficulty;
        @NotNull(message = "Estimated minutes is required")
        private Integer estimatedMinutes;
        private String content;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopicUpdateRequest {
        private Long categoryId;
        private String title;
        private String description;
        private String difficulty;
        private Integer estimatedMinutes;
        private String content;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProgressDto {
        private Long id;
        private Long topicId;
        private String topicTitle;
        private String categoryName;
        private String status;
        private Double score;
        private Instant startedAt;
        private Instant completedAt;

        private String warmUpText;
        private Double quizScore;
        private String feynmanSubmission;
        private Double feynmanScore;
        private String feynmanFeedback;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopicCompleteRequest {
        private String warmUpText;
        private Double quizScore;
        private String feynmanSubmission;
        private Double feynmanScore;
        private String feynmanFeedback;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardDto {
        private long topicsCompleted;
        private long topicsInProgress;
        private long revisionsDue;
        private long currentStreak;
        private RecommendationDto todayRecommendation;
        private List<RevisionDto> dueRevisions;
        private List<ProgressDto> continueLearning;
        private List<CategoryProgressDto> categoryProgress;
        private List<WeeklyActivityDto> weeklyActivity;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryProgressDto {
        private String categoryName;
        private long completed;
        private long total;
        private double percentage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WeeklyActivityDto {
        private String day;
        private long topicsCompleted;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RecommendationDto {
        private Long topicId;
        private String topicTitle;
        private String categoryName;
        private String difficulty;
        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevisionDto {
        private Long id;
        private Long topicId;
        private String topicTitle;
        private String nextRevisionDate;
        private Integer revisionLevel;
        private Boolean completed;
    }


    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizDto {
        private Long id;
        private Long topicId;
        private String title;
        private Boolean generatedByAi;
        private List<QuizQuestionDto> questions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizQuestionDto {
        private Long id;
        private String question;
        private List<String> options;
        private String answer;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizSubmitRequest {
        private List<QuizAnswerDto> answers;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizAnswerDto {
        private Long questionId;
        private String answer;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizResultDto {
        private Long attemptId;
        private Double score;
        private long correctCount;
        private long totalQuestions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TutorRequest {
        private String question;
        private Long topicId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TutorResponse {
        private String answer;
        private Long conversationId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ConversationDto {
        private Long id;
        private String prompt;
        private String response;
        private Instant createdAt;
    }


    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProgressSummaryDto {
        private double completionPercentage;
        private long totalTopics;
        private long completedTopics;
        private long inProgressTopics;
        private long currentStreak;
        private List<CategoryProgressDto> categoryBreakdown;
        private List<QuizPerformanceDto> quizPerformance;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizPerformanceDto {
        private String quizTitle;
        private Double score;
        private String attemptedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PageResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApiError {
        private String message;
        private int status;
        private Instant timestamp;
    }
}
