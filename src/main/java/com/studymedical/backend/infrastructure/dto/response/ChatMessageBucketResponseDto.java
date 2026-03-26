package com.studymedical.backend.infrastructure.dto.response;

import com.studymedical.backend.domain.entities.ChatMessageBucket;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatMessageBucketResponseDto(
        String id,
        UUID roomId,
        int count,
        List<MessageDto> messages,
        Instant createdAt,
        Instant updatedAt
) {
    public static ChatMessageBucketResponseDto fromEntity(ChatMessageBucket bucket) {
        return new ChatMessageBucketResponseDto(
                bucket.getId(),
                bucket.getRoomId(),
                bucket.getCount(),
                bucket.getMessages() != null
                        ? bucket.getMessages().stream()
                                .map(MessageDto::fromEntity)
                                .toList()
                        : List.of(),
                bucket.getCreatedAt(),
                bucket.getUpdatedAt()
        );
    }

    public record MessageDto(
            UUID senderId,
            String text,
            String type,
            Instant createdAt
    ) {
        public static MessageDto fromEntity(ChatMessageBucket.Message message) {
            return new MessageDto(
                    message.getSenderId(),
                    message.getText(),
                    message.getType() != null ? message.getType().name() : "TEXT",
                    message.getCreatedAt()
            );
        }
    }
}
