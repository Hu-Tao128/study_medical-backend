package com.studymedical.backend.application.usecases.quiz;

import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserProgress;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserProgressRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitQuizUseCaseTest {

    @Mock
    private QuizMongoRepository quizMongoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    private SubmitQuizUseCase submitQuizUseCase;

    @BeforeEach
    void setUp() {
        submitQuizUseCase = new SubmitQuizUseCase(
                quizMongoRepository,
                userRepository,
                topicRepository,
                userProgressRepository
        );
    }

    @Test
    void shouldCalculateScoreAndUpdateProgress() {
        UUID userId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        String quizId = "quiz-1";

        Quiz.QuizQuestion q1 = new Quiz.QuizQuestion("Q1", List.of("A", "B"), 0, "E1", false);
        Quiz.QuizQuestion q2 = new Quiz.QuizQuestion("Q2", List.of("A", "B"), 1, "E2", false);
        Quiz quiz = new Quiz(quizId, "Quiz", topicId, userId, List.of(q1, q2), Quiz.Visibility.PRIVATE, false, null, null, null, null);

        User user = User.builder().id(userId).authId(UUID.randomUUID()).email("u@test.com").role(User.Role.STUDENT).build();
        Topic topic = Topic.builder().id(topicId).name("Anatomia").build();

        when(quizMongoRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(userProgressRepository.findByUserAndTopic(user, topic)).thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmitQuizUseCase.SubmitQuizResult result = submitQuizUseCase.execute(userId, quizId, List.of(0, 1));

        assertEquals(2, result.totalQuestions());
        assertEquals(2, result.correctAnswers());
        assertEquals(1.0, result.score());

        ArgumentCaptor<UserProgress> captor = ArgumentCaptor.forClass(UserProgress.class);
        verify(userProgressRepository).save(captor.capture());

        UserProgress saved = captor.getValue();
        assertEquals(1, saved.getAttempts());
        assertEquals(1.0, saved.getAccuracy());
        assertEquals(1.0, saved.getLastScore());
        assertNotNull(saved.getLastStudiedAt());
    }
}
