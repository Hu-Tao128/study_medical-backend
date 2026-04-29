package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.quiz.GenerateQuizFromAiUseCase;
import com.studymedical.backend.application.usecases.quiz.SubmitQuizUseCase;
import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
import com.studymedical.backend.infrastructure.dto.response.QuizResponseDto;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<QuizResponseDto> generateQuiz(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody GenerateQuizRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
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
                .groupId(request.groupId())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .build();

        if (request.visibility() == Quiz.Visibility.GROUP) {
            roleAuthorizationService.requireGroupTeacherOrAdmin(currentUser, request.groupId());
        }

        Quiz created = generateQuizFromAiUseCase.execute(quizDraft);
        return ResponseEntity.ok(QuizResponseDto.fromEntity(created));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<QuizResponseDto>> getByTopic(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        List<QuizResponseDto> filtered = quizMongoRepository.findByTopicId(topicId).stream()
                .filter(quiz -> roleAuthorizationService.canReadByVisibility(
                        currentUser,
                        quiz.getCreatedBy(),
                        toSecurityVisibility(quiz.getVisibility()),
                        quiz.getGroupId()
                ))
                .map(QuizResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<SubmitQuizUseCase.SubmitQuizResult> submitQuiz(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String quizId,
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        roleAuthorizationService.requireSelfOrRole(currentUser, request.userId(), User.Role.ADMIN, User.Role.TEACHER);

        Quiz quiz = quizMongoRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Quiz no encontrado"));

        boolean canAccessQuiz = roleAuthorizationService.canReadByVisibility(
                currentUser,
                quiz.getCreatedBy(),
                toSecurityVisibility(quiz.getVisibility()),
                quiz.getGroupId()
        );

        if (!canAccessQuiz) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Sin acceso a este quiz");
        }

        SubmitQuizUseCase.SubmitQuizResult result = submitQuizUseCase.execute(
                request.userId(),
                quizId,
                request.answers() != null ? request.answers() : List.of()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> getById(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        Quiz quiz = quizMongoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Quiz no encontrado"));

        boolean canAccessQuiz = roleAuthorizationService.canReadByVisibility(
                currentUser,
                quiz.getCreatedBy(),
                toSecurityVisibility(quiz.getVisibility()),
                quiz.getGroupId()
        );

        if (!canAccessQuiz) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Sin acceso a este quiz");
        }

        return ResponseEntity.ok(QuizResponseDto.fromEntity(quiz));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizResponseDto> updateQuiz(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id,
            @Valid @RequestBody GenerateQuizRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.TEACHER, User.Role.ADMIN);

        Quiz existing = quizMongoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Quiz no encontrado"));

        roleAuthorizationService.requireSelfOrRole(currentUser, existing.getCreatedBy(), User.Role.ADMIN);

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

        existing.setTitle(request.title());
        existing.setTopicId(request.topicId());
        existing.setQuestions(questions);
        existing.setVisibility(request.visibility());
        existing.setGroupId(request.groupId());
        existing.setAiGenerated(request.aiGenerated() != null && request.aiGenerated());
        existing.setAiModel(request.aiModel());
        existing.setAiSource(request.aiSource());
        existing.setAiEmbeddingsId(request.aiEmbeddingsId());
        existing.initializeDefaults();

        Quiz updated = quizMongoRepository.save(existing);
        return ResponseEntity.ok(QuizResponseDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        Quiz existing = quizMongoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Quiz no encontrado"));

        roleAuthorizationService.requireSelfOrRole(currentUser, existing.getCreatedBy(), User.Role.ADMIN);
        quizMongoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record SubmitQuizRequest(@NotNull UUID userId, List<Integer> answers) {
    }

    public record GenerateQuizRequest(
            @NotBlank String title,
            @NotNull UUID topicId,
            List<GenerateQuizQuestionRequest> questions,
            Quiz.Visibility visibility,
            UUID groupId,
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

    private RoleAuthorizationService.QuizVisibility toSecurityVisibility(Quiz.Visibility visibility) {
        if (visibility == null) {
            return RoleAuthorizationService.QuizVisibility.PRIVATE;
        }
        return switch (visibility) {
            case PRIVATE -> RoleAuthorizationService.QuizVisibility.PRIVATE;
            case GROUP -> RoleAuthorizationService.QuizVisibility.GROUP;
            case PUBLIC -> RoleAuthorizationService.QuizVisibility.PUBLIC;
        };
    }
}
