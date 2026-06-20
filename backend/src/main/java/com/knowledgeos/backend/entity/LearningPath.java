package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_paths")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
