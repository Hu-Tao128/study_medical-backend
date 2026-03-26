package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.Note;
import com.studymedical.backend.domain.repositories.mongo.NoteMongoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteMongoRepository noteMongoRepository;

    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody UpsertNoteRequest request) {
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
    public ResponseEntity<?> updateNote(@PathVariable String id, @Valid @RequestBody UpsertNoteRequest request) {
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
    public ResponseEntity<List<Note>> getByUser(@PathVariable UUID userId) {
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
