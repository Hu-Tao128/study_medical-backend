package com.studymedical.backend.domain.repositories.mongo;

import com.studymedical.backend.domain.entities.Flashcard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashcardMongoRepository extends MongoRepository<Flashcard, String> {

    List<Flashcard> findByTopicId(UUID topicId);
}
