package com.studymedical.backend.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "clinical_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalCase {

    @Id
    private String id;

    private String title;

    private String description;

    @Builder.Default
    private List<String> symptoms = new ArrayList<>();

    private String diagnosis;

    @Builder.Default
    private List<CaseQuestion> questions = new ArrayList<>();

    @Indexed
    private UUID topicId;

    @Builder.Default
    private Flashcard.Difficulty difficulty = Flashcard.Difficulty.MEDIUM;

    @Builder.Default
    private boolean aiGenerated = false;

    private String aiModel;

    private String aiSource;

    private String aiEmbeddingsId;

    private Instant createdAt;

    public void initializeDefaults() {
        if (difficulty == null) {
            difficulty = Flashcard.Difficulty.MEDIUM;
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
    public static class CaseQuestion {
        private String question;

        @Builder.Default
        private List<String> options = new ArrayList<>();

        private Integer correctAnswer;

        private String explanation;
    }
}
