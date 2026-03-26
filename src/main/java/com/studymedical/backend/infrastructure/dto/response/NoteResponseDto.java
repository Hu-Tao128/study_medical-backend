package com.studymedical.backend.infrastructure.dto.response;

import com.studymedical.backend.domain.entities.Note;

import java.time.Instant;
import java.util.UUID;

public record NoteResponseDto(
        String id,
        UUID userId,
        String title,
        String contentMd,
        UUID topicId,
        String aiSummary,
        String aiEmbeddingsId,
        boolean aiGenerated,
        String aiModel,
        String aiSource,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoteResponseDto fromEntity(Note note) {
        return new NoteResponseDto(
                note.getId(),
                note.getUserId(),
                note.getTitle(),
                note.getContentMd(),
                note.getTopicId(),
                note.getAiSummary(),
                note.getAiEmbeddingsId(),
                note.isAiGenerated(),
                note.getAiModel(),
                note.getAiSource(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
