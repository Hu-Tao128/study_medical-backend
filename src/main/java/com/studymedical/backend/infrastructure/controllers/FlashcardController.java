package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.flashcard.CreateFlashcardUseCase;
import com.studymedical.backend.application.usecases.flashcard.GetFlashcardsByTopicUseCase;
import com.studymedical.backend.domain.entities.Flashcard;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final CreateFlashcardUseCase createFlashcardUseCase;
    private final GetFlashcardsByTopicUseCase getFlashcardsByTopicUseCase;

    @PostMapping
    public ResponseEntity<Flashcard> createFlashcard(@RequestBody Flashcard flashcard) {
        return ResponseEntity.ok(createFlashcardUseCase.execute(flashcard));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<Flashcard>> getByTopic(@PathVariable UUID topicId) {
        return ResponseEntity.ok(getFlashcardsByTopicUseCase.execute(topicId));
    }
}
