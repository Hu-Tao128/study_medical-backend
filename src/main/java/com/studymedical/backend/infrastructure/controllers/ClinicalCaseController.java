package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.ClinicalCase;
import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.repositories.mongo.ClinicalCaseMongoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class ClinicalCaseController {

    private final ClinicalCaseMongoRepository clinicalCaseMongoRepository;

    @PostMapping
    public ResponseEntity<ClinicalCase> createCase(@Valid @RequestBody CreateClinicalCaseRequest request) {
        List<ClinicalCase.CaseQuestion> questions = request.questions() == null
                ? List.of()
                : request.questions().stream()
                .map(q -> ClinicalCase.CaseQuestion.builder()
                        .question(q.question())
                        .options(q.options())
                        .correctAnswer(q.correctAnswer())
                        .explanation(q.explanation())
                        .build())
                .toList();

        ClinicalCase clinicalCase = ClinicalCase.builder()
                .title(request.title())
                .description(request.description())
                .symptoms(request.symptoms())
                .diagnosis(request.diagnosis())
                .questions(questions)
                .topicId(request.topicId())
                .difficulty(request.difficulty())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .build();
        clinicalCase.initializeDefaults();
        return ResponseEntity.ok(clinicalCaseMongoRepository.save(clinicalCase));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ClinicalCase>> getByTopic(@PathVariable UUID topicId) {
        return ResponseEntity.ok(clinicalCaseMongoRepository.findByTopicId(topicId));
    }

    public record CreateClinicalCaseRequest(
            @NotBlank String title,
            @NotBlank String description,
            List<String> symptoms,
            @NotBlank String diagnosis,
            List<CreateCaseQuestionRequest> questions,
            @NotNull UUID topicId,
            Flashcard.Difficulty difficulty,
            Boolean aiGenerated,
            String aiModel,
            String aiSource,
            String aiEmbeddingsId
    ) {
    }

    public record CreateCaseQuestionRequest(
            @NotBlank String question,
            List<String> options,
            Integer correctAnswer,
            String explanation
    ) {
    }
}
