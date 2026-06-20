package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "generated_by_ai", nullable = false)
    private Boolean generatedByAi;

    @PrePersist
    void prePersist() {
        if (generatedByAi == null) generatedByAi = false;
    }
}
