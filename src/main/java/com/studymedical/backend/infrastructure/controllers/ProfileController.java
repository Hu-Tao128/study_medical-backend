package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.application.usecases.user.UpdateProfileUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.dto.response.ProfileResponseDto;
import com.studymedical.backend.infrastructure.security.AuthTokenPayloadExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final AuthTokenPayloadExtractor tokenPayloadExtractor;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            AuthUserPayload payload = tokenPayloadExtractor.extract(jwt, authHeader);
            User user = createUserUseCase.execute(payload);
            ProfileResponseDto response = new ProfileResponseDto(
                    user.getId(),
                    user.getAuthId(),
                    user.getEmail(),
                    user.getDisplayName() != null ? user.getDisplayName() : "",
                    user.getPhotoUrl() != null ? user.getPhotoUrl() : "",
                    user.getRole().name(),
                    user.getInstitution() != null ? user.getInstitution().getId() : null,
                    user.getPreferredLanguage(),
                    user.getTheme(),
                    user.getLevel(),
                    user.getSemester(),
                    user.getCareer(),
                    user.getLastLoginAt(),
                    user.getLastActiveAt(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalido"));
        }
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UpdateProfileRequest request
    ) {
        try {
            AuthUserPayload payload = tokenPayloadExtractor.extract(jwt, authHeader);
            UUID authId = createUserUseCase.normalizeAuthId(payload.subject());

            return updateProfileUseCase.execute(
                            authId,
                            new UpdateProfileUseCase.UpdateProfileCommand(
                                    request.displayName(),
                                    request.photoUrl(),
                                    request.preferredLanguage(),
                                    request.theme(),
                                    request.level(),
                                    request.semester(),
                                    request.career()
                            )
                    )
                    .map(user -> {
                        ProfileResponseDto response = new ProfileResponseDto(
                                user.getId(),
                                user.getAuthId(),
                                user.getEmail(),
                                user.getDisplayName() != null ? user.getDisplayName() : "",
                                user.getPhotoUrl() != null ? user.getPhotoUrl() : "",
                                user.getRole().name(),
                                user.getInstitution() != null ? user.getInstitution().getId() : null,
                                user.getPreferredLanguage(),
                                user.getTheme(),
                                user.getLevel(),
                                user.getSemester(),
                                user.getCareer(),
                                user.getLastLoginAt(),
                                user.getLastActiveAt(),
                                user.getCreatedAt(),
                                user.getUpdatedAt()
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado")));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalido"));
        }
    }

    public record UpdateProfileRequest(
            String displayName,
            String photoUrl,
            String preferredLanguage,
            String theme,
            String level,
            Integer semester,
            String career
    ) {
    }
}
