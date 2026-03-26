package com.studymedical.backend.application.usecases.chat;

import com.studymedical.backend.domain.entities.ChatMessageBucket;
import com.studymedical.backend.domain.repositories.mongo.ChatMessageBucketMongoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetChatHistoryUseCase {

    private final ChatMessageBucketMongoRepository chatMessageBucketMongoRepository;

    public GetChatHistoryUseCase(ChatMessageBucketMongoRepository chatMessageBucketMongoRepository) {
        this.chatMessageBucketMongoRepository = chatMessageBucketMongoRepository;
    }

    public List<ChatMessageBucket.Message> execute(UUID roomId) {
        List<ChatMessageBucket> buckets = chatMessageBucketMongoRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        List<ChatMessageBucket.Message> messages = new ArrayList<>();

        for (ChatMessageBucket bucket : buckets) {
            if (bucket.getMessages() != null) {
                messages.addAll(bucket.getMessages());
            }
        }

        return messages;
    }
}
