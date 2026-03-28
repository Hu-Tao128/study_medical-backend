package com.studymedical.backend.domain.repositories.mongo;

import com.studymedical.backend.domain.entities.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteMongoRepository extends MongoRepository<Note, String> {

    List<Note> findByUserId(UUID userId);

    List<Note> findByTopicId(UUID topicId);

    List<Note> findByUserIdAndTopicId(UUID userId, UUID topicId);
}
