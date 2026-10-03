package com.studymedical.backend.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Documento (PDF) subido por un usuario para el pipeline RAG.
 */
public record UserDocument(
        UUID id,
        UUID userId,
        String filename,
        String contentType,
        DocumentStatus status,
        int chunkCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
