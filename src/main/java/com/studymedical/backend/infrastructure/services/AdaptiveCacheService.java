package com.studymedical.backend.infrastructure.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveCacheService {

    private final ObjectMapper objectMapper;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SearchResponse get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        
        if (Instant.now().isAfter(entry.expiresAt)) {
            cache.remove(key);
            return null;
        }
        return entry.value;
    }

    public void put(String key, SearchResponse value, long ttlMinutes) {
        CacheEntry entry = new CacheEntry(
            value,
            Instant.now().plusSeconds(ttlMinutes * 60)
        );
        cache.put(key, entry);
        // Limpieza periódica de entradas expiradas
        cleanupExpired();
    }

    public boolean isPopularQuery(String query) {
        String lower = query.toLowerCase();
        return lower.contains("diabetes") ||
               lower.contains("hipertension") ||
               lower.contains("asma") ||
               lower.contains("cancer") ||
               lower.contains("alzheimer");
    }

    public long calculateAdaptiveTtl(String query) {
        return isPopularQuery(query) ? 60 : 10; // 60 min o 10 min
    }

    private void cleanupExpired() {
        if (cache.size() % 100 == 0) {
            Instant now = Instant.now();
            cache.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt));
        }
    }

    private static class CacheEntry {
        final SearchResponse value;
        final Instant expiresAt;

        CacheEntry(SearchResponse value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
