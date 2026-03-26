package com.studymedical.backend.domain.repositories.mongo;

import com.studymedical.backend.domain.entities.ClinicalCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClinicalCaseMongoRepository extends MongoRepository<ClinicalCase, String> {

    List<ClinicalCase> findByTopicId(UUID topicId);
}
