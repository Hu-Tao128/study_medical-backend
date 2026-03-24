package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String token = authHeader.substring(7);
        
        try {
            User user = userService.getOrCreateFromToken(token);
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                    "photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl() : "",
                    "role", user.getRole().name(),
                    "createdAt", user.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}
