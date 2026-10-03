package com.studymedical.backend.infrastructure.persistence;

import com.studymedical.backend.domain.entities.DocumentChunk;
import com.studymedical.backend.domain.entities.DocumentStatus;
import com.studymedical.backend.domain.entities.UserDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de documentos RAG por usuario y sus chunks con embedding.
 *
 * <p>Usa SQL crudo porque `embedding` es de tipo pgvector.</p>
 */
@Repository
@RequiredArgsConstructor
public class UserDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertDocument(UUID id, UUID userId, String filename, String contentType) {
        jdbcTemplate.update("""
                INSERT INTO user_documents (id, user_id, filename, content_type, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """, id, userId, filename, contentType);
    }

    public void updateStatus(UUID id, DocumentStatus status, int chunkCount, String errorMessage) {
        jdbcTemplate.update("""
                UPDATE user_documents
                SET status = ?, chunk_count = ?, error_message = ?, updated_at = NOW()
                WHERE id = ?
                """, status.name(), chunkCount, errorMessage, id);
    }

    public Optional<UserDocument> findByIdAndUserId(UUID id, UUID userId) {
        List<UserDocument> results = jdbcTemplate.query("""
                SELECT id, user_id, filename, content_type, status, chunk_count,
                       error_message, created_at, updated_at
                FROM user_documents
                WHERE id = ? AND user_id = ?
                """, this::mapDocument, id, userId);
        return results.stream().findFirst();
    }

    public List<UserDocument> findByUserId(UUID userId) {
        return jdbcTemplate.query("""
                SELECT id, user_id, filename, content_type, status, chunk_count,
                       error_message, created_at, updated_at
                FROM user_documents
                WHERE user_id = ?
                ORDER BY created_at DESC
                """, this::mapDocument, userId);
    }

    public void insertChunks(UUID documentId, List<ChunkEmbedding> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO user_document_chunks
                    (id, document_id, chunk_index, page_number, chunk_text, embedding)
                VALUES (?, ?, ?, ?, ?, ?::vector)
                """, chunks, chunks.size(), (statement, chunk) -> {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, documentId);
            statement.setInt(3, chunk.chunkIndex());
            statement.setObject(4, chunk.pageNumber());
            statement.setString(5, chunk.text());
            statement.setString(6, chunk.pgVector());
        });
    }

    public List<DocumentChunk> searchTopK(UUID documentId, String queryVector, int limit) {
        return jdbcTemplate.query("""
                SELECT id, document_id, chunk_index, page_number, chunk_text,
                       (1 - (embedding <=> ?::vector)) AS similarity
                FROM user_document_chunks
                WHERE document_id = ? AND embedding IS NOT NULL
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """, (rs, rowNum) -> new DocumentChunk(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("document_id")),
                rs.getInt("chunk_index"),
                (Integer) rs.getObject("page_number"),
                rs.getString("chunk_text"),
                rs.getDouble("similarity")
        ), queryVector, documentId, queryVector, limit);
    }

    private UserDocument mapDocument(ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new UserDocument(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                rs.getString("filename"),
                rs.getString("content_type"),
                DocumentStatus.valueOf(rs.getString("status")),
                rs.getInt("chunk_count"),
                rs.getString("error_message"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }

    public record ChunkEmbedding(int chunkIndex, Integer pageNumber, String text, String pgVector) {
    }
}
