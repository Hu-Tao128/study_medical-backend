package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.services.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class DevAuthController {

    private final UserService userService;
    private final String jwtSecret;
    private final long jwtExpiration;
    private final boolean devMode;

    public DevAuthController(
            UserService userService,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpiration,
            @Value("${app.dev-mode:false}") boolean devMode) {
        this.userService = userService;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.devMode = devMode;
    }

    @PostMapping("/dev-login")
    public ResponseEntity<?> devLogin(@RequestBody Map<String, String> request) {
        if (!devMode) {
            return ResponseEntity.status(403).body(Map.of("error", "Dev login only available in dev mode"));
        }

        String userId = request.getOrDefault("userId", "dev-user-" + System.currentTimeMillis());
        String email = request.getOrDefault("email", "dev@example.com");
        String name = request.getOrDefault("name", "Dev User");

        userService.createOrUpdateUser(userId, email, name, null);

        String token = generateToken(userId, email);

        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresIn", jwtExpiration / 1000,
                "user", Map.of(
                        "id", userId,
                        "email", email,
                        "displayName", name,
                        "role", "STUDENT"
                )
        ));
    }

    private String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
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
