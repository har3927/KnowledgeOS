package com.knowledgeos.backend.ai;

import java.util.List;

public interface AiProvider {

    String generateExplanation(String topicTitle, String topicContent);

    List<QuizQuestionData> generateQuiz(String topicTitle, String topicContent, int questionCount);

    String recommendNextTopic(List<String> candidateTopics, List<String> completedTopics);


    String answerQuestion(String question, String context);

    String summarizeTopic(String topicTitle, String topicContent);

    TopicData generateRandomTopic(String categoryHint, List<String> existingTopicTitles);

    String evaluateFeynmanSummary(String topicTitle, String topicContent, String userExplanation);

    record QuizQuestionData(String question, List<String> options, String answer) {}

    record TopicData(String title, String description, String content, String difficulty, int estimatedMinutes, String categoryName) {}
}
