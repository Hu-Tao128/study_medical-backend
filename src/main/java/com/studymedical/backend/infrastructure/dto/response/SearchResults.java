package com.studymedical.backend.infrastructure.dto.response;

import com.studymedical.backend.domain.entities.MedicalSearchResult;

import lombok.Data;

import java.util.List;

@Data
public class SearchResults {
    private List<MedicalSearchResult> top;
    private List<MedicalSearchResult> nih;
    private List<MedicalSearchResult> local;
}
