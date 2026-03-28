package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.ClinicalCase;
import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.mongo.ClinicalCaseMongoRepository;
import com.studymedical.backend.infrastructure.dto.response.ClinicalCaseResponseDto;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class ClinicalCaseController {

    private final ClinicalCaseMongoRepository clinicalCaseMongoRepository;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping
    public ResponseEntity<ClinicalCaseResponseDto> createCase(
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
            roleAuthorizationService.requireGroupTeacherOrAdmin(currentUser, request.groupId());
        }

        clinicalCase.initializeDefaults();
        ClinicalCase created = clinicalCaseMongoRepository.save(clinicalCase);
        return ResponseEntity.ok(ClinicalCaseResponseDto.fromEntity(created));
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<ClinicalCaseResponseDto>> getByTopic(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID topicId
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        List<ClinicalCaseResponseDto> filtered = clinicalCaseMongoRepository.findByTopicId(topicId).stream()
                .filter(clinicalCase -> roleAuthorizationService.canReadByVisibility(
                        currentUser,
                        clinicalCase.getCreatedBy(),
                        toSecurityVisibility(clinicalCase.getVisibility()),
                        clinicalCase.getGroupId()
                ))
                .map(ClinicalCaseResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicalCaseResponseDto> getById(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        ClinicalCase clinicalCase = clinicalCaseMongoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Caso clinico no encontrado"));

        boolean canRead = roleAuthorizationService.canReadByVisibility(
                currentUser,
                clinicalCase.getCreatedBy(),
                toSecurityVisibility(clinicalCase.getVisibility()),
                clinicalCase.getGroupId()
        );

        if (!canRead) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Sin acceso a este caso");
        }

        return ResponseEntity.ok(ClinicalCaseResponseDto.fromEntity(clinicalCase));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicalCaseResponseDto> updateCase(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @Valid @RequestBody CreateClinicalCaseRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);
        roleAuthorizationService.requireAnyRole(currentUser, User.Role.TEACHER, User.Role.ADMIN);

        ClinicalCase existing = clinicalCaseMongoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Caso clinico no encontrado"));

        roleAuthorizationService.requireSelfOrRole(currentUser, existing.getCreatedBy(), User.Role.ADMIN);

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

        existing.setTitle(request.title());
        existing.setDescription(request.description());
        existing.setSymptoms(request.symptoms());
        existing.setDiagnosis(request.diagnosis());
        existing.setQuestions(questions);
        existing.setTopicId(request.topicId());
        existing.setDifficulty(request.difficulty());
        existing.setVisibility(request.visibility());
        existing.setGroupId(request.groupId());
        existing.setAiGenerated(request.aiGenerated() != null && request.aiGenerated());
        existing.setAiModel(request.aiModel());
        existing.setAiSource(request.aiSource());
        existing.setAiEmbeddingsId(request.aiEmbeddingsId());
        existing.initializeDefaults();

        ClinicalCase updated = clinicalCaseMongoRepository.save(existing);
        return ResponseEntity.ok(ClinicalCaseResponseDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(jwt, authHeader);

        ClinicalCase existing = clinicalCaseMongoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Caso clinico no encontrado"));

        roleAuthorizationService.requireSelfOrRole(currentUser, existing.getCreatedBy(), User.Role.ADMIN);
        clinicalCaseMongoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
