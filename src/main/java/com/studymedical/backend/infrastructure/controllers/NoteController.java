package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.Note;
import com.studymedical.backend.domain.repositories.mongo.NoteMongoRepository;
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
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        note.initializeOnCreate();
        return ResponseEntity.ok(noteMongoRepository.save(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable String id, @RequestBody Note note) {
        return noteMongoRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(note.getTitle());
                    existing.setContentMd(note.getContentMd());
                    existing.setTopicId(note.getTopicId());
                    existing.setAiSummary(note.getAiSummary());
                    existing.setAiEmbeddingsId(note.getAiEmbeddingsId());
                    existing.initializeOnUpdate();
                    return ResponseEntity.ok(noteMongoRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Note>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(noteMongoRepository.findByUserId(userId));
    }
}
