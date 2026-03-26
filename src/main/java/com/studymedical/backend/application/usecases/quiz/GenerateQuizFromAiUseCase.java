package com.studymedical.backend.application.usecases.quiz;

import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
import org.springframework.stereotype.Service;

@Service
public class GenerateQuizFromAiUseCase {

    private final QuizMongoRepository quizMongoRepository;

    public GenerateQuizFromAiUseCase(QuizMongoRepository quizMongoRepository) {
        this.quizMongoRepository = quizMongoRepository;
    }

    public Quiz execute(Quiz quizDraft) {
        quizDraft.setAiGenerated(true);
        quizDraft.initializeDefaults();
        if (quizDraft.getQuestions() != null) {
            quizDraft.getQuestions().forEach(question -> {
                if (question != null && !question.isAiGenerated()) {
                    question.setAiGenerated(true);
                }
            });
        }
        return quizMongoRepository.save(quizDraft);
    }
}
