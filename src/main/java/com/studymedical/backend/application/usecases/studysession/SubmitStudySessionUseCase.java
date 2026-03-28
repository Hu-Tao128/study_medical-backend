package com.studymedical.backend.application.usecases.studysession;

import com.studymedical.backend.domain.entities.StudySession;
import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserProgress;
import com.studymedical.backend.domain.repositories.StudySessionRepository;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubmitStudySessionUseCase {

    private final StudySessionRepository studySessionRepository;
    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;

    public SubmitStudySessionUseCase(
            StudySessionRepository studySessionRepository,
            TopicRepository topicRepository,
            UserProgressRepository userProgressRepository
    ) {
        this.studySessionRepository = studySessionRepository;
        this.topicRepository = topicRepository;
        this.userProgressRepository = userProgressRepository;
    }

    @Transactional
    public SubmitStudySessionResult execute(UUID userId, UUID sessionId, UUID topicId, List<Attempt> attempts) {
        StudySession session = studySessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic no encontrado"));

        if (!session.getTopic().getId().equals(topic.getId())) {
            throw new IllegalArgumentException("topicId no coincide con la sesion");
        }

        int total = attempts == null ? 0 : attempts.size();
        int correctCount = attempts == null ? 0 : (int) attempts.stream().filter(Attempt::correct).count();
        double accuracy = total == 0 ? 0.0 : (double) correctCount / total;
        int timeSpentMinutes = attempts == null ? 0 : (int) (attempts.stream()
                .mapToLong(attempt -> Math.max(0L, attempt.timeMs()))
                .sum() / 60000);

        session.setTotalQuestions(total);
        session.setCorrectAnswers(correctCount);
        session.setAccuracy(accuracy);
        session.setEndedAt(LocalDateTime.now());
        studySessionRepository.save(session);

        UserProgress progress = userProgressRepository.findByUserAndTopic(session.getUser(), topic)
                .orElseGet(() -> UserProgress.builder().user(session.getUser()).topic(topic).build());

        int previousAttempts = progress.getAttempts() == null ? 0 : progress.getAttempts();
        double previousAccuracy = progress.getAccuracy() == null ? 0.0 : progress.getAccuracy();
        int newAttempts = previousAttempts + total;

        double newAccuracy = newAttempts == 0
                ? 0.0
                : ((previousAccuracy * previousAttempts) + correctCount) / newAttempts;

        progress.setAttempts(newAttempts);
        progress.setAccuracy(newAccuracy);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousStudiedAt = progress.getLastStudiedAt();

        progress.setLastScore(accuracy);
        progress.setLastStudiedAt(now);

        int previousMinutes = progress.getTimeSpentMinutes() == null ? 0 : progress.getTimeSpentMinutes();
        progress.setTimeSpentMinutes(previousMinutes + timeSpentMinutes);

        progress.setStreakDays(calculateStreak(progress.getStreakDays(), previousStudiedAt, now));

        userProgressRepository.save(progress);

        return new SubmitStudySessionResult(accuracy, correctCount, total);
    }

    private int calculateStreak(Integer currentStreak, LocalDateTime previousStudiedAt, LocalDateTime now) {
        int streak = currentStreak == null ? 0 : currentStreak;
        if (previousStudiedAt == null) {
            return 1;
        }

        LocalDate previousDate = previousStudiedAt.toLocalDate();
        LocalDate today = now.toLocalDate();

        if (previousDate.equals(today)) {
            return Math.max(1, streak);
        }

        if (previousDate.equals(today.minusDays(1))) {
            return Math.max(1, streak) + 1;
        }

        return 1;
    }

    public record Attempt(String cardId, Integer difficulty, boolean correct, long timeMs) {
    }

    public record SubmitStudySessionResult(double accuracy, int correctCount, int total) {
    }
}
