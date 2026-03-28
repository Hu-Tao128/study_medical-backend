package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.studysession.GetProgressUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final GetProgressUseCase getProgressUseCase;
    private final RoleAuthorizationService roleAuthorizationService;

    @GetMapping
    public ResponseEntity<ProgressResponse> getByTopic(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam @NotNull UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        GetProgressUseCase.ProgressResult result = getProgressUseCase.execute(currentUser.getId(), topicId);
        return ResponseEntity.ok(new ProgressResponse(result.accuracy(), result.attempts(), result.lastStudiedAt()));
    }

    public record ProgressResponse(double accuracy, int attempts, LocalDateTime lastStudiedAt) {
    }
}
