package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.DocumentStatus;
import com.studymedical.backend.domain.entities.UserDocument;
import com.studymedical.backend.domain.services.EmbeddingNotConfiguredException;
import com.studymedical.backend.domain.services.EmbeddingProvider;
import com.studymedical.backend.infrastructure.persistence.UserDocumentRepository;
import com.studymedical.backend.infrastructure.services.PdfTextExtractor.PdfPage;
import com.studymedical.backend.infrastructure.services.TextChunker.ChunkDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private PdfTextExtractor pdfTextExtractor;

    @Mock
    private TextChunker textChunker;

    @Mock
    private EmbeddingProvider embeddingProvider;

    @Mock
    private UserDocumentRepository repository;

    private DocumentIngestionService service;

    @BeforeEach
    void setUp() {
        service = new DocumentIngestionService(
                pdfTextExtractor,
                textChunker,
                embeddingProvider,
                repository
        );
    }

    @Test
    void shouldRejectWhenEmbeddingProviderIsNotConfigured() {
        when(embeddingProvider.isConfigured()).thenReturn(false);

        assertThrows(
                EmbeddingNotConfiguredException.class,
                () -> service.ingest(UUID.randomUUID(), "a.pdf", "application/pdf", InputStream.nullInputStream())
        );

        verify(repository, never()).insertDocument(any(), any(), any(), any());
    }

    @Test
    void shouldIngestDocumentAndMarkItReady() throws IOException {
        UUID userId = UUID.randomUUID();
        when(embeddingProvider.isConfigured()).thenReturn(true);
        when(pdfTextExtractor.extract(any())).thenReturn(List.of(new PdfPage(1, "contenido")));
        when(textChunker.chunk(anyList())).thenReturn(List.of(new ChunkDraft("contenido", 1, 0)));
        when(embeddingProvider.embedToPgVector("contenido")).thenReturn("[0.1,0.2]");

        UserDocument ready = new UserDocument(
                UUID.randomUUID(), userId, "a.pdf", "application/pdf",
                DocumentStatus.READY, 1, null, LocalDateTime.now(), LocalDateTime.now()
        );
        when(repository.findByIdAndUserId(any(UUID.class), eq(userId))).thenReturn(Optional.of(ready));

        UserDocument result = service.ingest(
                userId, "a.pdf", "application/pdf", InputStream.nullInputStream()
        );

        assertEquals(DocumentStatus.READY, result.status());
        verify(repository).insertChunks(any(UUID.class), argThat(chunks -> chunks.size() == 1));
        verify(repository).updateStatus(any(UUID.class), eq(DocumentStatus.READY), eq(1), isNull());
    }

    @Test
    void shouldMarkFailedWhenExtractionFails() throws IOException {
        when(embeddingProvider.isConfigured()).thenReturn(true);
        when(pdfTextExtractor.extract(any())).thenThrow(new IOException("boom"));

        assertThrows(
                IllegalStateException.class,
                () -> service.ingest(UUID.randomUUID(), "a.pdf", "application/pdf", InputStream.nullInputStream())
        );

        verify(repository).updateStatus(
                any(UUID.class), eq(DocumentStatus.FAILED), eq(0), contains("boom")
        );
    }
}
