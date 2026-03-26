package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.application.usecases.user.UpdateProfileUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.security.AuthTokenPayloadExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.Map;

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
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", user.getId());
            response.put("authId", user.getAuthId());
            response.put("email", user.getEmail());
            response.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "");
            response.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl() : "");
            response.put("role", user.getRole().name());
            response.put("institutionId", user.getInstitution() != null ? user.getInstitution().getId() : null);
            response.put("preferredLanguage", user.getPreferredLanguage());
            response.put("theme", user.getTheme());
            response.put("level", user.getLevel());
            response.put("semester", user.getSemester());
            response.put("career", user.getCareer());
            response.put("lastLoginAt", user.getLastLoginAt());
            response.put("lastActiveAt", user.getLastActiveAt());
            response.put("createdAt", user.getCreatedAt());
            response.put("updatedAt", user.getUpdatedAt());
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
                    .map(user -> ResponseEntity.ok(Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                            "photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl() : "",
                            "preferredLanguage", user.getPreferredLanguage(),
                            "theme", user.getTheme(),
                            "level", user.getLevel(),
                            "semester", user.getSemester(),
                            "career", user.getCareer(),
                            "updatedAt", user.getUpdatedAt()
                    )))
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
