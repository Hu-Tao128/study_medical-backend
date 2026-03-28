package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.Note;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.NoteMongoRepository;
import com.studymedical.backend.infrastructure.dto.response.NoteResponseDto;
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
    public ResponseEntity<NoteResponseDto> createNote(
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
                .tags(request.tags())
                .isFavorite(request.isFavorite() != null && request.isFavorite())
                .isArchived(request.isArchived() != null && request.isArchived())
                .build();
        note.initializeOnCreate();
        Note created = noteMongoRepository.save(note);
        return ResponseEntity.ok(NoteResponseDto.fromEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        return noteMongoRepository.findById(id)
                .map(note -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, note.getUserId(), User.Role.ADMIN);
                    return ResponseEntity.ok(NoteResponseDto.fromEntity(note));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> listNotes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "topicId", required = false) UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        List<Note> notes = topicId == null
                ? noteMongoRepository.findByUserId(currentUser.getId())
                : noteMongoRepository.findByUserIdAndTopicId(currentUser.getId(), topicId);

        return ResponseEntity.ok(notes.stream().map(NoteResponseDto::fromEntity).toList());
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
                    existing.setAiGenerated(request.aiGenerated() != null && request.aiGenerated());
                    existing.setAiModel(request.aiModel());
                    existing.setAiSource(request.aiSource());
                    existing.setTags(request.tags());
                    if (request.isFavorite() != null) {
                        existing.setFavorite(request.isFavorite());
                    }
                    if (request.isArchived() != null) {
                        existing.setArchived(request.isArchived());
                    }
                    existing.initializeOnUpdate();
                    Note updated = noteMongoRepository.save(existing);
                    return ResponseEntity.ok(NoteResponseDto.fromEntity(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchNote(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @Valid @RequestBody PatchNoteRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        return noteMongoRepository.findById(id)
                .map(existing -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, existing.getUserId(), User.Role.ADMIN);
                    if (request.title() != null) {
                        existing.setTitle(request.title());
                    }
                    if (request.contentMd() != null) {
                        existing.setContentMd(request.contentMd());
                    }
                    if (request.topicId() != null) {
                        existing.setTopicId(request.topicId());
                    }
                    if (request.tags() != null) {
                        existing.setTags(request.tags());
                    }
                    if (request.isFavorite() != null) {
                        existing.setFavorite(request.isFavorite());
                    }
                    if (request.isArchived() != null) {
                        existing.setArchived(request.isArchived());
                    }
                    existing.initializeOnUpdate();
                    Note updated = noteMongoRepository.save(existing);
                    return ResponseEntity.ok(NoteResponseDto.fromEntity(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        return noteMongoRepository.findById(id)
                .map(existing -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, existing.getUserId(), User.Role.ADMIN);
                    noteMongoRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteResponseDto>> getByUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID userId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireSelfOrRole(currentUser, userId, User.Role.ADMIN);
        List<NoteResponseDto> notes = noteMongoRepository.findByUserId(userId).stream()
                .map(NoteResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(notes);
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
            String aiSource,
            List<String> tags,
            Boolean isFavorite,
            Boolean isArchived
    ) {
    }

    public record PatchNoteRequest(
            String title,
            String contentMd,
            UUID topicId,
            List<String> tags,
            Boolean isFavorite,
            Boolean isArchived
    ) {
    }
}
