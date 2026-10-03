package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.DocumentChunk;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentPromptsTest {

    @Test
    void shouldIncludeQuestionContentAndPageNumbers() {
        UUID documentId = UUID.randomUUID();
        List<DocumentChunk> chunks = List.of(
                new DocumentChunk(UUID.randomUUID(), documentId, 0, 3, "Texto de la pagina tres", 0.9),
                new DocumentChunk(UUID.randomUUID(), documentId, 1, 7, "Texto de la pagina siete", 0.8)
        );

        String prompt = DocumentPrompts.buildUserPrompt("¿Qué es la homeostasis?", chunks);

        assertTrue(prompt.contains("¿Qué es la homeostasis?"));
        assertTrue(prompt.contains("página 3"));
        assertTrue(prompt.contains("página 7"));
        assertTrue(prompt.contains("Texto de la pagina tres"));
    }

    @Test
    void shouldHandleChunksWithoutPageNumber() {
        DocumentChunk chunk = new DocumentChunk(
                UUID.randomUUID(), UUID.randomUUID(), 0, null, "texto", 0.5
        );

        String prompt = DocumentPrompts.buildUserPrompt("pregunta", List.of(chunk));

        assertTrue(prompt.contains("página desconocida"));
    }
}
