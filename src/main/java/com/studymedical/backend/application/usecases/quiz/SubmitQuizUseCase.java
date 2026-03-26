package com.studymedical.backend.application.usecases.quiz;

import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserProgress;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserProgressRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubmitQuizUseCase {

    private final QuizMongoRepository quizMongoRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;

    public SubmitQuizUseCase(
            QuizMongoRepository quizMongoRepository,
            UserRepository userRepository,
            TopicRepository topicRepository,
            UserProgressRepository userProgressRepository
    ) {
        this.quizMongoRepository = quizMongoRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.userProgressRepository = userProgressRepository;
    }

    @Transactional
    public SubmitQuizResult execute(UUID userId, String quizId, List<Integer> answers) {
        Quiz quiz = quizMongoRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz no encontrado"));

        int totalQuestions = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        int correctAnswers = 0;

        for (int i = 0; i < totalQuestions; i++) {
            Quiz.QuizQuestion question = quiz.getQuestions().get(i);
            Integer userAnswer = i < answers.size() ? answers.get(i) : null;
            if (question != null && question.getCorrectAnswer() != null && question.getCorrectAnswer().equals(userAnswer)) {
                correctAnswers++;
            }
        }

        double score = totalQuestions == 0 ? 0.0 : (double) correctAnswers / totalQuestions;
        updateProgressIfPossible(userId, quiz.getTopicId(), score);

        return new SubmitQuizResult(totalQuestions, correctAnswers, score);
    }

    private void updateProgressIfPossible(UUID userId, UUID topicId, double score) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Topic> topicOptional = topicRepository.findById(topicId);

        if (userOptional.isEmpty() || topicOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        Topic topic = topicOptional.get();

        UserProgress progress = userProgressRepository.findByUserAndTopic(user, topic)
                .orElseGet(() -> UserProgress.builder().user(user).topic(topic).build());

        int previousAttempts = progress.getAttempts() == null ? 0 : progress.getAttempts();
        double previousAccuracy = progress.getAccuracy() == null ? 0.0 : progress.getAccuracy();

        int newAttempts = previousAttempts + 1;
        double newAccuracy = ((previousAccuracy * previousAttempts) + score) / newAttempts;

        progress.setAttempts(newAttempts);
        progress.setAccuracy(newAccuracy);
        progress.setLastScore(score);
        progress.setLastStudiedAt(LocalDateTime.now());

        userProgressRepository.save(progress);
    }

    public record SubmitQuizResult(int totalQuestions, int correctAnswers, double score) {
    }
}
