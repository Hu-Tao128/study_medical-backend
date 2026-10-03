package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.infrastructure.services.PdfTextExtractor.PdfPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextChunkerTest {

    private final TextChunker chunker = new TextChunker();

    @Test
    void shouldReturnNoChunksWhenThereIsNoText() {
        List<TextChunker.ChunkDraft> chunks = chunker.chunk(
                List.of(new PdfPage(1, "   "), new PdfPage(2, ""))
        );

        assertTrue(chunks.isEmpty());
    }

    @Test
    void shouldCreateSingleChunkForShortPageKeepingPageNumber() {
        List<TextChunker.ChunkDraft> chunks = chunker.chunk(
                List.of(new PdfPage(3, "La homeostasis es el equilibrio interno."))
        );

        assertEquals(1, chunks.size());
        assertEquals(3, chunks.get(0).pageNumber());
        assertEquals(0, chunks.get(0).index());
        assertTrue(chunks.get(0).text().contains("homeostasis"));
    }

    @Test
    void shouldSplitLongTextIntoOverlappingChunks() {
        String longText = String.join(" ", java.util.Collections.nCopies(200, "palabra"));

        List<TextChunker.ChunkDraft> chunks = chunker.chunk(List.of(new PdfPage(1, longText)), 120, 40);

        assertTrue(chunks.size() > 1, "el texto largo debe partirse en varios chunks");
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).index());
            assertTrue(chunks.get(i).text().length() <= 130);
        }
    }

    @Test
    void shouldTrackPageWhereChunkStarts() {
        String pageOne = String.join(" ", java.util.Collections.nCopies(60, "uno"));
        String pageTwo = String.join(" ", java.util.Collections.nCopies(60, "dos"));

        List<TextChunker.ChunkDraft> chunks = chunker.chunk(
                List.of(new PdfPage(1, pageOne), new PdfPage(2, pageTwo)),
                100,
                20
        );

        assertTrue(chunks.size() >= 2);
        assertEquals(1, chunks.get(0).pageNumber());
        assertEquals(2, chunks.get(chunks.size() - 1).pageNumber());
    }
}
