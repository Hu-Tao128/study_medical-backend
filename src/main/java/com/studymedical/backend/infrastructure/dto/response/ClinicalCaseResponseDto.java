package com.studymedical.backend.infrastructure.dto.response;

import com.studymedical.backend.domain.entities.ClinicalCase;
import com.studymedical.backend.domain.entities.Flashcard;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClinicalCaseResponseDto(
        String id,
        String title,
        String description,
        List<String> symptoms,
        String diagnosis,
        List<CaseQuestionDto> questions,
        UUID topicId,
        UUID createdBy,
        String difficulty,
        String visibility,
        UUID groupId,
        boolean aiGenerated,
        String aiModel,
        String aiSource,
        String aiEmbeddingsId,
        Instant createdAt
) {
    public static ClinicalCaseResponseDto fromEntity(ClinicalCase clinicalCase) {
        return new ClinicalCaseResponseDto(
                clinicalCase.getId(),
                clinicalCase.getTitle(),
                clinicalCase.getDescription(),
                clinicalCase.getSymptoms(),
                clinicalCase.getDiagnosis(),
                clinicalCase.getQuestions() != null
                        ? clinicalCase.getQuestions().stream()
                                .map(CaseQuestionDto::fromEntity)
                                .toList()
                        : List.of(),
                clinicalCase.getTopicId(),
                clinicalCase.getCreatedBy(),
                clinicalCase.getDifficulty() != null ? clinicalCase.getDifficulty().name() : "MEDIUM",
                clinicalCase.getVisibility() != null ? clinicalCase.getVisibility().name() : "PRIVATE",
                clinicalCase.getGroupId(),
                clinicalCase.isAiGenerated(),
                clinicalCase.getAiModel(),
                clinicalCase.getAiSource(),
                clinicalCase.getAiEmbeddingsId(),
                clinicalCase.getCreatedAt()
        );
    }

    public record CaseQuestionDto(
            String question,
            List<String> options,
            Integer correctAnswer,
            String explanation
    ) {
        public static CaseQuestionDto fromEntity(ClinicalCase.CaseQuestion q) {
            return new CaseQuestionDto(
                    q.getQuestion(),
                    q.getOptions(),
                    q.getCorrectAnswer(),
                    q.getExplanation()
            );
        }
    }
}
