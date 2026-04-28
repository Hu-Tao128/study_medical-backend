package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final UserRepository userRepository;
    private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        boolean isPremium = user.getPlan() != null && user.getPlan().equals("premium");
        int limit = isPremium ? 10 : 3; // requests per minute

        Bucket bucket = buckets.computeIfAbsent(userId,
            id -> Bucket.builder()
                    .addLimit(Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1))))
                    .build());

        return bucket.tryConsume(1);
    }

    public boolean isPremium(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getPlan() != null && user.getPlan().equals("premium");
    }
}
