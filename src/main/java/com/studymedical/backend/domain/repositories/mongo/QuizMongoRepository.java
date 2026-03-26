package com.studymedical.backend.domain.repositories.mongo;

import com.studymedical.backend.domain.entities.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizMongoRepository extends MongoRepository<Quiz, String> {

    List<Quiz> findByTopicId(UUID topicId);
}
