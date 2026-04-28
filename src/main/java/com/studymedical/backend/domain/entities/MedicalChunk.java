package com.studymedical.backend.domain.entities;

import lombok.Data;

import java.util.UUID;

@Data
public class MedicalChunk {
    private UUID id;
    private String chunkText;
    private String chunkTitle;
    private String book;
    private String author;
    private String edition;
    private String tags;
    private String disease;
}
