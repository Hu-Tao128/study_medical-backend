package com.studymedical.backend.domain.services;

/**
 * Puerto para generar embeddings de texto.
 *
 * <p>La dimensión esperada por el esquema actual ({@code vector(1536)}) coincide
 * con modelos tipo OpenAI {@code text-embedding-3-small}.</p>
 */
public interface EmbeddingProvider {

    /**
     * @return true si hay credenciales/configuración suficiente para generar embeddings.
     */
    boolean isConfigured();

    /**
     * Genera el vector de embedding de un texto.
     *
     * @throws EmbeddingNotConfiguredException si no hay proveedor configurado.
     */
    float[] embed(String text);

    /**
     * Serializa el embedding al formato que entiende pgvector: {@code [0.1,0.2,...]}.
     */
    default String embedToPgVector(String text) {
        return toPgVector(embed(text));
    }

    static String toPgVector(float[] vector) {
        StringBuilder builder = new StringBuilder(vector.length * 8);
        builder.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }
}
