package com.studymedical.backend.infrastructure.dto.response;

import com.studymedical.backend.domain.entities.Flashcard;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FlashcardResponseDto(
        String id,
        UUID topicId,
        UUID createdBy,
        String question,
        String answer,
        String difficulty,
        String visibility,
        UUID groupId,
        List<String> tags,
        boolean aiGenerated,
        String aiModel,
        String aiSource,
        String aiEmbeddingsId,
        Instant createdAt
) {
    public static FlashcardResponseDto fromEntity(Flashcard flashcard) {
        return new FlashcardResponseDto(
                flashcard.getId(),
                flashcard.getTopicId(),
                flashcard.getCreatedBy(),
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                flashcard.getDifficulty() != null ? flashcard.getDifficulty().name() : "MEDIUM",
                flashcard.getVisibility() != null ? flashcard.getVisibility().name() : "PRIVATE",
                flashcard.getGroupId(),
                flashcard.getTags(),
                flashcard.isAiGenerated(),
                flashcard.getAiModel(),
                flashcard.getAiSource(),
                flashcard.getAiEmbeddingsId(),
                flashcard.getCreatedAt()
        );
    }
}
