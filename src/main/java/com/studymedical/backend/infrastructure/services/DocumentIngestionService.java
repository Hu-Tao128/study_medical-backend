package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.DocumentStatus;
import com.studymedical.backend.domain.entities.UserDocument;
import com.studymedical.backend.domain.services.EmbeddingNotConfiguredException;
import com.studymedical.backend.domain.services.EmbeddingProvider;
import com.studymedical.backend.infrastructure.persistence.UserDocumentRepository;
import com.studymedical.backend.infrastructure.persistence.UserDocumentRepository.ChunkEmbedding;
import com.studymedical.backend.infrastructure.services.PdfTextExtractor.PdfPage;
import com.studymedical.backend.infrastructure.services.TextChunker.ChunkDraft;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ingesta de un PDF: extrae texto, lo divide en chunks, genera embeddings y
 * los persiste asociados al usuario.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final PdfTextExtractor pdfTextExtractor;
    private final TextChunker textChunker;
    private final EmbeddingProvider embeddingProvider;
    private final UserDocumentRepository repository;

    public UserDocument ingest(
            UUID userId,
            String filename,
            String contentType,
            InputStream pdf
    ) {
        if (!embeddingProvider.isConfigured()) {
            throw new EmbeddingNotConfiguredException();
        }

        UUID documentId = UUID.randomUUID();
        repository.insertDocument(documentId, userId, filename, contentType);

        try {
            repository.updateStatus(documentId, DocumentStatus.PROCESSING, 0, null);

            List<PdfPage> pages = pdfTextExtractor.extract(pdf);
            List<ChunkDraft> drafts = textChunker.chunk(pages);
            if (drafts.isEmpty()) {
                throw new IllegalArgumentException("El PDF no contiene texto extraíble");
            }

            List<ChunkEmbedding> rows = new ArrayList<>(drafts.size());
            for (ChunkDraft draft : drafts) {
                rows.add(new ChunkEmbedding(
                        draft.index(),
                        draft.pageNumber(),
                        draft.text(),
                        embeddingProvider.embedToPgVector(draft.text())
                ));
            }

            repository.insertChunks(documentId, rows);
            repository.updateStatus(documentId, DocumentStatus.READY, rows.size(), null);
        } catch (Exception error) {
            log.warn("Fallo procesando documento {}: {}", documentId, error.getMessage());
            repository.updateStatus(documentId, DocumentStatus.FAILED, 0, error.getMessage());
            if (error instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Error procesando el documento", error);
        }

        return repository.findByIdAndUserId(documentId, userId).orElseThrow();
    }
}
