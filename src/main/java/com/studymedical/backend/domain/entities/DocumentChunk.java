package com.studymedical.backend.domain.entities;

import java.util.UUID;

/**
 * Fragmento de un documento con su página y (opcionalmente) la similitud
 * respecto a una consulta.
 */
public record DocumentChunk(
        UUID id,
        UUID documentId,
        int chunkIndex,
        Integer pageNumber,
        String text,
        double similarity
) {
}
