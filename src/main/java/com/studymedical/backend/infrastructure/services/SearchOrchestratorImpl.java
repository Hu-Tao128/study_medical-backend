package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.MedicalSearchResult;
import com.studymedical.backend.domain.services.SearchOrchestrationResult;
import com.studymedical.backend.domain.services.SearchOrchestrator;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchOrchestratorImpl implements SearchOrchestrator {

    private final NihSearchExecutor nihSearchExecutor;
    private final LocalSearchExecutor localSearchExecutor;

    @Override
    public SearchOrchestrationResult execute(SearchQuery query, UUID userId) {
        List<MedicalSearchResult> nihResults = new ArrayList<>();
        List<MedicalSearchResult> localResults = new ArrayList<>();

        try {
            if ("all".equalsIgnoreCase(query.getSanitizedSource()) ||
                query.getSanitizedSource().contains("nih")) {
                nihResults = nihSearchExecutor.search(query, userId);
            }
        } catch (Exception e) {
            log.warn("NIH search failed, continuing with local results", e);
        }

        try {
            if ("all".equalsIgnoreCase(query.getSanitizedSource()) ||
                "local".equalsIgnoreCase(query.getSanitizedSource())) {
                localResults = localSearchExecutor.search(query);
            }
        } catch (Exception e) {
            log.warn("Local search failed", e);
        }

        return new SearchOrchestrationResult(nihResults, localResults);
    }
}
