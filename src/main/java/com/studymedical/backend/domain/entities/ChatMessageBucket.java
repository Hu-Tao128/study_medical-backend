package com.studymedical.backend.domain.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "chat_buckets")
@CompoundIndex(name = "idx_chat_room_bucket", def = "{'roomId': 1, 'bucketIndex': -1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageBucket {

    public static final int MAX_BUCKET_SIZE = 50;

    @Id
    private String id;

    @Indexed
    private UUID roomId;

    private int bucketIndex;

    private int count;

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;

    public void initializeDefaults() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
        count = messages != null ? messages.size() : 0;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private UUID senderId;
        private String text;

        @Builder.Default
        private MessageType type = MessageType.TEXT;

        private Instant createdAt;

        public void initializeDefaults() {
            if (type == null) {
                type = MessageType.TEXT;
            }
            if (createdAt == null) {
                createdAt = Instant.now();
            }
        }
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }
}
