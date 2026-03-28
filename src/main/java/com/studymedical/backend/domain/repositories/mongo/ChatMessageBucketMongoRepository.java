package com.studymedical.backend.domain.repositories.mongo;

import com.studymedical.backend.domain.entities.ChatMessageBucket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageBucketMongoRepository extends MongoRepository<ChatMessageBucket, String> {

    Optional<ChatMessageBucket> findTopByRoomIdOrderByBucketIndexDesc(UUID roomId);

    List<ChatMessageBucket> findByRoomIdOrderByBucketIndexAsc(UUID roomId);
}
