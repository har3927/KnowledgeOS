package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.RevisionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface RevisionScheduleRepository extends JpaRepository<RevisionSchedule, Long> {

    @Query("SELECT rs FROM RevisionSchedule rs JOIN FETCH rs.topic WHERE rs.user.id = :userId AND rs.nextRevisionDate = :date AND rs.completed = false")
    List<RevisionSchedule> findDueToday(Long userId, LocalDate date);

    @Query("SELECT rs FROM RevisionSchedule rs JOIN FETCH rs.topic WHERE rs.user.id = :userId AND rs.nextRevisionDate > :date AND rs.completed = false ORDER BY rs.nextRevisionDate")
    List<RevisionSchedule> findUpcoming(Long userId, LocalDate date);

    @Query("SELECT rs FROM RevisionSchedule rs JOIN FETCH rs.topic WHERE rs.user.id = :userId AND rs.completed = true ORDER BY rs.nextRevisionDate DESC")
    List<RevisionSchedule> findCompleted(Long userId);

    boolean existsByUserIdAndTopicIdAndRevisionLevel(Long userId, Long topicId, Integer revisionLevel);
}
