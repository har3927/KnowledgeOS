package com.knowledgeos.backend.ai;

import java.util.List;

public interface AiProvider {

    String generateExplanation(String topicTitle, String topicContent);

    List<QuizQuestionData> generateQuiz(String topicTitle, String topicContent, int questionCount);

    String recommendNextTopic(List<String> candidateTopics, List<String> completedTopics);

    String generateLearningPath(String goal, String level, List<String> availableTopics);

    String answerQuestion(String question, String context);

    String summarizeTopic(String topicTitle, String topicContent);

    record QuizQuestionData(String question, List<String> options, String answer) {}
}
