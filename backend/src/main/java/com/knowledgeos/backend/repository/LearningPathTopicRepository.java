package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.LearningPathTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LearningPathTopicRepository extends JpaRepository<LearningPathTopic, Long> {

    @Query("SELECT lpt FROM LearningPathTopic lpt JOIN FETCH lpt.topic WHERE lpt.learningPath.id = :pathId ORDER BY lpt.sequenceNo")
    List<LearningPathTopic> findByLearningPathIdOrderBySequenceNo(Long pathId);

    List<LearningPathTopic> findAllByOrderByLearningPathIdAscSequenceNoAsc();
}
