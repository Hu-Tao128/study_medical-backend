package com.studymedical.backend.infrastructure.persistence;

import com.studymedical.backend.domain.entities.MedicalChunk;
import com.studymedical.backend.domain.repositories.MedicalChunkRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MedicalChunkJpaRepository implements MedicalChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MedicalChunk> findByDisease(String disease, int limit, int offset) {
        String sql = """
            SELECT id, chunk_text, chunk_title, book, author, edition
            FROM medical_chunks
            WHERE disease = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, this::mapRow, disease, limit, offset);
    }

    @Override
    public List<MedicalChunk> findByBook(String book, int limit, int offset) {
        String sql = """
            SELECT id, chunk_text, chunk_title, book, author, edition
            FROM medical_chunks
            WHERE book ILIKE ?
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, this::mapRow, "%" + book + "%", limit, offset);
    }

    @Override
    public List<MedicalChunk> findAll(int limit, int offset) {
        String sql = """
            SELECT id, chunk_text, chunk_title, book, author, edition
            FROM medical_chunks
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, this::mapRow, limit, offset);
    }

    @Override
    public List<MedicalChunk> searchByText(String query, int limit, int offset) {
        String sql = """
            SELECT id, chunk_text, chunk_title, book, author, edition
            FROM medical_chunks
            WHERE to_tsvector('spanish', chunk_text || ' ' || chunk_title)
                  @@ plainto_tsquery('spanish', ?)
            ORDER BY ts_rank(to_tsvector('spanish', chunk_text), plainto_tsquery('spanish', ?)) DESC
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, this::mapRow, query, query, limit, offset);
    }

    private MedicalChunk mapRow(ResultSet rs, int rowNum) {
        try {
            MedicalChunk chunk = new MedicalChunk();
            chunk.setId(UUID.fromString(rs.getString("id")));
            chunk.setChunkText(rs.getString("chunk_text"));
            chunk.setChunkTitle(rs.getString("chunk_title"));
            chunk.setBook(rs.getString("book"));
            chunk.setAuthor(rs.getString("author"));
            chunk.setEdition(rs.getString("edition"));
            return chunk;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping MedicalChunk", e);
        }
    }
}
