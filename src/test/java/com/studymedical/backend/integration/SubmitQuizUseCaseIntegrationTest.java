package com.studymedical.backend.integration;

import com.studymedical.backend.application.usecases.quiz.SubmitQuizUseCase;
import com.studymedical.backend.domain.entities.Quiz;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserProgress;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserProgressRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.domain.repositories.mongo.QuizMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SubmitQuizUseCaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("app.dev-mode", () -> "true");
    }

    @Autowired
    private SubmitQuizUseCase submitQuizUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuizMongoRepository quizMongoRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Test
    void shouldPersistProgressAfterSubmitQuiz() {
        User user = userRepository.save(User.builder()
                .authId(UUID.randomUUID())
                .email("integration-user@test.com")
                .role(User.Role.STUDENT)
                .build());

        Topic topic = topicRepository.save(Topic.builder().name("Cardiologia").build());

        Quiz.QuizQuestion q1 = Quiz.QuizQuestion.builder()
                .question("q1")
                .options(List.of("a", "b"))
                .correctAnswer(0)
                .build();

        Quiz.QuizQuestion q2 = Quiz.QuizQuestion.builder()
                .question("q2")
                .options(List.of("a", "b"))
                .correctAnswer(1)
                .build();

        Quiz quiz = Quiz.builder()
                .title("Quiz Integracion")
                .topicId(topic.getId())
                .createdBy(user.getId())
                .visibility(Quiz.Visibility.PRIVATE)
                .questions(List.of(q1, q2))
                .build();

        quiz = quizMongoRepository.save(quiz);

        SubmitQuizUseCase.SubmitQuizResult result = submitQuizUseCase.execute(user.getId(), quiz.getId(), List.of(0, 1));

        assertEquals(2, result.totalQuestions());
        assertEquals(2, result.correctAnswers());
        assertEquals(1.0, result.score());

        List<UserProgress> progress = userProgressRepository.findByUser_Id(user.getId());
        assertEquals(1, progress.size());
        assertTrue(progress.get(0).getAccuracy() >= 1.0);
    }
}
