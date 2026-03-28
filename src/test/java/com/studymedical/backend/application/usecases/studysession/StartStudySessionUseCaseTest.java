package com.studymedical.backend.application.usecases.studysession;

import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.entities.StudySession;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.StudySessionRepository;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.domain.repositories.mongo.FlashcardMongoRepository;
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
class StartStudySessionUseCaseTest {

    @Mock
    private FlashcardMongoRepository flashcardMongoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

    private StartStudySessionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new StartStudySessionUseCase(
                flashcardMongoRepository,
                userRepository,
                topicRepository,
                studySessionRepository
        );
    }

    @Test
    void shouldStartSessionWithFlashcards() {
        UUID userId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Topic topic = Topic.builder().id(topicId).build();

        Flashcard card = Flashcard.builder()
                .id("card-1")
                .topicId(topicId)
                .question("Pregunta")
                .tags(List.of("neuro"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(flashcardMongoRepository.findByTopicId(topicId)).thenReturn(List.of(card));
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> {
            StudySession session = invocation.getArgument(0);
            session.setId(sessionId);
            return session;
        });

        StartStudySessionUseCase.StartStudySessionResult result = useCase.execute(userId, topicId, "FLASHCARDS", 20);

        assertEquals(sessionId, result.sessionId());
        assertEquals(1, result.cards().size());
        assertEquals("card-1", result.cards().get(0).cardId());
    }
}
