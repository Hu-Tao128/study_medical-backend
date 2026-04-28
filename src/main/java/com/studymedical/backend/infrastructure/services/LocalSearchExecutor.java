package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.MedicalChunk;
import com.studymedical.backend.domain.entities.MedicalSearchResult;
import com.studymedical.backend.domain.repositories.MedicalChunkRepository;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j"
@Service"
@RequiredArgsConstructor"
public class LocalSearchExecutor {

    private final MedicalChunkRepository medicalChunkRepository;
    private final VectorSearchService vectorSearchService;

    public List<MedicalSearchResult> search(SearchQuery query) {
        try {
            // 🔥 Prioridad: Vector search (cosine similarity real)
            List<MedicalSearchResult> results = vectorSearchService.searchByVector(
                generateQueryEmbedding(query.getQ()),
                query.getLimit(),
                query.getOffset()
            ).stream()
                .map(vResult -> {
                    MedicalChunk chunk = new MedicalChunk();
                    chunk.setId(java.util.UUID.fromString(vResult.getId()));
                    chunk.setChunkTitle(vResult.getChunkTitle());
                    chunk.setChunkText(vResult.getChunkText());
                    chunk.setBook(vResult.getBook());
                    chunk.setAuthor(vResult.getAuthor());
                    chunk.setEdition(vResult.getEdition());
                    return SearchResultNormalizer.fromLocal(chunk);
                })
                .collect(Collectors.toList());

            // Si vector search no devuelve nada, fallback a text search
            if (results.isEmpty()) {
                results = medicalChunkRepository.searchByText(
                    query.getQ(), query.getLimit(), query.getOffset()
                ).stream()
                    .map(SearchResultNormalizer::fromLocal)
                    .collect(Collectors.toList());
            }

            return results;
        } catch (Exception e) {
            log.warn("Local search failed for query: {}", query.getQ(), e);
            return List.of();
        }
    }

    // Placeholder para generar embedding de la query
    private String generateQueryEmbedding(String query) {
        // TODO: Integrar con servicio de embeddings (OpenAI, Cohere, etc.)
        // Por ahora retornamos un vector vacío como fallback
        return "[0.0]";
    }
}
