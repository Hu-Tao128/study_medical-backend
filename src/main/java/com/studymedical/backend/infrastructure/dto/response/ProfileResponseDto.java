package com.studymedical.backend.infrastructure.dto.response;

import java.time.LocalDateTime;
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
        String level,
        Integer semester,
        String career,
        LocalDateTime lastLoginAt,
        LocalDateTime lastActiveAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
