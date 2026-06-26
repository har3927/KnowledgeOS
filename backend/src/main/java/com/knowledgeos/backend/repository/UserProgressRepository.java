package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.ProgressStatus;
import com.knowledgeos.backend.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndTopicId(Long userId, Long topicId);

    List<UserProgress> findByUserId(Long userId);

    List<UserProgress> findByUserIdAndStatus(Long userId, ProgressStatus status);

    long countByUserIdAndStatus(Long userId, ProgressStatus status);

    @Query("SELECT up FROM UserProgress up JOIN FETCH up.topic t JOIN FETCH t.category WHERE up.user.id = :userId")
    List<UserProgress> findByUserIdWithTopic(Long userId);
}
