package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.domain.entities.Note;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.NoteMongoRepository;
import com.studymedical.backend.infrastructure.dto.response.NoteResponseDto;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteMongoRepository noteMongoRepository;
    private final RoleAuthorizationService roleAuthorizationService;
    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody UpsertNoteRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        
        // Usamos el ID interno del usuario autenticado para la nota
        UUID targetUserId = currentUser.getId();
        UUID targetTopicId = (request.topicId() != null && !request.topicId().isBlank()) 
                ? createUserUseCase.normalizeAuthId(request.topicId()) 
                : null;

        Note note = Note.builder()
                .userId(targetUserId)
                .title(request.title())
                .contentMd(request.contentMd())
                .topicId(targetTopicId)
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
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);

        return noteMongoRepository.findById(id)
                .map(note -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, note.getUserId(), User.Role.ADMIN);
                    return ResponseEntity.ok(NoteResponseDto.fromEntity(note));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> listNotes(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(value = "topicId", required = false) String topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        UUID targetTopicId = (topicId != null && !topicId.isBlank()) ? createUserUseCase.normalizeAuthId(topicId) : null;

        List<Note> notes = targetTopicId == null
                ? noteMongoRepository.findByUserId(currentUser.getId())
                : noteMongoRepository.findByUserIdAndTopicId(currentUser.getId(), targetTopicId);

        return ResponseEntity.ok(notes.stream().map(NoteResponseDto::fromEntity).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id,
            @Valid @RequestBody UpsertNoteRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);

        return noteMongoRepository.findById(id)
                .map(existing -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, existing.getUserId(), User.Role.ADMIN);
                    
                    UUID targetTopicId = (request.topicId() != null && !request.topicId().isBlank()) 
                            ? createUserUseCase.normalizeAuthId(request.topicId()) 
                            : null;

                    existing.setTitle(request.title());
                    existing.setContentMd(request.contentMd());
                    existing.setTopicId(targetTopicId);
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
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id,
            @Valid @RequestBody PatchNoteRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);

        return noteMongoRepository.findById(id)
                .map(existing -> {
                    roleAuthorizationService.requireSelfOrRole(currentUser, existing.getUserId(), User.Role.ADMIN);
                    if (request.title() != null) {
                        existing.setTitle(request.title());
                    }
                    if (request.contentMd() != null) {
                        existing.setContentMd(request.contentMd());
                    }
                    if (request.topicId() != null && !request.topicId().isBlank()) {
                        existing.setTopicId(createUserUseCase.normalizeAuthId(request.topicId()));
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
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);

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
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String userId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        UUID targetAuthId = createUserUseCase.normalizeAuthId(userId);
        
        // Si el userId es el authId de Firebase, necesitamos buscar el usuario por authId
        if (currentUser.getAuthId().equals(targetAuthId)) {
            List<NoteResponseDto> notes = noteMongoRepository.findByUserId(currentUser.getId()).stream()
                    .map(NoteResponseDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(notes);
        }
        
        // Para administradores o acceso cruzado (si se permite)
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.ADMIN);
        return ResponseEntity.ok(List.of()); // Simplificado para admin
    }

    public record UpsertNoteRequest(
            String userId, // Ahora opcional en lógica de creación
            @NotBlank String title,
            @NotBlank String contentMd,
            String topicId,
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
            String topicId,
            List<String> tags,
            Boolean isFavorite,
            Boolean isArchived
    ) {
    }
}
