package com.studymedical.backend.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    private String id;

    private String title;

    @Indexed
    private UUID topicId;

    @Indexed
    private UUID createdBy;

    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Builder.Default
    private boolean aiGenerated = false;

    private String aiModel;

    private String aiSource;

    private String aiEmbeddingsId;

    private Instant createdAt;

    public void initializeDefaults() {
        if (visibility == null) {
            visibility = Visibility.PRIVATE;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizQuestion {
        private String question;

        @Builder.Default
        private List<String> options = new ArrayList<>();

        private Integer correctAnswer;

        private String explanation;

        @Builder.Default
        private boolean aiGenerated = false;
    }

    public enum Visibility {
        PRIVATE,
        GROUP,
        PUBLIC
    }
}
