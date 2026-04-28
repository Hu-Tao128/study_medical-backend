package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.MedicalSearchResult;
import com.studymedical.backend.infrastructure.dto.request.SearchQuery;

import java.util.List;
import java.util.UUID;

public interface SearchOrchestrator {
    SearchOrchestrationResult execute(SearchQuery query, UUID userId);
}
