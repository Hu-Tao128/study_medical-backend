package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.studysession.GetStudySessionsUseCase;
import com.studymedical.backend.application.usecases.studysession.StartStudySessionUseCase;
import com.studymedical.backend.application.usecases.studysession.SubmitStudySessionUseCase;
import com.studymedical.backend.domain.entities.StudySession;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StartStudySessionUseCase startStudySessionUseCase;
    private final SubmitStudySessionUseCase submitStudySessionUseCase;
    private final GetStudySessionsUseCase getStudySessionsUseCase;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping("/start")
    public ResponseEntity<StartStudySessionResponse> start(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody StartStudySessionRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        StartStudySessionUseCase.StartStudySessionResult result = startStudySessionUseCase.execute(
                currentUser.getId(),
                request.topicId(),
                request.mode(),
                request.limit() == null ? 20 : request.limit()
        );

        List<StartStudySessionResponse.CardDto> cards = result.cards().stream()
                .map(card -> new StartStudySessionResponse.CardDto(card.cardId(), card.question(), card.tags()))
                .toList();

        return ResponseEntity.ok(new StartStudySessionResponse(result.sessionId(), cards));
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitStudySessionUseCase.SubmitStudySessionResult> submit(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody SubmitStudySessionRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        List<SubmitStudySessionUseCase.Attempt> attempts = request.attempts() == null
                ? List.of()
                : request.attempts().stream()
                .map(a -> new SubmitStudySessionUseCase.Attempt(a.cardId(), a.difficulty(), Boolean.TRUE.equals(a.correct()), a.timeMs() == null ? 0L : a.timeMs()))
                .toList();

        SubmitStudySessionUseCase.SubmitStudySessionResult result = submitStudySessionUseCase.execute(
                currentUser.getId(),
                request.sessionId(),
                request.topicId(),
                attempts
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<StudySessionDto>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "topicId", required = false) UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        List<StudySessionDto> sessions = getStudySessionsUseCase.execute(currentUser.getId(), topicId).stream()
                .map(StudySessionDto::fromEntity)
                .toList();
        return ResponseEntity.ok(sessions);
    }

    public record StartStudySessionRequest(
            @NotNull UUID topicId,
            String mode,
            Integer limit
    ) {
    }

    public record StartStudySessionResponse(UUID sessionId, List<CardDto> cards) {
        public record CardDto(String cardId, String question, List<String> tags) {
        }
    }

    public record SubmitStudySessionRequest(
            @NotNull UUID sessionId,
            @NotNull UUID topicId,
            List<AttemptDto> attempts
    ) {
    }

    public record AttemptDto(
            @NotNull String cardId,
            Integer difficulty,
            Boolean correct,
            Long timeMs
    ) {
    }

    public record StudySessionDto(
            UUID id,
            UUID topicId,
            String mode,
            int totalQuestions,
            int correctAnswers,
            double accuracy,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        static StudySessionDto fromEntity(StudySession session) {
            return new StudySessionDto(
                    session.getId(),
                    session.getTopic() != null ? session.getTopic().getId() : null,
                    session.getMode(),
                    session.getTotalQuestions() == null ? 0 : session.getTotalQuestions(),
                    session.getCorrectAnswers() == null ? 0 : session.getCorrectAnswers(),
                    session.getAccuracy() == null ? 0.0 : session.getAccuracy(),
                    session.getStartedAt(),
                    session.getEndedAt()
            );
        }
    }
}
