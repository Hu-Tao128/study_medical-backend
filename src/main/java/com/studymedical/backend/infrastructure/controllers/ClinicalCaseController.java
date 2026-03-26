package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.ClinicalCase;
import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.ClinicalCaseMongoRepository;
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
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class ClinicalCaseController {

    private final ClinicalCaseMongoRepository clinicalCaseMongoRepository;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping
    public ResponseEntity<ClinicalCase> createCase(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateClinicalCaseRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.TEACHER, User.Role.ADMIN);

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
                .createdBy(currentUser.getId())
                .difficulty(request.difficulty())
                .visibility(request.visibility())
                .groupId(request.groupId())
                .aiGenerated(request.aiGenerated() != null && request.aiGenerated())
                .aiModel(request.aiModel())
                .aiSource(request.aiSource())
                .aiEmbeddingsId(request.aiEmbeddingsId())
                .build();

        if (request.visibility() == ClinicalCase.Visibility.GROUP) {
            roleAuthorizationService.requireGroupAccess(currentUser, request.groupId());
        }

        clinicalCase.initializeDefaults();
        return ResponseEntity.ok(clinicalCaseMongoRepository.save(clinicalCase));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ClinicalCase>> getByTopic(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        List<ClinicalCase> filtered = clinicalCaseMongoRepository.findByTopicId(topicId).stream()
                .filter(clinicalCase -> roleAuthorizationService.canReadByVisibility(
                        currentUser,
                        clinicalCase.getCreatedBy(),
                        toSecurityVisibility(clinicalCase.getVisibility()),
                        clinicalCase.getGroupId()
                ))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    public record CreateClinicalCaseRequest(
            @NotBlank String title,
            @NotBlank String description,
            List<String> symptoms,
            @NotBlank String diagnosis,
            List<CreateCaseQuestionRequest> questions,
            @NotNull UUID topicId,
            Flashcard.Difficulty difficulty,
            ClinicalCase.Visibility visibility,
            UUID groupId,
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

    private RoleAuthorizationService.QuizVisibility toSecurityVisibility(ClinicalCase.Visibility visibility) {
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
