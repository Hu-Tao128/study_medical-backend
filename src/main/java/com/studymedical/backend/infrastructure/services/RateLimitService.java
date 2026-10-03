package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.domain.entities.Institution;
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

        int limit = isPremium(user) ? 10 : 3; // requests per minute

        Bucket bucket = buckets.computeIfAbsent(userId,
            id -> Bucket.builder()
                    .addLimit(Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1))))
                    .build());

        return bucket.tryConsume(1);
    }

    public boolean isPremium(UUID userId) {
        return userRepository.findById(userId).map(this::isPremium).orElse(false);
    }

    /**
     * Un usuario es premium si pertenece a una institución con plan PRO o ENTERPRISE.
     * El plan vive en {@link Institution} (valores FREE/PRO/ENTERPRISE).
     */
    private boolean isPremium(User user) {
        Institution institution = user.getInstitution();
        if (institution == null || institution.getPlan() == null) {
            return false;
        }
        return institution.getPlan() != Institution.Plan.FREE;
    }
}
