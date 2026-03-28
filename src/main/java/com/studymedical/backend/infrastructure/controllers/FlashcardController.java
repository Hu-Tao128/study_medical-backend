package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.flashcard.CreateFlashcardUseCase;
import com.studymedical.backend.application.usecases.flashcard.GetFlashcardsByTopicUseCase;
import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.FlashcardMongoRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final CreateFlashcardUseCase createFlashcardUseCase;
    private final GetFlashcardsByTopicUseCase getFlashcardsByTopicUseCase;
    private final FlashcardMongoRepository flashcardMongoRepository;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        return flashcardMongoRepository.findById(id)
                .map(card -> {
                    boolean canRead = roleAuthorizationService.canReadByVisibility(
                            currentUser,
                            card.getCreatedBy(),
                            toSecurityVisibility(card.getVisibility()),
                            card.getGroupId()
                    );
                    if (!canRead) {
                        throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Sin acceso a esta flashcard");
                    }
                    return ResponseEntity.ok(FlashcardResponseDto.fromEntity(card));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @Valid @RequestBody CreateFlashcardRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.TEACHER, User.Role.ADMIN);

        return flashcardMongoRepository.findById(id)
                .map(existing -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, existing.getCreatedBy(), User.Role.ADMIN);
                    existing.setTopicId(request.topicId());
                    existing.setQuestion(request.question());
                    existing.setAnswer(request.answer());
                    existing.setDifficulty(request.difficulty());
                    existing.setVisibility(request.visibility());
                    existing.setGroupId(request.groupId());
                    existing.setTags(request.tags());
                    existing.setAiGenerated(request.aiGenerated() != null && request.aiGenerated());
                    existing.setAiModel(request.aiModel());
                    existing.setAiSource(request.aiSource());
                    existing.setAiEmbeddingsId(request.aiEmbeddingsId());
                    existing.initializeDefaults();
                    Flashcard updated = flashcardMongoRepository.save(existing);
                    return ResponseEntity.ok(FlashcardResponseDto.fromEntity(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        return flashcardMongoRepository.findById(id)
                .map(existing -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, existing.getCreatedBy(), User.Role.ADMIN);
                    flashcardMongoRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
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
