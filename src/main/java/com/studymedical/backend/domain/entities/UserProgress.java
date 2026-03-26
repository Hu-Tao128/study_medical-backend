package com.studymedical.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_progress_user_topic", columnNames = {"user_id", "topic_id"})
        },
        indexes = {
                @Index(name = "idx_progress_user", columnList = "user_id"),
                @Index(name = "idx_progress_topic", columnList = "topic_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_progress_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_progress_topic"))
    private Topic topic;

    private Double accuracy;

    private Integer attempts;

    @Column(name = "last_score")
    private Double lastScore;

    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;

    @PrePersist
    protected void onCreate() {
        if (attempts == null) {
            attempts = 0;
        }
        if (accuracy == null) {
            accuracy = 0.0;
        }
        if (lastScore == null) {
            lastScore = 0.0;
        }
    }
}
