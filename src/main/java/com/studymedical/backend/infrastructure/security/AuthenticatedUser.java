package com.studymedical.backend.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        UUID authId
) {
}
