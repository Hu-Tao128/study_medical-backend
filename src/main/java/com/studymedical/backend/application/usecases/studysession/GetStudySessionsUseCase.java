package com.studymedical.backend.application.usecases.studysession;

import com.studymedical.backend.domain.entities.StudySession;
import com.studymedical.backend.domain.repositories.StudySessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetStudySessionsUseCase {

    private final StudySessionRepository studySessionRepository;

    public GetStudySessionsUseCase(StudySessionRepository studySessionRepository) {
        this.studySessionRepository = studySessionRepository;
    }

    public List<StudySession> execute(UUID userId, UUID topicId) {
        if (topicId == null) {
            return studySessionRepository.findByUser_IdOrderByStartedAtDesc(userId);
        }
        return studySessionRepository.findByUser_IdAndTopic_IdOrderByStartedAtDesc(userId, topicId);
    }
}
