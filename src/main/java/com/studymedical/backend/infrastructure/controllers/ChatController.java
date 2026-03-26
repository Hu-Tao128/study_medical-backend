package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.chat.GetChatHistoryUseCase;
import com.studymedical.backend.application.usecases.chat.SendMessageUseCase;
import com.studymedical.backend.domain.entities.ChatMessageBucket;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessageBucket> sendMessage(
            @PathVariable UUID roomId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        ChatMessageBucket.Message message = ChatMessageBucket.Message.builder()
                .senderId(request.senderId())
                .text(request.text())
                .type(request.type())
                .build();

        return ResponseEntity.ok(sendMessageUseCase.execute(roomId, message));
    }

    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<ChatMessageBucket.Message>> getHistory(@PathVariable UUID roomId) {
        return ResponseEntity.ok(getChatHistoryUseCase.execute(roomId));
    }

    public record SendMessageRequest(
            @NotNull UUID senderId,
            @NotBlank String text,
            ChatMessageBucket.MessageType type
    ) {
    }
}
