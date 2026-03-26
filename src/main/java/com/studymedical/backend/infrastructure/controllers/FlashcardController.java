package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.flashcard.CreateFlashcardUseCase;
import com.studymedical.backend.application.usecases.flashcard.GetFlashcardsByTopicUseCase;
import com.studymedical.backend.domain.entities.Flashcard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    public ResponseEntity<Flashcard> createFlashcard(@Valid @RequestBody CreateFlashcardRequest request) {
        Flashcard flashcard = Flashcard.builder()
                .topicId(request.topicId())
                .createdBy(request.createdBy())
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
            @NotNull UUID createdBy,
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
