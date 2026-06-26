package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.DailyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyRecommendationRepository extends JpaRepository<DailyRecommendation, Long> {
    Optional<DailyRecommendation> findByUserIdAndRecommendationDate(Long userId, LocalDate date);
}
