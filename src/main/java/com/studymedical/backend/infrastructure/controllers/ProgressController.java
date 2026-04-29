package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.studysession.GetProgressUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final GetProgressUseCase getProgressUseCase;
    private final RoleAuthorizationService roleAuthorizationService;

    @GetMapping
    public ResponseEntity<ProgressResponse> getByTopic(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam @NotNull UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        GetProgressUseCase.ProgressResult result = getProgressUseCase.execute(currentUser.getId(), topicId);
        return ResponseEntity.ok(new ProgressResponse(result.accuracy(), result.attempts(), result.lastStudiedAt()));
    }

    @GetMapping("/radar")
    public ResponseEntity<RadarResponse> radar(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        GetProgressUseCase.RadarResult result = getProgressUseCase.executeRadar(currentUser.getId());
        List<RadarTopicResponse> topics = result.topics().stream()
                .map(topic -> new RadarTopicResponse(topic.topicId(), topic.name(), topic.accuracy()))
                .toList();
        return ResponseEntity.ok(new RadarResponse(topics));
    }

    public record ProgressResponse(double accuracy, int attempts, LocalDateTime lastStudiedAt) {
    }

    public record RadarResponse(List<RadarTopicResponse> topics) {
    }

    public record RadarTopicResponse(UUID topicId, String name, double accuracy) {
    }
}
