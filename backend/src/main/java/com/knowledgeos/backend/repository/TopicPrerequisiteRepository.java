package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.TopicPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TopicPrerequisiteRepository extends JpaRepository<TopicPrerequisite, Long> {

    @Query("SELECT tp FROM TopicPrerequisite tp JOIN FETCH tp.prerequisiteTopic WHERE tp.topic.id = :topicId")
    List<TopicPrerequisite> findByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT tp FROM TopicPrerequisite tp JOIN FETCH tp.topic JOIN FETCH tp.prerequisiteTopic")
    List<TopicPrerequisite> findAllWithTopics();
}
