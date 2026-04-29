package com.studymedical.backend.infrastructure.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.application.usecases.user.UserService;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.dto.response.AuthResponse;
import com.studymedical.backend.infrastructure.services.FirebaseAuthService;
import com.studymedical.backend.infrastructure.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final FirebaseAuthService firebaseAuthService;
    private final UserService userService;
    private final JwtService jwtService;
    private final CreateUserUseCase createUserUseCase;

    @Value("${app.dev-mode:false}")
    private boolean devMode;

    @PostMapping("/sync-session")
    public ResponseEntity<AuthResponse> syncSession(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String firebaseToken = authHeader.substring(7);
        FirebaseToken decoded = firebaseAuthService.verify(firebaseToken);

        User user = userService.getOrCreate(decoded);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getAuthId());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getAuthId());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        try {
            UUID userId = jwtService.extractUserId(token);
            UUID authId = jwtService.extractAuthId(token);
            
            String newAccessToken = jwtService.generateAccessToken(userId, authId);
            String newRefreshToken = jwtService.generateRefreshToken(userId, authId);

            return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/dev-login")
    public ResponseEntity<?> devLogin(@RequestBody Map<String, String> request) {
        if (!devMode) {
            return ResponseEntity.status(403).body(Map.of("error", "Dev login only available in dev mode"));
        }

        String requestedUserId = request.getOrDefault("userId", UUID.randomUUID().toString());
        UUID normalizedAuthId = createUserUseCase.normalizeAuthId(requestedUserId);

        String email = request.getOrDefault("email", "dev@example.com");
        String name = request.getOrDefault("name", "Dev User");

        User user = createUserUseCase.execute(
                new AuthUserPayload(normalizedAuthId.toString(), email, name, null)
        );

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getAuthId());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getAuthId());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}
