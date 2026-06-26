package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status;

    private Double score;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "warm_up_text", columnDefinition = "TEXT")
    private String warmUpText;

    @Column(name = "quiz_score")
    private Double quizScore;

    @Column(name = "feynman_submission", columnDefinition = "TEXT")
    private String feynmanSubmission;

    @Column(name = "feynman_score")
    private Double feynmanScore;

    @Column(name = "feynman_feedback", columnDefinition = "TEXT")
    private String feynmanFeedback;

    @PrePersist
    void prePersist() {
        if (status == null) status = ProgressStatus.NOT_STARTED;
    }
}
