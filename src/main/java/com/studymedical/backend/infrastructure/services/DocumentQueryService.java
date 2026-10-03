package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.DocumentChunk;
import com.studymedical.backend.domain.entities.DocumentStatus;
import com.studymedical.backend.domain.entities.UserDocument;
import com.studymedical.backend.domain.services.ChatCompletionProvider;
import com.studymedical.backend.domain.services.DocumentPrompts;
import com.studymedical.backend.domain.services.EmbeddingNotConfiguredException;
import com.studymedical.backend.domain.services.EmbeddingProvider;
import com.studymedical.backend.infrastructure.persistence.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Responde preguntas sobre un documento usando RAG: recupera los chunks más
 * similares y deja que el LLM redacte una respuesta citando las páginas.
 */
@Service
@RequiredArgsConstructor
public class DocumentQueryService {

    private final UserDocumentRepository repository;
    private final EmbeddingProvider embeddingProvider;
    private final ChatCompletionProvider chatCompletionProvider;

    public DocumentAnswer answer(UUID userId, UUID documentId, String question, Integer topK) {
        UserDocument document = repository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento no encontrado"
                ));

        if (document.status() != DocumentStatus.READY) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El documento aún no está listo"
            );
        }

        if (!embeddingProvider.isConfigured()) {
            throw new EmbeddingNotConfiguredException();
        }

        int limit = topK == null ? 5 : Math.max(1, Math.min(topK, 10));
        String queryVector = embeddingProvider.embedToPgVector(question);
        List<DocumentChunk> chunks = repository.searchTopK(documentId, queryVector, limit);

        if (chunks.isEmpty()) {
            return new DocumentAnswer(
                    "No encontré información relacionada en este documento.",
                    List.of()
            );
        }

        if (!chatCompletionProvider.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Modelo de chat no configurado"
            );
        }

        String answer = chatCompletionProvider.complete(
                DocumentPrompts.SYSTEM_PROMPT,
                DocumentPrompts.buildUserPrompt(question, chunks)
        );

        List<Citation> citations = chunks.stream()
                .map(chunk -> new Citation(
                        chunk.chunkIndex(),
                        chunk.pageNumber(),
                        chunk.similarity(),
                        snippet(chunk.text())
                ))
                .toList();

        return new DocumentAnswer(answer, citations);
    }

    private static String snippet(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 240 ? text : text.substring(0, 240) + "…";
    }

    public record Citation(int chunkIndex, Integer pageNumber, double similarity, String snippet) {
    }

    public record DocumentAnswer(String answer, List<Citation> citations) {
    }
}
