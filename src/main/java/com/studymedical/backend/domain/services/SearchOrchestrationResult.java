package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.MedicalSearchResult;

import java.util.List;

public class SearchOrchestrationResult {
    private List<MedicalSearchResult> nihResults;
    private List<MedicalSearchResult> localResults;

    public SearchOrchestrationResult(List<MedicalSearchResult> nihResults,
                                     List<MedicalSearchResult> localResults) {
        this.nihResults = nihResults;
        this.localResults = localResults;
    }

    public List<MedicalSearchResult> getNihResults() { return nihResults; }
    public List<MedicalSearchResult> getLocalResults() { return localResults; }
}
