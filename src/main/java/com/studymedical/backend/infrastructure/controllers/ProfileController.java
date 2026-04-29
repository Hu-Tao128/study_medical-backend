package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.user.GetProfileUseCase;
import com.studymedical.backend.application.usecases.user.UpdateProfileUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.dto.response.ProfileResponseDto;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        return getProfileUseCase.execute(principal.authId())
                .<ResponseEntity<?>>map(user -> {
                    ProfileResponseDto response = mapToResponse(user);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado")));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody UpdateProfileRequest request
    ) {
        return updateProfileUseCase.execute(
                        principal.authId(),
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
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado")));
    }

    private ProfileResponseDto mapToResponse(User user) {
        return new ProfileResponseDto(
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
