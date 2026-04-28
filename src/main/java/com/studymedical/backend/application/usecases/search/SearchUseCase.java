package com.studymedical.backend.application.usecases.search;

import com.studymedical.backend.domain.entities.MedicalSearchResult;
import com.studymedical.backend.domain.services.SearchOrchestrator;
import com.studymedical.backend.domain.services.SearchOrchestrationResult;
import com.studymedical.backend.domain.services.SearchAggregationService;
import com.studymedical.backend.domain.services.VectorSearchService;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;
import com.studymedical.backend.infrastructure.dto.response.Pagination;
import com.studymedical.backend.infrastructure.dto.response.SearchResponse;
import com.studymedical.backend.infrastructure.dto.response.SearchResults;
import com.studymedical.backend.infrastructure.services.AdaptiveCacheService;
import com.studymedical.backend.infrastructure.services.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchUseCase {

    private final SearchOrchestrator searchOrchestrator;
    private final SearchAggregationService searchAggregationService;
    private final AdaptiveCacheService adaptiveCacheService;
    private final RateLimitService rateLimitService;
    private final VectorSearchService vectorSearchService;

    public SearchResponse execute(SearchQuery query, UUID userId) {

        // 1. Rate Limit Check
        if (!rateLimitService.isAllowed(userId)) {
            throw new RateLimitExceededException("Espera unos segundos...");
        }

        // 2. Cache Check (prioridad 1: Cache)
        String cacheKey = buildCacheKey(query);
        SearchResponse cached = adaptiveCacheService.get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for query: {}", query.getQ());
            return cached;
        }

        // 3. Ejecutar búsqueda según source
        SearchOrchestrationResult orchestrationResult = searchOrchestrator.execute(query, userId);

        List<MedicalSearchResult> nihResults = orchestrationResult.getNihResults();
        List<MedicalSearchResult> localResults = orchestrationResult.getLocalResults();

        // 4. 🔥 MERGE + RANKING (con cosine similarity real)
        String queryEmbedding = generateQueryEmbedding(query.getQ());
        List<MedicalSearchResult> topResults = searchAggregationService.mergeAndRank(
                nihResults,
                localResults,
                query.getQ(),
                queryEmbedding,
                query.getLimit()
        );

        // 5. Construir respuesta
        SearchResponse response = buildResponse(topResults, nihResults, localResults, query);

        // 6. Guardar en cache (TTL adaptativo)
        long ttl = adaptiveCacheService.calculateAdaptiveTtl(query.getQ());
        adaptiveCacheService.put(cacheKey, response, ttl);

        return response;
    }

    private SearchResponse buildResponse(List<MedicalSearchResult> topResults,
                                        List<MedicalSearchResult> nihResults,
                                        List<MedicalSearchResult> localResults,
                                        SearchQuery query) {
        SearchResponse response = new SearchResponse();
        response.setQuery(query.getQ());

        // Sources
        List<String> sources = new java.util.ArrayList<>();
        if ("all".equalsIgnoreCase(query.getSanitizedSource())) {
            sources.addAll(List.of("nih", "local"));
        } else {
            sources.add(query.getSanitizedSource());
        }
        response.setSources(sources);

        // Results
        SearchResults results = new SearchResults();
        results.setTop(topResults);
        results.setNih(nihResults != null ? nihResults : List.of());
        results.setLocal(localResults != null ? localResults : List.of());
        response.setResults(results);

        // Pagination
        Pagination pagination = new Pagination();
        pagination.setPage(query.getPage());
        pagination.setHasMoreNih(nihResults != null && nihResults.size() >= query.getLimit());
        pagination.setHasMoreLocal(localResults != null && localResults.size() >= query.getLimit());
        response.setPagination(pagination);

        return response;
    }

    private String buildCacheKey(SearchQuery query) {
        return String.format("%s:%s:%d:%d",
                query.getQ().toLowerCase(),
                query.getSanitizedSource(),
                query.getLimit(),
                query.getPage());
    }

    // 🔥 Generar embedding de la query para cosine similarity
    private String generateQueryEmbedding(String query) {
        // TODO: Integrar con servicio de embeddings (OpenAI, Cohere, etc.)
        // Por ahora retornamos vacío - el ranking usará fallback
        return "";
    }
}
