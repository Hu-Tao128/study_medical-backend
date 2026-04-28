package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.search.RateLimitExceededException;
import com.studymedical.backend.application.usecases.search.SearchUseCase;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;
import com.studymedical.backend.infrastructure.dto.response.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j"
@RestController"
@RequiredArgsConstructor"
public class SearchController {

    private final SearchUseCase searchUseCase;

    @GetMapping("/api/v1/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String source,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "1") int page,
            Authentication auth) {

        // Validar parámetros
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        limit = Math.min(limit, 20); // máximo 20
        page = Math.max(page, 1);

        // Extraer userId del token JWT
        String authId = auth.getName();
        UUID userId = UUID.fromString(authId); // Asumiendo que el sub del JWT es el UUID

        SearchQuery query = new SearchQuery();
        query.setQ(q.trim());
        query.setSource(source);
        query.setLimit(limit);
        query.setPage(page);

        try {
            SearchResponse response = searchUseCase.execute(query, userId);
            return ResponseEntity.ok(response);
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).build();
        } catch (Exception e) {
            log.error("Search failed for query: {}", q, e);
            return ResponseEntity.status(500).build();
        }
    }
}
