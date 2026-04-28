package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.MedicalSearchResult;
import com.studymedical.backend.domain.entities.SearchSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAggregationService {

    private final VectorSearchService vectorSearchService;

    // 🔥 MERGE + RANKING con cosine similarity real
    public List<MedicalSearchResult> mergeAndRank(
            List<MedicalSearchResult> nihResults,
            List<MedicalSearchResult> localResults,
            String query,
            String queryEmbedding,
            int limit) {

        List<MedicalSearchResult> all = new ArrayList<>();
        all.addAll(nihResults != null ? nihResults : List.of());
        all.addAll(localResults != null ? localResults : List.of());

        if (all.isEmpty()) return List.of();

        // Calcular relevanceScore para cada resultado
        for (MedicalSearchResult result : all) {
            double score = calculateRelevanceScore(result, query, queryEmbedding);
            result.setRelevanceScore(score);
        }

        // Ordenar por score descendente y limitar
        return all.stream()
                .sorted(Comparator.comparing(MedicalSearchResult::getRelevanceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private double calculateRelevanceScore(MedicalSearchResult result,
                                          String query,
                                          String queryEmbedding) {
        // 🔥 Text similarity usando cosine similarity real si hay embeddings
        double textSimilarity = 0.0;
        
        if (queryEmbedding != null && !queryEmbedding.isEmpty()) {
            // Si el resultado es local y tiene embedding, usar cosine similarity
            if (result.getSource() == SearchSource.LOCAL) {
                // El embedding del resultado ya se calculó en vector search
                textSimilarity = result.getRelevanceScore() != null ? 
                                 result.getRelevanceScore() : 0.0;
            }
        }
        
        // Fallback: similarity básica basada en coincidencia de palabras
        if (textSimilarity == 0.0) {
            textSimilarity = calculateBasicSimilarity(
                result.getTitle() + " " + result.getDescription(), 
                query
            );
        }

        double sourceBoost = getSourceBoost(result.getSource());
        double recencyBoost = getRecencyBoost(result.getPublicationDate());

        return (textSimilarity * 0.6) + (sourceBoost * 0.2) + (recencyBoost * 0.2);
    }

    private double calculateBasicSimilarity(String text, String query) {
        if (text == null || query == null) return 0.0;
        
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        // Contar palabras de la query que aparecen en el texto
        String[] queryWords = lowerQuery.split("\\s+");
        int matches = 0;
        for (String word : queryWords) {
            if (word.length() > 2 && lowerText.contains(word)) {
                matches++;
            }
        }
        
        return queryWords.length > 0 ? (double) matches / queryWords.length : 0.0;
    }

    private double getSourceBoost(SearchSource source) {
        return switch (source) {
            case LOCAL -> 0.2;          // DB local rápida
            case NIH_MEDLINE -> 0.3;    // Definiciones oficiales
            case NIH_PUBMED -> 0.1;     // Artículos (menor boost)
        };
    }

    private double getRecencyBoost(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) return 0.0;
        
        try {
            int year = Integer.parseInt(pubDate.substring(0, 4));
            int currentYear = java.time.Year.now().getValue();
            int age = currentYear - year;
            
            if (age <= 1) return 1.0;
            if (age <= 3) return 0.8;
            if (age <= 5) return 0.6;
            if (age <= 10) return 0.4;
            return 0.2;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
