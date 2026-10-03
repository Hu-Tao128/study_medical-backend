package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.infrastructure.services.PdfTextExtractor.PdfPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Divide el texto de un documento en fragmentos (chunks) con solapamiento.
 *
 * <p>Cada chunk conserva la página donde inicia, útil para citar la fuente.
 * El chunking es agnóstico al proveedor de embeddings.</p>
 */
@Service
public class TextChunker {

    public static final int DEFAULT_MAX_CHARS = 1200;
    public static final int DEFAULT_OVERLAP_CHARS = 200;

    public List<ChunkDraft> chunk(List<PdfPage> pages) {
        return chunk(pages, DEFAULT_MAX_CHARS, DEFAULT_OVERLAP_CHARS);
    }

    public List<ChunkDraft> chunk(List<PdfPage> pages, int maxChars, int overlapChars) {
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars debe ser > 0");
        }

        // Aproximación: ~6 caracteres por palabra en español.
        int overlapWords = Math.max(0, overlapChars) / 6;

        List<Word> words = new ArrayList<>();
        for (PdfPage page : pages) {
            if (page == null || page.text() == null) {
                continue;
            }
            String normalized = page.text().replaceAll("\\s+", " ").trim();
            if (normalized.isEmpty()) {
                continue;
            }
            for (String token : normalized.split(" ")) {
                if (!token.isEmpty()) {
                    words.add(new Word(token, page.pageNumber()));
                }
            }
        }

        List<ChunkDraft> chunks = new ArrayList<>();
        int index = 0;
        int start = 0;

        while (start < words.size()) {
            int length = 0;
            int end = start;
            while (end < words.size()) {
                int wordLength = words.get(end).text().length();
                int extra = (end > start ? 1 : 0) + wordLength;
                if (end > start && length + extra > maxChars) {
                    break;
                }
                length += extra;
                end++;
            }

            StringBuilder builder = new StringBuilder(length);
            for (int k = start; k < end; k++) {
                if (k > start) {
                    builder.append(' ');
                }
                builder.append(words.get(k).text());
            }

            chunks.add(new ChunkDraft(builder.toString(), words.get(start).page(), index++));

            if (end >= words.size()) {
                break;
            }
            start = Math.max(start + 1, end - overlapWords);
        }

        return chunks;
    }

    private record Word(String text, int page) {
    }

    public record ChunkDraft(String text, int pageNumber, int index) {
    }
}
