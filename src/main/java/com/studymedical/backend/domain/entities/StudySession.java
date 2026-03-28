package com.studymedical.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_sessions", indexes = {
        @Index(name = "idx_study_sessions_user", columnList = "user_id"),
        @Index(name = "idx_study_sessions_topic", columnList = "topic_id"),
        @Index(name = "idx_study_sessions_mode", columnList = "mode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_study_sessions_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false, foreignKey = @ForeignKey(name = "fk_study_sessions_topic"))
    private Topic topic;

    @Column(nullable = false)
    private String mode;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    private Double accuracy;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (totalQuestions == null) {
            totalQuestions = 0;
        }
        if (correctAnswers == null) {
            correctAnswers = 0;
        }
        if (accuracy == null) {
            accuracy = 0.0;
        }
    }
}
