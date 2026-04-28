package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.MedicalSearchResult;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;
import com.studymedical.backend.infrastructure.external.MedlinePlusClient;
import com.studymedical.backend.infrastructure.external.MedlinePlusClient.MedlineDocument;
import com.studymedical.backend.infrastructure.external.PubMedClient;
import com.studymedical.backend.infrastructure.external.PubMedClient.PubMedArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

@Slf4j"
@Service"
@RequiredArgsConstructor"
public class NihSearchExecutor {

    private final PubMedClient pubMedClient;
    private final MedlinePlusClient medlinePlusClient;
    private final NihRateLimitService nihRateLimitService;

    public List<MedicalSearchResult> search(SearchQuery query, UUID userId) {
        List<MedicalSearchResult> results = new ArrayList<>();

        try {
            // PubMed (artículos) - con efetch para abstract completo
            Callable<List<String>> pubmedSearchTask = () ->
                pubMedClient.search(query.getQ(), query.getLimit(), query.getOffset());

            List<String> ids = nihRateLimitService.executeWithLimit(
                "PubMed Search: " + query.getQ(), pubmedSearchTask);

            if (ids != null && !ids.isEmpty()) {
                Callable<List<PubMedArticle>> fetchTask = () ->
                    pubMedClient.fetchDetails(ids);
                
                List<PubMedArticle> articles = nihRateLimitService.executeWithLimit(
                    "PubMed Fetch: " + ids.size() + " articles", fetchTask);

                if (articles != null) {
                    articles.forEach(article -> results.add(
                        SearchResultNormalizer.fromPubMed(
                            article.getId(),
                            article.getTitle(),
                            article.getAbstractText(),
                            article.getPubDate(),
                            article.getUrl(),
                            article.getAuthors()
                        )
                    ));
                }
            }

            // MedlinePlus (definiciones) - solo para búsqueda general
            if ("all".equalsIgnoreCase(query.getSanitizedSource()) ||
                "nih-medline".equalsIgnoreCase(query.getSanitizedSource())) {
                
                Callable<List<MedlineDocument>> medlineTask = () ->
                    medlinePlusClient.searchHealthTopics(query.getQ());
                
                List<MedlineDocument> docs = nihRateLimitService.executeWithLimit(
                    "MedlinePlus: " + query.getQ(), medlineTask);

                if (docs != null) {
                    docs.forEach(doc -> results.add(
                        SearchResultNormalizer.fromMedlinePlus(
                            doc.getUrl(),
                            doc.getTitle(),
                            doc.getFullSummary(),
                            "definition"
                        )
                    ));
                }
            }

        } catch (NihRateLimitException e) {
            log.warn("NIH rate limit exceeded for query: {}", query.getQ());
            throw e;
        } catch (Exception e) {
            log.warn("NIH search failed for query: {}", query.getQ(), e);
        }

        // Limitar resultados ANTES de devolver
        return results.size() > query.getLimit() ?
               results.subList(0, query.getLimit()) : results;
    }
}
