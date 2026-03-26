package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.Note;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.NoteMongoRepository;
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
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteMongoRepository noteMongoRepository;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping
    public ResponseEntity<Note> createNote(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody UpsertNoteRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireSelfOrRole(currentUser, request.userId(), User.Role.ADMIN);

        Note note = Note.builder()
                .userId(request.userId())
                .title(request.title())
                .contentMd(request.contentMd())
                .topicId(request.topicId())
                .aiSummary(request.aiSummary())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .build();
        note.initializeOnCreate();
        return ResponseEntity.ok(noteMongoRepository.save(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @Valid @RequestBody UpsertNoteRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireSelfOrRole(currentUser, request.userId(), User.Role.ADMIN);

        return noteMongoRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(request.title());
                    existing.setContentMd(request.contentMd());
                    existing.setTopicId(request.topicId());
                    existing.setAiSummary(request.aiSummary());
                    existing.setAiEmbeddingsId(request.aiEmbeddingsId());
                    existing.initializeOnUpdate();
                    return ResponseEntity.ok(noteMongoRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Note>> getByUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID userId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireSelfOrRole(currentUser, userId, User.Role.ADMIN);
        return ResponseEntity.ok(noteMongoRepository.findByUserId(userId));
    }

    public record UpsertNoteRequest(
            @NotNull UUID userId,
            @NotBlank String title,
            @NotBlank String contentMd,
            @NotNull UUID topicId,
            String aiSummary,
            String aiEmbeddingsId,
            Boolean aiGenerated,
            String aiModel,
            String aiSource
    ) {
    }
}
