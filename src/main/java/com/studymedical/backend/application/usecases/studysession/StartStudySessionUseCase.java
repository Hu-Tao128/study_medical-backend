package com.studymedical.backend.application.usecases.studysession;

import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.entities.StudySession;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.StudySessionRepository;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import com.studymedical.backend.domain.repositories.mongo.FlashcardMongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StartStudySessionUseCase {

    private final FlashcardMongoRepository flashcardMongoRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final StudySessionRepository studySessionRepository;

    public StartStudySessionUseCase(
            FlashcardMongoRepository flashcardMongoRepository,
            UserRepository userRepository,
            TopicRepository topicRepository,
            StudySessionRepository studySessionRepository
    ) {
        this.flashcardMongoRepository = flashcardMongoRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.studySessionRepository = studySessionRepository;
    }

    @Transactional
    public StartStudySessionResult execute(UUID userId, UUID topicId, String mode, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic no encontrado"));

        String normalizedMode = normalizeMode(mode);

        if (!"FLASHCARDS".equals(normalizedMode)) {
            throw new IllegalArgumentException("Modo no soportado para start: " + normalizedMode);
        }

        int effectiveLimit = Math.max(1, Math.min(limit, 100));

        List<StudyCard> cards = flashcardMongoRepository.findByTopicId(topicId).stream()
                .limit(effectiveLimit)
                .map(card -> new StudyCard(card.getId(), card.getQuestion(), card.getTags()))
                .toList();

        StudySession session = StudySession.builder()
                .user(user)
                .topic(topic)
                .mode(normalizedMode.toLowerCase())
                .totalQuestions(cards.size())
                .build();

        StudySession saved = studySessionRepository.save(session);

        return new StartStudySessionResult(saved.getId(), cards);
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "FLASHCARDS";
        }
        return mode.trim().toUpperCase();
    }

    public record StartStudySessionResult(UUID sessionId, List<StudyCard> cards) {
    }

    public record StudyCard(String cardId, String question, List<String> tags) {
    }
}
