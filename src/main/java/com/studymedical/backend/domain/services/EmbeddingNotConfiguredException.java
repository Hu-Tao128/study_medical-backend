package com.studymedical.backend.domain.services;

/**
 * Se lanza cuando se intenta generar embeddings sin proveedor configurado.
 */
public class EmbeddingNotConfiguredException extends RuntimeException {

    public EmbeddingNotConfiguredException() {
        super("No hay proveedor de embeddings configurado. Define OPENAI_API_KEY (o el proveedor elegido).");
    }
}
