package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "revision_schedule")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevisionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "next_revision_date", nullable = false)
    private LocalDate nextRevisionDate;

    @Column(name = "revision_level", nullable = false)
    private Integer revisionLevel;

    @Column(nullable = false)
    private Boolean completed;

    @PrePersist
    void prePersist() {
        if (revisionLevel == null) revisionLevel = 1;
        if (completed == null) completed = false;
    }
}
