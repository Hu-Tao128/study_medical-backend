package com.studymedical.backend.infrastructure.dto.response;

import com.studymedical.backend.domain.entities.Quiz;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizResponseDto(
        String id,
        String title,
        UUID topicId,
        UUID createdBy,
        List<QuizQuestionDto> questions,
        String visibility,
        UUID groupId,
        boolean aiGenerated,
        String aiModel,
        String aiSource,
        String aiEmbeddingsId,
        Instant createdAt
) {
    public static QuizResponseDto fromEntity(Quiz quiz) {
        return new QuizResponseDto(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTopicId(),
                quiz.getCreatedBy(),
                quiz.getQuestions() != null 
                        ? quiz.getQuestions().stream()
                                .map(QuizQuestionDto::fromEntity)
                                .toList() 
                        : List.of(),
                quiz.getVisibility() != null ? quiz.getVisibility().name() : "PRIVATE",
                quiz.getGroupId(),
                quiz.isAiGenerated(),
                quiz.getAiModel(),
                quiz.getAiSource(),
                quiz.getAiEmbeddingsId(),
                quiz.getCreatedAt()
        );
    }

    public record QuizQuestionDto(
            String question,
            List<String> options,
            Integer correctAnswer,
            String explanation,
            boolean aiGenerated
    ) {
        public static QuizQuestionDto fromEntity(Quiz.QuizQuestion q) {
            return new QuizQuestionDto(
                    q.getQuestion(),
                    q.getOptions(),
                    q.getCorrectAnswer(),
                    q.getExplanation(),
                    q.isAiGenerated()
            );
        }
    }
}
