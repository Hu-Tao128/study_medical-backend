package com.studymedical.backend.application.usecases.chat;

import com.studymedical.backend.domain.entities.ChatMessageBucket;
import com.studymedical.backend.domain.repositories.mongo.ChatMessageBucketMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendMessageUseCaseTest {

    @Mock
    private ChatMessageBucketMongoRepository chatMessageBucketMongoRepository;

    private SendMessageUseCase sendMessageUseCase;

    @BeforeEach
    void setUp() {
        sendMessageUseCase = new SendMessageUseCase(chatMessageBucketMongoRepository);
    }

    @Test
    void shouldReuseLastBucketWhenItHasCapacity() {
        UUID roomId = UUID.randomUUID();

        ChatMessageBucket existing = ChatMessageBucket.builder()
                .id("bucket-1")
                .roomId(roomId)
                .messages(new ArrayList<>())
                .count(10)
                .createdAt(Instant.now())
                .build();

        ChatMessageBucket.Message message = ChatMessageBucket.Message.builder()
                .senderId(UUID.randomUUID())
                .text("hola")
                .build();

        when(chatMessageBucketMongoRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId)).thenReturn(Optional.of(existing));
        when(chatMessageBucketMongoRepository.save(any(ChatMessageBucket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessageBucket saved = sendMessageUseCase.execute(roomId, message);

        assertEquals("bucket-1", saved.getId());
        assertEquals(1, saved.getMessages().size());
        assertEquals(1, saved.getCount());
        assertNotNull(saved.getUpdatedAt());
        verify(chatMessageBucketMongoRepository).save(any(ChatMessageBucket.class));
    }

    @Test
    void shouldCreateNewBucketWhenLastBucketIsFull() {
        UUID roomId = UUID.randomUUID();

        ChatMessageBucket full = ChatMessageBucket.builder()
                .id("bucket-full")
                .roomId(roomId)
                .messages(new ArrayList<>(List.of(new ChatMessageBucket.Message())))
                .count(ChatMessageBucket.MAX_BUCKET_SIZE)
                .createdAt(Instant.now())
                .build();

        ChatMessageBucket.Message message = ChatMessageBucket.Message.builder()
                .senderId(UUID.randomUUID())
                .text("mensaje")
                .build();

        when(chatMessageBucketMongoRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId)).thenReturn(Optional.of(full));
        when(chatMessageBucketMongoRepository.save(any(ChatMessageBucket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessageBucket saved = sendMessageUseCase.execute(roomId, message);

        assertNotNull(saved.getId());
        assertEquals(roomId, saved.getRoomId());
        assertEquals(1, saved.getMessages().size());
        assertEquals(1, saved.getCount());
    }
}
