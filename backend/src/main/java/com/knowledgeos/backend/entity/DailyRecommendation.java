package com.knowledgeos.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_recommendations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Column(columnDefinition = "TEXT")
    private String reason;
}
