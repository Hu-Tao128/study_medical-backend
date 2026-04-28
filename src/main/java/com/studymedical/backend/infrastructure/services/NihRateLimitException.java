package com.studymedical.backend.infrastructure.services;

public class NihRateLimitException extends RuntimeException {
    public NihRateLimitException(String message) {
        super(message);
    }
}
