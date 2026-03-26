package com.studymedical.backend.application.usecases.chat;

import com.studymedical.backend.domain.entities.ChatMessageBucket;
import com.studymedical.backend.domain.repositories.mongo.ChatMessageBucketMongoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class SendMessageUseCase {

    private final ChatMessageBucketMongoRepository chatMessageBucketMongoRepository;

    public SendMessageUseCase(ChatMessageBucketMongoRepository chatMessageBucketMongoRepository) {
        this.chatMessageBucketMongoRepository = chatMessageBucketMongoRepository;
    }

    public ChatMessageBucket execute(UUID roomId, ChatMessageBucket.Message message) {
        message.initializeDefaults();

        ChatMessageBucket bucket = chatMessageBucketMongoRepository
                .findTopByRoomIdOrderByCreatedAtDesc(roomId)
                .filter(existing -> existing.getCount() < ChatMessageBucket.MAX_BUCKET_SIZE)
                .orElseGet(() -> ChatMessageBucket.builder()
                        .id(roomId + "-" + Instant.now().toEpochMilli())
                        .roomId(roomId)
                        .messages(new ArrayList<>())
                        .count(0)
                        .build());

        bucket.getMessages().add(message);
        bucket.initializeDefaults();

        return chatMessageBucketMongoRepository.save(bucket);
    }
}
