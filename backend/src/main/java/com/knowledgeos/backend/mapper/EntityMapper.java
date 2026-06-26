package com.knowledgeos.backend.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EntityMapper {

    private final ObjectMapper objectMapper;

    public Dtos.CategoryDto toCategoryDto(Category c) {
        return Dtos.CategoryDto.builder()
                .id(c.getId()).name(c.getName()).description(c.getDescription())
                .build();
    }

    public Dtos.TopicSummaryDto toTopicSummary(Topic t) {
        return Dtos.TopicSummaryDto.builder()
                .id(t.getId()).title(t.getTitle())
                .difficulty(t.getDifficulty().name())
                .build();
    }

    public Dtos.TopicDto toTopicDto(Topic t, List<Topic> prerequisites, String progressStatus) {
        return Dtos.TopicDto.builder()
                .id(t.getId())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getName())
                .title(t.getTitle())
                .description(t.getDescription())
                .difficulty(t.getDifficulty().name())
                .estimatedMinutes(t.getEstimatedMinutes())
                .content(t.getContent())
                .createdAt(t.getCreatedAt())
                .prerequisites(prerequisites.stream().map(this::toTopicSummary).toList())
                .progressStatus(progressStatus)
                .build();
    }

    public Dtos.TopicDto toTopicDto(Topic t) {
        return toTopicDto(t, List.of(), "NOT_STARTED");
    }

    public Dtos.ProgressDto toProgressDto(UserProgress p) {
        return Dtos.ProgressDto.builder()
                .id(p.getId())
                .topicId(p.getTopic().getId())
                .topicTitle(p.getTopic().getTitle())
                .categoryName(p.getTopic().getCategory().getName())
                .status(p.getStatus().name())
                .score(p.getScore())
                .startedAt(p.getStartedAt())
                .completedAt(p.getCompletedAt())
                .warmUpText(p.getWarmUpText())
                .quizScore(p.getQuizScore())
                .feynmanSubmission(p.getFeynmanSubmission())
                .feynmanScore(p.getFeynmanScore())
                .feynmanFeedback(p.getFeynmanFeedback())
                .build();
    }

    public Dtos.RevisionDto toRevisionDto(RevisionSchedule rs) {
        return Dtos.RevisionDto.builder()
                .id(rs.getId())
                .topicId(rs.getTopic().getId())
                .topicTitle(rs.getTopic().getTitle())
                .nextRevisionDate(rs.getNextRevisionDate().toString())
                .revisionLevel(rs.getRevisionLevel())
                .completed(rs.getCompleted())
                .build();
    }

    public Dtos.QuizDto toQuizDto(Quiz quiz, List<QuizQuestion> questions, boolean includeAnswers) {
        return Dtos.QuizDto.builder()
                .id(quiz.getId())
                .topicId(quiz.getTopic().getId())
                .title(quiz.getTitle())
                .generatedByAi(quiz.getGeneratedByAi())
                .questions(questions.stream().map(q -> toQuizQuestionDto(q, includeAnswers)).toList())
                .build();
    }

    public Dtos.QuizQuestionDto toQuizQuestionDto(QuizQuestion q, boolean includeAnswer) {
        return Dtos.QuizQuestionDto.builder()
                .id(q.getId())
                .question(q.getQuestion())
                .options(parseOptions(q.getOptionsJson()))
                .answer(includeAnswer ? q.getAnswer() : null)
                .build();
    }

    public Dtos.ConversationDto toConversationDto(AiConversation c) {
        return Dtos.ConversationDto.builder()
                .id(c.getId())
                .prompt(c.getPrompt())
                .response(c.getResponse())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private List<String> parseOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
