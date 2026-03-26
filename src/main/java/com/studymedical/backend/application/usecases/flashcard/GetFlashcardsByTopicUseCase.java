package com.studymedical.backend.application.usecases.flashcard;

import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.repositories.mongo.FlashcardMongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetFlashcardsByTopicUseCase {

    private final FlashcardMongoRepository flashcardMongoRepository;

    public GetFlashcardsByTopicUseCase(FlashcardMongoRepository flashcardMongoRepository) {
        this.flashcardMongoRepository = flashcardMongoRepository;
    }

    public List<Flashcard> execute(UUID topicId) {
        return flashcardMongoRepository.findByTopicId(topicId);
    }
}
