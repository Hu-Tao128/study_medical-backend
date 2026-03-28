package com.studymedical.backend.application.usecases.studysession;

import com.studymedical.backend.domain.entities.StudySession;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserProgress;
import com.studymedical.backend.domain.repositories.StudySessionRepository;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitStudySessionUseCaseTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    private SubmitStudySessionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubmitStudySessionUseCase(studySessionRepository, topicRepository, userProgressRepository);
    }

    @Test
    void shouldSubmitSessionAndUpdateProgress() {
        UUID userId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Topic topic = Topic.builder().id(topicId).build();

        StudySession session = StudySession.builder()
                .id(sessionId)
                .user(user)
                .topic(topic)
                .mode("FLASHCARDS")
                .build();

        UserProgress progress = UserProgress.builder()
                .user(user)
                .topic(topic)
                .attempts(10)
                .accuracy(0.5)
                .build();

        when(studySessionRepository.findByIdAndUser_Id(sessionId, userId)).thenReturn(Optional.of(session));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(userProgressRepository.findByUserAndTopic(user, topic)).thenReturn(Optional.of(progress));
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SubmitStudySessionUseCase.Attempt> attempts = List.of(
                new SubmitStudySessionUseCase.Attempt("c1", 3, true, 10000),
                new SubmitStudySessionUseCase.Attempt("c2", 2, false, 20000)
        );

        SubmitStudySessionUseCase.SubmitStudySessionResult result = useCase.execute(userId, sessionId, topicId, attempts);

        assertEquals(2, result.total());
        assertEquals(1, result.correctCount());
        assertEquals(0.5, result.accuracy());
        assertEquals(12, progress.getAttempts());
    }
}
