package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.quiz.GenerateQuizFromAiUseCase;
import com.studymedical.backend.application.usecases.quiz.SubmitQuizUseCase;
import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
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
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final GenerateQuizFromAiUseCase generateQuizFromAiUseCase;
    private final SubmitQuizUseCase submitQuizUseCase;
    private final QuizMongoRepository quizMongoRepository;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping("/ai-generate")
    public ResponseEntity<Quiz> generateQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody GenerateQuizRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.TEACHER, User.Role.ADMIN);

        List<Quiz.QuizQuestion> questions = request.questions() == null
                ? List.of()
                : request.questions().stream()
                .map(q -> Quiz.QuizQuestion.builder()
                        .question(q.question())
                        .options(q.options())
                        .correctAnswer(q.correctAnswer())
                        .explanation(q.explanation())
                        .aiGenerated(q.aiGenerated() != null && q.aiGenerated())
                        .build())
                .toList();

        Quiz quizDraft = Quiz.builder()
                .title(request.title())
                .topicId(request.topicId())
                .createdBy(currentUser.getId())
                .questions(questions)
                .visibility(request.visibility())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .build();
        return ResponseEntity.ok(generateQuizFromAiUseCase.execute(quizDraft));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<Quiz>> getByTopic(@PathVariable UUID topicId) {
        return ResponseEntity.ok(quizMongoRepository.findByTopicId(topicId));
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<SubmitQuizUseCase.SubmitQuizResult> submitQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String quizId,
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireSelfOrRole(currentUser, request.userId(), User.Role.ADMIN, User.Role.TEACHER);

        SubmitQuizUseCase.SubmitQuizResult result = submitQuizUseCase.execute(
                request.userId(),
                quizId,
                request.answers() != null ? request.answers() : List.of()
        );
        return ResponseEntity.ok(result);
    }

    public record SubmitQuizRequest(@NotNull UUID userId, List<Integer> answers) {
    }

    public record GenerateQuizRequest(
            @NotBlank String title,
            @NotNull UUID topicId,
            List<GenerateQuizQuestionRequest> questions,
            Quiz.Visibility visibility,
            Boolean aiGenerated,
            String aiModel,
            String aiSource,
            String aiEmbeddingsId
    ) {
    }

    public record GenerateQuizQuestionRequest(
            @NotBlank String question,
            List<String> options,
            @NotNull Integer correctAnswer,
            String explanation,
            Boolean aiGenerated
    ) {
    }
}
