package com.studymedical.backend.infrastructure.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${app.jwt.secret:default-secret-key-that-should-be-at-least-32-chars-long}") String secret,
            @Value("${app.jwt.access-token-expiration:900000}") long accessTokenExpiration, // 15 min
            @Value("${app.jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration // 7 days
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(UUID userId, UUID authId) {
        return generateToken(userId, authId, "ACCESS", accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId, UUID authId) {
        return generateToken(userId, authId, "REFRESH", refreshTokenExpiration);
    }

    private String generateToken(UUID userId, UUID authId, String type, long expiration) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("authId", authId.toString())
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public UUID extractAuthId(String token) {
        return UUID.fromString(parseToken(token).get("authId", String.class));
    }

    public String extractType(String token) {
        return parseToken(token).get("type", String.class);
    }
}
