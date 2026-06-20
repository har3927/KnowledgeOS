package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_path_topics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningPathTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;
}
