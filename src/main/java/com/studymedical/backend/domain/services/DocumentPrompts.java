package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.DocumentChunk;

import java.util.List;

/**
 * Construcción de los prompts del RAG. Separado para poder testearlo sin LLM.
 */
public final class DocumentPrompts {

    private DocumentPrompts() {
    }

    public static final String SYSTEM_PROMPT = """
            Eres un asistente de estudio médico. Responde ÚNICAMENTE con la información
            de los fragmentos del documento que se te proporcionan. Cita la fuente con el
            formato [página N] después de cada dato relevante. Si la respuesta no está en
            los fragmentos, dilo claramente. No inventes información ni uses conocimiento
            externo.
            """.stripIndent().trim();

    public static String buildUserPrompt(String question, List<DocumentChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        builder.append("Pregunta: ").append(question).append("\n\n");
        builder.append("Fragmentos del documento:\n\n");

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            String page = chunk.pageNumber() != null
                    ? "página " + chunk.pageNumber()
                    : "página desconocida";
            builder.append('[').append(i + 1).append("] (").append(page).append(")\n");
            builder.append(chunk.text()).append("\n\n");
        }

        return builder.toString().trim();
    }
}
