package com.studymedical.backend.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymedical.backend.application.usecases.auth.AuthUserPayload;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class AuthTokenPayloadExtractor {

    private final ObjectMapper objectMapper;

    public AuthTokenPayloadExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthUserPayload extract(Jwt jwt, String authorizationHeader) {
        if (jwt != null) {
            return new AuthUserPayload(
                    jwt.getSubject(),
                    firstNonBlank((String) jwt.getClaims().get("email")),
                    firstNonBlank(
                            (String) jwt.getClaims().get("name"),
                            (String) jwt.getClaims().get("display_name")
                    ),
                    firstNonBlank((String) jwt.getClaims().get("picture"))
            );
        }

        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            throw new IllegalArgumentException("Authorization header ausente o invalido");
        }

        Map<String, Object> claims = decodePayload(token);

        return new AuthUserPayload(
                firstNonBlank((String) claims.get("sub")),
                firstNonBlank((String) claims.get("email")),
                firstNonBlank((String) claims.get("name"), (String) claims.get("display_name")),
                firstNonBlank((String) claims.get("picture"), (String) claims.get("photo_url"))
        );
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    private Map<String, Object> decodePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Token JWT invalido");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            return objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("No fue posible leer el payload del token", ex);
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
