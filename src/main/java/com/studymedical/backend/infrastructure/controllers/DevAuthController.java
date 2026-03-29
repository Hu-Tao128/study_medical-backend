package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import com.studymedical.backend.application.usecases.user.CreateUserUseCase;
import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.infrastructure.security.AuthTokenPayloadExtractor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class DevAuthController {

    private static final Logger log = LoggerFactory.getLogger(DevAuthController.class);

    private final CreateUserUseCase createUserUseCase;
    private final AuthTokenPayloadExtractor tokenPayloadExtractor;
    private final String jwtSecret;
    private final long jwtExpiration;
    private final boolean devMode;

    public DevAuthController(
            CreateUserUseCase createUserUseCase,
            AuthTokenPayloadExtractor tokenPayloadExtractor,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpiration,
            @Value("${app.dev-mode:false}") boolean devMode) {
        this.createUserUseCase = createUserUseCase;
        this.tokenPayloadExtractor = tokenPayloadExtractor;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.devMode = devMode;
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

        String token = generateToken(user.getAuthId().toString(), user.getEmail(), user.getDisplayName());

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("accessToken", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtExpiration / 1000);
        
        Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("authId", user.getAuthId());
        userMap.put("email", user.getEmail());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("role", user.getRole().name());
        response.put("user", userMap);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync-session")
    public ResponseEntity<?> syncSession(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Authorization header requerido"));
        }

        try {
            AuthUserPayload payload = tokenPayloadExtractor.extract(jwt, authHeader);
            User user = createUserUseCase.execute(payload);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", user.getId());
            response.put("authId", user.getAuthId());
            response.put("email", user.getEmail());
            response.put("displayName", user.getDisplayName());
            response.put("role", user.getRole().name());
            response.put("photoUrl", user.getPhotoUrl());
            response.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing session with backend", e);
            if (devMode) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "Token invalido");
                errorResponse.put("detail", e.getMessage());
                return ResponseEntity.status(401).body(errorResponse);
            }
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Token invalido"));
        }
    }

    private String generateToken(String authId, String email, String name) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(authId)
                .claim("email", email)
                .claim("name", name)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
