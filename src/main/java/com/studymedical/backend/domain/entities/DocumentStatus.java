package com.studymedical.backend.domain.entities;

/**
 * Estado del procesamiento de un documento subido.
 */
public enum DocumentStatus {
    PENDING,
    PROCESSING,
    READY,
    FAILED
}
