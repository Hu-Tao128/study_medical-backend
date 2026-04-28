package com.studymedical.backend.domain.services;

import lombok.Data;

@Data
public class MedicalSearchResultWithEmbedding {
    private String id;
    private String chunkTitle;
    private String chunkText;
    private String book;
    private String author;
    private String edition;
    private double similarity;
}
