package com.studymedical.backend.application.usecases.auth;

public record AuthUserPayload(
        String subject,
        String email,
        String displayName,
        String photoUrl
) {
}
