-- RAG por usuario: documentos subidos + chunks con embedding y número de página.
-- Requiere la extensión pgvector (vector(1536) => OpenAI text-embedding-3-small).

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS user_documents (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    filename TEXT NOT NULL,
    content_type TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    chunk_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_user_documents_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS user_documents_user_idx ON user_documents(user_id);

CREATE TABLE IF NOT EXISTS user_document_chunks (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES user_documents(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    page_number INTEGER,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS user_document_chunks_doc_idx
    ON user_document_chunks(document_id);

-- Índice vectorial para la búsqueda por similitud coseno.
CREATE INDEX IF NOT EXISTS user_document_chunks_embedding_idx
    ON user_document_chunks
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

COMMENT ON TABLE user_documents IS 'Documentos (PDF) subidos por el usuario para RAG';
COMMENT ON TABLE user_document_chunks IS 'Fragmentos del documento con embedding y página de origen (para citas)';
