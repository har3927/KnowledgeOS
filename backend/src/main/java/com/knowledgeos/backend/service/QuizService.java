package com.knowledgeos.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgeos.backend.ai.AiProvider;
import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import com.knowledgeos.backend.exception.ResourceNotFoundException;
import com.knowledgeos.backend.mapper.EntityMapper;
import com.knowledgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final TopicService topicService;
    private final UserContextService userContext;
    private final AiProvider aiProvider;
    private final EntityMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public Dtos.QuizDto generateQuiz(Long topicId) {
        Topic topic = topicService.findTopic(topicId);
        List<AiProvider.QuizQuestionData> questions = aiProvider.generateQuiz(
                topic.getTitle(), topic.getContent(), 5);

        Quiz quiz = Quiz.builder()
                .topic(topic)
                .title("Quiz: " + topic.getTitle())
                .generatedByAi(true)
                .build();
        quiz = quizRepository.save(quiz);

        for (AiProvider.QuizQuestionData q : questions) {
            try {
                QuizQuestion question = QuizQuestion.builder()
                        .quiz(quiz)
                        .question(q.question())
                        .optionsJson(objectMapper.writeValueAsString(q.options()))
                        .answer(q.answer())
                        .build();
                questionRepository.save(question);
            } catch (Exception e) {
                // skip malformed question
            }
        }

        List<QuizQuestion> saved = questionRepository.findByQuizId(quiz.getId());
        return mapper.toQuizDto(quiz, saved, false);
    }

    public Dtos.QuizDto getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
        List<QuizQuestion> questions = questionRepository.findByQuizId(quizId);
        return mapper.toQuizDto(quiz, questions, false);
    }

    @Transactional
    public Dtos.QuizResultDto submitQuiz(Long quizId, Dtos.QuizSubmitRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
        List<QuizQuestion> questions = questionRepository.findByQuizId(quizId);

        long correct = 0;
        for (Dtos.QuizAnswerDto answer : request.getAnswers()) {
            boolean isCorrect = questions.stream()
                    .anyMatch(q -> q.getId().equals(answer.getQuestionId())
                            && q.getAnswer().equalsIgnoreCase(answer.getAnswer()));
            if (isCorrect) correct++;
        }

        double score = questions.isEmpty() ? 0 : (correct * 100.0 / questions.size());
        QuizAttempt attempt = QuizAttempt.builder()
                .user(userContext.getCurrentUser())
                .quiz(quiz)
                .score(score)
                .build();
        attempt = attemptRepository.save(attempt);

        return Dtos.QuizResultDto.builder()
                .attemptId(attempt.getId())
                .score(score)
                .correctCount(correct)
                .totalQuestions(questions.size())
                .build();
    }
}
