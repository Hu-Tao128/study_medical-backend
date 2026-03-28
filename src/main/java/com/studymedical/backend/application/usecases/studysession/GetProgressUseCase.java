package com.studymedical.backend.application.usecases.studysession;

import com.studymedical.backend.domain.entities.Topic;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserProgress;
import com.studymedical.backend.domain.repositories.TopicRepository;
import com.studymedical.backend.domain.repositories.UserProgressRepository;
import com.studymedical.backend.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GetProgressUseCase {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserProgressRepository userProgressRepository;

    public GetProgressUseCase(
            UserRepository userRepository,
            TopicRepository topicRepository,
            UserProgressRepository userProgressRepository
    ) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.userProgressRepository = userProgressRepository;
    }

    public ProgressResult execute(UUID userId, UUID topicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic no encontrado"));

        UserProgress progress = userProgressRepository.findByUserAndTopic(user, topic)
                .orElseGet(() -> UserProgress.builder().user(user).topic(topic).build());

        return new ProgressResult(
                progress.getAccuracy() == null ? 0.0 : progress.getAccuracy(),
                progress.getAttempts() == null ? 0 : progress.getAttempts(),
                progress.getLastStudiedAt()
        );
    }

    public RadarResult executeRadar(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<RadarTopic> topics = userProgressRepository.findByUser_Id(user.getId()).stream()
                .map(progress -> new RadarTopic(
                        progress.getTopic() != null ? progress.getTopic().getId() : null,
                        progress.getTopic() != null ? progress.getTopic().getName() : "",
                        progress.getAccuracy() == null ? 0.0 : progress.getAccuracy()
                ))
                .toList();

        return new RadarResult(topics);
    }

    public record ProgressResult(double accuracy, int attempts, LocalDateTime lastStudiedAt) {
    }

    public record RadarResult(List<RadarTopic> topics) {
    }

    public record RadarTopic(UUID topicId, String name, double accuracy) {
    }
}
