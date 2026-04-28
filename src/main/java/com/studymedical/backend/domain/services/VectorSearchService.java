package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.Embedding;
import com.studymedical.backend.domain.repositories.EmbeddingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final JdbcTemplate jdbcTemplate;

    // 🔥 Búsqueda vectorial real usando cosine similarity
    public List<MedicalSearchResultWithEmbedding> searchByVector(String queryEmbedding, int limit, int offset) {
        String sql = """
            SELECT m.id, m.chunk_text, m.chunk_title, m.book, m.author, m.edition,
                   (1 - (e.embedding <=> ?::vector)) as similarity
            FROM embeddings e
            JOIN medical_chunks m ON m.id::text = e.content_id::text
            WHERE e.content_type = 'medical_chunk'
            ORDER BY e.embedding <=> ?::vector
            LIMIT ? OFFSET ?
            """;

        try {
            return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapVectorResult(rs),
                queryEmbedding, queryEmbedding, limit, offset);
        } catch (Exception e) {
            log.warn("Vector search failed, falling back to text search", e);
            return List.of();
        }
    }

    // Calcular cosine similarity entre dos embeddings
    public double cosineSimilarity(String embedding1, String embedding2) {
        if (embedding1 == null || embedding2 == null) return 0.0;

        try {
            double[] vec1 = parseEmbedding(embedding1);
            double[] vec2 = parseEmbedding(embedding2);

            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;

            for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
                dotProduct += vec1[i] * vec2[i];
                norm1 += vec1[i] * vec1[i];
                norm2 += vec2[i] * vec2[i];
            }

            if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        } catch (Exception e) {
            log.warn("Error calculating cosine similarity", e);
            return 0.0;
        }
    }

    private MedicalSearchResultWithEmbedding mapVectorResult(ResultSet rs) {
        try {
            MedicalSearchResultWithEmbedding result = new MedicalSearchResultWithEmbedding();
            result.setId(rs.getString("id"));
            result.setChunkTitle(rs.getString("chunk_title"));
            result.setChunkText(rs.getString("chunk_text"));
            result.setBook(rs.getString("book"));
            result.setAuthor(rs.getString("author"));
            result.setEdition(rs.getString("edition"));
            result.setSimilarity(rs.getDouble("similarity"));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping vector result", e);
        }
    }

    private double[] parseEmbedding(String embeddingStr) {
        String[] parts = embeddingStr.replace("[", "").replace("]", "").split(",");
        double[] vec = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vec[i] = Double.parseDouble(parts[i].trim());
        }
        return vec;
    }
}
