package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.flashcard.CreateFlashcardUseCase;
import com.studymedical.backend.application.usecases.flashcard.GetFlashcardsByTopicUseCase;
import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.dto.response.FlashcardResponseDto;
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
    public ResponseEntity<FlashcardResponseDto> createFlashcard(
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
                .visibility(request.visibility())
                .groupId(request.groupId())
                .tags(request.tags())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .build();
        if (request.visibility() == Flashcard.Visibility.GROUP) {
            roleAuthorizationService.requireGroupTeacherOrAdmin(currentUser, request.groupId());
        }

        Flashcard created = createFlashcardUseCase.execute(flashcard);
        return ResponseEntity.ok(FlashcardResponseDto.fromEntity(created));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<FlashcardResponseDto>> getByTopic(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        List<FlashcardResponseDto> filtered = getFlashcardsByTopicUseCase.execute(topicId).stream()
                .filter(card -> roleAuthorizationService.canReadByVisibility(
                        currentUser,
                        card.getCreatedBy(),
                        toSecurityVisibility(card.getVisibility()),
                        card.getGroupId()
                ))
                .map(FlashcardResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    public record CreateFlashcardRequest(
            @NotNull UUID topicId,
            @NotBlank String question,
            @NotBlank String answer,
            Flashcard.Difficulty difficulty,
            Flashcard.Visibility visibility,
            UUID groupId,
            List<String> tags,
            Boolean aiGenerated,
            String aiModel,
            String aiSource,
            String aiEmbeddingsId
    ) {
    }

    private RoleAuthorizationService.QuizVisibility toSecurityVisibility(Flashcard.Visibility visibility) {
        if (visibility == null) {
            return RoleAuthorizationService.QuizVisibility.PRIVATE;
        }
        return switch (visibility) {
            case PRIVATE -> RoleAuthorizationService.QuizVisibility.PRIVATE;
            case GROUP -> RoleAuthorizationService.QuizVisibility.GROUP;
            case PUBLIC -> RoleAuthorizationService.QuizVisibility.PUBLIC;
        };
    }
}
