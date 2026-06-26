package com.knowledgeos.backend.service;

import com.knowledgeos.backend.ai.AiProvider;
import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.*;
import com.knowledgeos.backend.mapper.EntityMapper;
import com.knowledgeos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;
    private final AiConversationRepository conversationRepository;
    private final TopicRepository topicRepository;
    private final UserContextService userContext;
    private final EntityMapper mapper;

    @Transactional
    public Dtos.TutorResponse askTutor(Dtos.TutorRequest request) {
        String context = "";
        if (request.getTopicId() != null) {
            context = topicRepository.findById(request.getTopicId())
                    .map(t -> t.getTitle() + ": " + t.getContent())
                    .orElse("");
        }
        String answer = aiProvider.answerQuestion(request.getQuestion(), context);

        AiConversation conversation = AiConversation.builder()
                .user(userContext.getCurrentUser())
                .prompt(request.getQuestion())
                .response(answer)
                .build();
        conversation = conversationRepository.save(conversation);

        return Dtos.TutorResponse.builder()
                .answer(answer)
                .conversationId(conversation.getId())
                .build();
    }

    public List<Dtos.ConversationDto> getConversationHistory() {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userContext.getCurrentUserId()).stream()
                .map(mapper::toConversationDto)
                .toList();
    }


    public String summarizeTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new com.knowledgeos.backend.exception.ResourceNotFoundException("Topic not found"));
        return aiProvider.summarizeTopic(topic.getTitle(), topic.getContent());
    }

    public String generateExplanation(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new com.knowledgeos.backend.exception.ResourceNotFoundException("Topic not found"));
        return aiProvider.generateExplanation(topic.getTitle(), topic.getContent());
    }

    public java.util.Map<String, Object> evaluateFeynman(Long topicId, String userExplanation) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new com.knowledgeos.backend.exception.ResourceNotFoundException("Topic not found"));
        
        String feedback = aiProvider.evaluateFeynmanSummary(topic.getTitle(), topic.getContent(), userExplanation);
        
        int score = 70;
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Score:\\s*(\\d+)/100", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(feedback);
            if (matcher.find()) {
                score = Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // fallback
        }
        
        return java.util.Map.of(
            "feedback", feedback,
            "score", score
        );
    }
}
