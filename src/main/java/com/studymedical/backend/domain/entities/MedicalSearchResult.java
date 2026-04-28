package com.studymedical.backend.domain.entities;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class MedicalSearchResult {
    private String id;
    private String title;
    private String description;
    private SearchSource source;
    private String url;
    private List<String> authors;
    private String publicationDate;
    private String bookTitle;
    private String edition;
    private ContentType contentType;
    private Double relevanceScore;
}
