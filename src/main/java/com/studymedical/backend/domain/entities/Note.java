package com.studymedical.backend.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "notes")
@CompoundIndex(name = "idx_notes_user_updated", def = "{'userId': 1, 'updatedAt': -1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    private String id;

    @Indexed
    private UUID userId;

    private String title;

    private String contentMd;

    @Indexed
    private UUID topicId;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private String aiSummary;

    private String aiEmbeddingsId;

    @Builder.Default
    private boolean isFavorite = false;

    @Builder.Default
    private boolean isArchived = false;

    @Builder.Default
    private boolean aiGenerated = false;

    private String aiModel;

    private String aiSource;

    private Instant createdAt;

    private Instant updatedAt;

    public void initializeOnCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    public void initializeOnUpdate() {
        updatedAt = Instant.now();
    }
}
