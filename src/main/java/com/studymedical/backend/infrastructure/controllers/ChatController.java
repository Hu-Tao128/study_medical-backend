package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.chat.GetChatHistoryUseCase;
import com.studymedical.backend.application.usecases.chat.SendMessageUseCase;
import com.studymedical.backend.domain.entities.ChatMessageBucket;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<ChatMessageBucket> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID roomId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireSelfOrRole(currentUser, request.senderId(), User.Role.ADMIN);

        ChatMessageBucket.Message message = ChatMessageBucket.Message.builder()
                .senderId(request.senderId())
                .text(request.text())
                .type(request.type())
                .build();

        return ResponseEntity.ok(sendMessageUseCase.execute(roomId, message));
    }

    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<ChatMessageBucket.Message>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID roomId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.STUDENT, User.Role.TEACHER, User.Role.ADMIN);
        return ResponseEntity.ok(getChatHistoryUseCase.execute(roomId));
    }

    public record SendMessageRequest(
            @NotNull UUID senderId,
            @NotBlank String text,
            ChatMessageBucket.MessageType type
    ) {
    }
}
