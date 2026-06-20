package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topic_prerequisites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TopicPrerequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_topic_id", nullable = false)
    private Topic prerequisiteTopic;
}
