package com.studymedical.backend.domain.repositories;

import com.studymedical.backend.domain.entities.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    Optional<StudySession> findByIdAndUser_Id(UUID id, UUID userId);

    List<StudySession> findByUser_IdOrderByStartedAtDesc(UUID userId);

    List<StudySession> findByUser_IdAndTopic_IdOrderByStartedAtDesc(UUID userId, UUID topicId);
}
