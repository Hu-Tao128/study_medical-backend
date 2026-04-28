package com.studymedical.backend.infrastructure.dto.request;

import lombok.Data;

@Data
public class SearchQuery {
    private String q;
    private String source;
    private int limit;
    private int page;

    public int getOffset() {
        return (page - 1) * limit;
    }

    public String getSanitizedSource() {
        return source == null ? "all" : source;
    }
}
