package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
}
