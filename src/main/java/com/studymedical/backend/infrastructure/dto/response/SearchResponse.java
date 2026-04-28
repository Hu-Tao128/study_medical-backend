package com.studymedical.backend.infrastructure.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private String query;
    private List<String> sources;
    private SearchResults results;
    private Pagination pagination;
}
