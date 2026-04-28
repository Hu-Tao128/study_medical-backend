package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.MedicalChunk;

import java.util.List;
import java.util.UUID;

public interface MedicalChunkRepository {
    List<MedicalChunk> findByDisease(String disease, int limit, int offset);
    List<MedicalChunk> findByBook(String book, int limit, int offset);
    List<MedicalChunk> findAll(int limit, int offset);
    List<MedicalChunk> searchByText(String query, int limit, int offset);
}
