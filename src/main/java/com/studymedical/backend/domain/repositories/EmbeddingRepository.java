package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

    List<Embedding> findByContentTypeAndContentId(String contentType, UUID contentId);
}
