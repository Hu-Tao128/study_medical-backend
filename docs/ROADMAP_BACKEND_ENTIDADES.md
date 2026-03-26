# Roadmap de Entidades Backend

## Estado actual

### Hecho
- Se rediseño el core relacional en Supabase con tablas: `users`, `institutions`, `groups`, `memberships`, `topics`, `user_progress`, `embeddings`.
- Se aplicaron relaciones con `FOREIGN KEY`, `ON DELETE` y los indices criticos (`memberships`, `user_progress`, `groups`, `topics`, `embeddings`).
- Se habilito RLS en tablas core y se crearon politicas por rol y pertenencia.
- Se alineo `users.auth_id` a `UUID`, y se agregaron campos `career` y `last_active_at`.
- Se agregaron entidades JPA para PostgreSQL: `User`, `Institution`, `StudyGroup`, `Membership`, `Topic`, `UserProgress`, `Embedding`.
- Se agregaron documentos MongoDB: `Flashcard`, `Quiz`, `ClinicalCase`, `ChatMessageBucket`, `Note`.
- Se implementaron casos de uso de aplicacion:
  - `CreateUser`, `GetProfile`, `UpdateProfile`
  - `CreateFlashcard`, `GetFlashcardsByTopic`
  - `GenerateQuizFromAi`, `SubmitQuiz`
  - `SendMessage`, `GetChatHistory`
- Se crearon controladores REST para `/profile`, `/flashcards`, `/quizzes`, `/cases`, `/notes`, `/chat` y flujo `/auth/sync-session`.

### En progreso
- Endurecer validacion de sesiones Firebase/Supabase en un unico flujo 100% validado por `oauth2ResourceServer` sin fallback de decode manual en entornos no-dev.
- Agregar validaciones de negocio por rol en controladores (`STUDENT`, `TEACHER`, `ADMIN`) de forma uniforme.

### Siguiente
- Crear DTOs de request/response para evitar exponer entidades directamente en endpoints.
- Agregar tests de integracion para:
  - RLS + ownership en `users`, `memberships`, `user_progress`
  - Flujos de quiz y actualizacion de progreso
  - Bucket pattern de chat (`count <= 50`)
- Agregar migraciones incrementales para evolucion de esquemas sin depender de `ddl-auto`.
- Documentar contrato de API actualizado para frontend Flutter.

## Migrations aplicadas en Supabase
- `core_schema_structures_v1`
- `core_schema_rls_policies_v1`
- `users_role_default_text_fix`
