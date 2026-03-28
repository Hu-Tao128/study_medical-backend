package com.studymedical.backend.application.usecases.chat;

import com.studymedical.backend.domain.entities.ChatMessageBucket;
import com.studymedical.backend.domain.repositories.mongo.ChatMessageBucketMongoRepository;
import org.springframework.stereotype.Service;

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
                .findTopByRoomIdOrderByBucketIndexDesc(roomId)
                .filter(existing -> existing.getCount() < ChatMessageBucket.MAX_BUCKET_SIZE)
                .orElseGet(() -> ChatMessageBucket.builder()
                        .roomId(roomId)
                        .bucketIndex(
                                chatMessageBucketMongoRepository
                                        .findTopByRoomIdOrderByBucketIndexDesc(roomId)
                                        .map(last -> last.getBucketIndex() + 1)
                                        .orElse(0)
                        )
                        .messages(new ArrayList<>())
                        .count(0)
                        .build());

        bucket.getMessages().add(message);
        bucket.initializeDefaults();

        return chatMessageBucketMongoRepository.save(bucket);
    }
}
