package com.studymedical.backend.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    private String id;

    @Indexed
    private UUID topicId;

    @Indexed
    private UUID createdBy;

    private String question;

    private String answer;

    private Difficulty difficulty;

    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    private UUID groupId;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private boolean aiGenerated = false;

    private String aiModel;

    private String aiSource;

    private String aiEmbeddingsId;

    private Instant createdAt;

    public void initializeDefaults() {
        if (difficulty == null) {
            difficulty = Difficulty.MEDIUM;
        }
        if (visibility == null) {
            visibility = Visibility.PRIVATE;
        }
        if (visibility != Visibility.GROUP) {
            groupId = null;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    public enum Visibility {
        PRIVATE,
        GROUP,
        PUBLIC
    }
}
