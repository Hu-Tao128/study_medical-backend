package com.studymedical.backend.domain.services;

/**
 * Puerto para generar respuestas de un modelo de chat (LLM).
 */
public interface ChatCompletionProvider {

    boolean isConfigured();

    String complete(String systemPrompt, String userPrompt);
}
