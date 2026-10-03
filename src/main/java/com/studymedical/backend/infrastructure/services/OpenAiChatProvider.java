package com.studymedical.backend.infrastructure.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studymedical.backend.domain.services.ChatCompletionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * LLM de chat basado en la API de OpenAI (por defecto {@code gpt-4o-mini}).
 * Deshabilitado si no hay {@code app.ai.openai.api-key}.
 */
@Service
public class OpenAiChatProvider implements ChatCompletionProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiChatProvider(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.ai.openai.chat-model:gpt-4o-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            throw new IllegalStateException("Modelo de chat no configurado");
        }

        try {
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            ArrayNode messages = objectMapper.createArrayNode();
            messages.add(systemMessage);
            messages.add(userMessage);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.set("messages", messages);
            body.put("temperature", 0.2);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Proveedor de chat respondió " + response.statusCode()
                );
            }

            JsonNode content = objectMapper.readTree(response.body())
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (!content.isTextual()) {
                throw new IllegalStateException("Respuesta de chat inválida");
            }
            return content.asText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Generación de respuesta interrumpida", e);
        } catch (Exception e) {
            throw new IllegalStateException("Error generando respuesta", e);
        }
    }
}
