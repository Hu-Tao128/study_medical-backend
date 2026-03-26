package com.studymedical.backend.infrastructure.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProfileResponseDto(
        UUID id,
        UUID authId,
        String email,
        String displayName,
        String photoUrl,
        String role,
        UUID institutionId,
        String preferredLanguage,
        String theme,
        Integer level,
        Integer semester,
        String career,
        Instant lastLoginAt,
        Instant lastActiveAt,
        Instant createdAt,
        Instant updatedAt
) {
}
