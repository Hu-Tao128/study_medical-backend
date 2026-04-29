package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.search.RateLimitExceededException;
import com.studymedical.backend.application.usecases.search.SearchUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;
import com.studymedical.backend.infrastructure.dto.response.SearchResponse;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import com.studymedical.backend.infrastructure.services.AdaptiveCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private static final List<String> VALID_SOURCES = List.of("all", "nih", "local");
    private static final int MAX_QUERY_LENGTH = 100;

    private final SearchUseCase searchUseCase;
    private final RoleAuthorizationService roleAuthorizationService;
    private final AdaptiveCacheService adaptiveCacheService;

    @GetMapping
    public ResponseEntity<?> search(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String source,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "1") int page) {

        // Validar autenticación
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalido"));
        }

        // Validar parámetros de búsqueda
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El parámetro 'q' es requerido"));
        }

        String trimmedQuery = q.trim();
        if (trimmedQuery.length() > MAX_QUERY_LENGTH) {
            return ResponseEntity.badRequest().body(Map.of("error", "La búsqueda no puede exceder " + MAX_QUERY_LENGTH + " caracteres"));
        }

        // Validar source
        if (!VALID_SOURCES.contains(source)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Source inválido. Valores permitidos: " + VALID_SOURCES));
        }

        limit = Math.min(limit, 20);
        page = Math.max(page, 1);

        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        UUID userId = currentUser.getId();

        SearchQuery query = new SearchQuery();
        query.setQ(trimmedQuery);
        query.setSource(source);
        query.setLimit(limit);
        query.setPage(page);

        // 🔥 Middleware de caching ANTES del use case
        String cacheKey = buildCacheKey(query);
        SearchResponse cached = adaptiveCacheService.get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for query: {}", trimmedQuery);
            return ResponseEntity.ok(cached);
        }

        try {
            SearchResponse response = searchUseCase.execute(query, userId);
            return ResponseEntity.ok(response);
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).body(Map.of("error", "Demasiadas solicitudes, intenta en unos segundos"));
        } catch (Exception e) {
            log.error("Search failed | q={} | userId={} | source={} | page={}", trimmedQuery, userId, source, page, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    private String buildCacheKey(SearchQuery query) {
        return String.format("%s:%s:%d:%d",
                query.getQ().toLowerCase(),
                query.getSanitizedSource(),
                query.getLimit(),
                query.getPage());
    }
}
