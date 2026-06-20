package com.knowledgeos.backend.repository;

import com.knowledgeos.backend.entity.Difficulty;
import com.knowledgeos.backend.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query("SELECT t FROM Topic t WHERE " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:difficulty IS NULL OR t.difficulty = :difficulty) AND " +
           "(:searchPattern IS NULL OR LOWER(t.title) LIKE :searchPattern OR " +
           "LOWER(t.description) LIKE :searchPattern)")
    Page<Topic> findWithFilters(@Param("categoryId") Long categoryId,
                                @Param("difficulty") Difficulty difficulty,
                                @Param("searchPattern") String searchPattern,
                                Pageable pageable);

    List<Topic> findByCategoryId(Long categoryId);
}
