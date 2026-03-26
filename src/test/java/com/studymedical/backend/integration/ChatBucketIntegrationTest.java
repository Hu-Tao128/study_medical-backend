package com.studymedical.backend.integration;

import com.studymedical.backend.application.usecases.chat.GetChatHistoryUseCase;
import com.studymedical.backend.application.usecases.chat.SendMessageUseCase;
import com.studymedical.backend.domain.entities.ChatMessageBucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ChatBucketIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("app.dev-mode", () -> "true");
    }

    @Autowired
    private SendMessageUseCase sendMessageUseCase;

    @Autowired
    private GetChatHistoryUseCase getChatHistoryUseCase;

    @Test
    void shouldSplitMessagesInBucketsAndReturnHistory() {
        UUID roomId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        for (int i = 0; i < 55; i++) {
            ChatMessageBucket.Message message = ChatMessageBucket.Message.builder()
                    .senderId(senderId)
                    .text("mensaje-" + i)
                    .type(ChatMessageBucket.MessageType.TEXT)
                    .build();
            sendMessageUseCase.execute(roomId, message);
        }

        List<ChatMessageBucket.Message> history = getChatHistoryUseCase.execute(roomId);
        assertEquals(55, history.size());
    }
}
