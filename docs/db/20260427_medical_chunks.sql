-- Tabla para almacenar fragmentos de libros médicos locales
CREATE TABLE IF NOT EXISTS medical_chunks (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    chunk_text TEXT NOT NULL,
    chunk_title TEXT,
    book TEXT,
    author TEXT,
    edition TEXT,
    embedding VECTOR(1536),
    tags JSONB,
    disease TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Índices para búsquedas eficientes
CREATE INDEX IF NOT EXISTS medical_chunks_disease_idx ON medical_chunks(disease);
CREATE INDEX IF NOT EXISTS medical_chunks_book_idx ON medical_chunks(book);
CREATE INDEX IF NOT EXISTS medical_chunks_created_idx ON medical_chunks(created_at DESC);

-- Índice vectorial para búsqueda por similitud coseno (requiere extensión vector)
CREATE INDEX IF NOT EXISTS medical_chunks_embedding_idx 
    ON medical_chunks 
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- Comentarios
COMMENT ON TABLE medical_chunks IS 'Fragmentos de texto de libros médicos para búsqueda local';
COMMENT ON COLUMN medical_chunks.embedding IS 'Vector de embedding generado por OpenAI/Cohere (1536 dimensiones)';
COMMENT ON COLUMN medical_chunks.tags IS 'Etiquetas en formato JSON, ej: {"anatomy": true, "physiology": true}';
COMMENT ON COLUMN medical_chunks.disease IS 'Enfermedad principal relacionada, para filtros rápidos';
