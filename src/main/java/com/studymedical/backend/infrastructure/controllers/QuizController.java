package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.quiz.GenerateQuizFromAiUseCase;
import com.studymedical.backend.application.usecases.quiz.SubmitQuizUseCase;
import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final GenerateQuizFromAiUseCase generateQuizFromAiUseCase;
    private final SubmitQuizUseCase submitQuizUseCase;
    private final QuizMongoRepository quizMongoRepository;

    @PostMapping("/ai-generate")
    public ResponseEntity<Quiz> generateQuiz(@RequestBody Quiz quizDraft) {
        return ResponseEntity.ok(generateQuizFromAiUseCase.execute(quizDraft));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<Quiz>> getByTopic(@PathVariable UUID topicId) {
        return ResponseEntity.ok(quizMongoRepository.findByTopicId(topicId));
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<SubmitQuizUseCase.SubmitQuizResult> submitQuiz(
            @PathVariable String quizId,
            @RequestBody SubmitQuizRequest request
    ) {
        SubmitQuizUseCase.SubmitQuizResult result = submitQuizUseCase.execute(
                request.userId(),
                quizId,
                request.answers() != null ? request.answers() : List.of()
        );
        return ResponseEntity.ok(result);
    }

    public record SubmitQuizRequest(UUID userId, List<Integer> answers) {
    }
}
