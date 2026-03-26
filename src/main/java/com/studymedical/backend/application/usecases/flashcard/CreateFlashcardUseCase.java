package com.studymedical.backend.application.usecases.flashcard;

import com.studymedical.backend.domain.entities.Flashcard;
import com.studymedical.backend.domain.repositories.mongo.FlashcardMongoRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateFlashcardUseCase {

    private final FlashcardMongoRepository flashcardMongoRepository;

    public CreateFlashcardUseCase(FlashcardMongoRepository flashcardMongoRepository) {
        this.flashcardMongoRepository = flashcardMongoRepository;
    }

    public Flashcard execute(Flashcard flashcard) {
        flashcard.initializeDefaults();
        return flashcardMongoRepository.save(flashcard);
    }
}
