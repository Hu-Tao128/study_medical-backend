package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.flashcard.CreateFlashcardUseCase;
import com.studymedical.backend.application.usecases.flashcard.GetFlashcardsByTopicUseCase;
import com.studymedical.backend.domain.entities.Flashcard;
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
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final CreateFlashcardUseCase createFlashcardUseCase;
    private final GetFlashcardsByTopicUseCase getFlashcardsByTopicUseCase;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping
    public ResponseEntity<Flashcard> createFlashcard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateFlashcardRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.TEACHER, User.Role.ADMIN);

        Flashcard flashcard = Flashcard.builder()
                .topicId(request.topicId())
                .createdBy(currentUser.getId())
                .question(request.question())
                .answer(request.answer())
                .difficulty(request.difficulty())
                .tags(request.tags())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .build();
        return ResponseEntity.ok(createFlashcardUseCase.execute(flashcard));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<Flashcard>> getByTopic(@PathVariable UUID topicId) {
        return ResponseEntity.ok(getFlashcardsByTopicUseCase.execute(topicId));
    }

    public record CreateFlashcardRequest(
            @NotNull UUID topicId,
            @NotBlank String question,
            @NotBlank String answer,
            Flashcard.Difficulty difficulty,
            List<String> tags,
            Boolean aiGenerated,
            String aiModel,
            String aiSource,
            String aiEmbeddingsId
    ) {
    }
}
