package com.studymedical.backend.application.usecases.user;

import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User execute(AuthUserPayload payload) {
        UUID authId = normalizeAuthId(payload.subject());

        User user = userRepository.findByAuthId(authId)
                .orElseGet(() -> User.builder()
                        .authId(authId)
                        .role(User.Role.STUDENT)
                        .build());

        user.setEmail(resolveEmail(payload));
        user.setDisplayName(payload.displayName());
        user.setPhotoUrl(payload.photoUrl());
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public UUID normalizeAuthId(String subject) {
        if (subject == null || subject.isBlank()) {
            return UUID.randomUUID();
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String resolveEmail(AuthUserPayload payload) {
        if (payload.email() != null && !payload.email().isBlank()) {
            return payload.email();
        }
        return "user-" + normalizeAuthId(payload.subject()) + "@firebase.local";
    }
}
