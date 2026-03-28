package com.studymedical.backend.infrastructure.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getmap() {
        return ResponseEntity.ok(Map.of(
                "service", "Study Medical Backend",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "health", "GET /api/v1/health",
                        "auth", "POST /api/v1/dev/auth",
                        "profile", "GET /api/v1/profile, PUT /api/v1/profile",
                        "quizzes", "GET /api/v1/quizzes, POST /api/v1/quizzes/generate, POST /api/v1/quizzes/submit",
                        "flashcards", "GET /api/v1/flashcards, POST /api/v1/flashcards",
                        "notes", "GET /api/v1/notes, POST /api/v1/notes, PUT /api/v1/notes/{id}, DELETE /api/v1/notes/{id}",
                        "clinicalCases", "GET /api/v1/clinical-cases, POST /api/v1/clinical-cases",
                        "chat", "GET /api/v1/chat/history, POST /api/v1/chat/send"
                )
        ));
    }

    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Study Medical Backend"
        ));
    }
}
