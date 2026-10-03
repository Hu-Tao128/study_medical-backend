package com.studymedical.backend.infrastructure.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studymedical.backend.domain.services.EmbeddingNotConfiguredException;
import com.studymedical.backend.domain.services.EmbeddingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Proveedor de embeddings basado en la API de OpenAI.
 *
 * <p>Se activa solo si {@code app.ai.openai.api-key} tiene valor; en caso contrario
 * {@link #isConfigured()} devuelve false y el pipeline de ingesta lo reporta como 503.</p>
 */
@Service
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiEmbeddingProvider(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.ai.openai.embedding-model:text-embedding-3-small}") String model
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
    public float[] embed(String text) {
        if (!isConfigured()) {
            throw new EmbeddingNotConfiguredException();
        }

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("input", text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/embeddings"))
                    .timeout(Duration.ofSeconds(30))
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
                        "Proveedor de embeddings respondió " + response.statusCode()
                );
            }

            JsonNode embedding = objectMapper.readTree(response.body())
                    .path("data")
                    .path(0)
                    .path("embedding");

            if (!embedding.isArray()) {
                throw new IllegalStateException("Respuesta de embeddings inválida");
            }

            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = (float) embedding.get(i).asDouble();
            }
            return vector;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Generación de embedding interrumpida", e);
        } catch (Exception e) {
            throw new IllegalStateException("Error generando embedding", e);
        }
    }
}
