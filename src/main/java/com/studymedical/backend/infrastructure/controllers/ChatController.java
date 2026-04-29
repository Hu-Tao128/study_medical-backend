package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.chat.GetChatHistoryUseCase;
import com.studymedical.backend.application.usecases.chat.SendMessageUseCase;
import com.studymedical.backend.domain.entities.ChatMessageBucket;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.dto.response.ChatMessageBucketResponseDto;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessageBucketResponseDto> sendMessage(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID roomId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        roleAuthorizationService.requireSelfOrRole(currentUser, request.senderId(), User.Role.ADMIN);
        roleAuthorizationService.requireGroupAccess(currentUser, roomId);

        ChatMessageBucket.Message message = ChatMessageBucket.Message.builder()
                .senderId(request.senderId())
                .text(request.text())
                .type(request.type())
                .build();

        ChatMessageBucket bucket = sendMessageUseCase.execute(roomId, message);
        return ResponseEntity.ok(ChatMessageBucketResponseDto.fromEntity(bucket));
    }

    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<ChatMessageBucketResponseDto.MessageDto>> getHistory(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID roomId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        roleAuthorizationService.requireGroupAccess(currentUser, roomId);
        List<ChatMessageBucketResponseDto.MessageDto> messages = getChatHistoryUseCase.execute(roomId).stream()
                .map(ChatMessageBucketResponseDto.MessageDto::fromEntity)
                .toList();
        return ResponseEntity.ok(messages);
    }

    public record SendMessageRequest(
            @NotNull UUID senderId,
            @NotBlank String text,
            ChatMessageBucket.MessageType type
    ) {
    }
}
