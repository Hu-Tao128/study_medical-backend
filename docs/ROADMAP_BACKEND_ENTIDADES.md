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
- Se endurecio el flujo de sesion: en no-dev, `/auth/sync-session` exige JWT validado por `oauth2ResourceServer`.
- Se agregaron DTOs de request + `@Valid` en controladores de contenido para no exponer entidades directamente.
- Se agregaron pruebas para casos criticos:
  - `SubmitQuizUseCaseTest` (calculo y persistencia de progreso)
  - `SendMessageUseCaseTest` (bucket reutilizado/rotado)
  - `RlsPolicySqlTest` (presencia de politicas RLS clave en SQL)
- Se agrego `RoleAuthorizationService` y se aplico autorizacion uniforme por rol/ownership en endpoints de contenido y chat.

### En progreso
- Endurecer reglas de autorizacion por pertenencia a grupo para chat y contenido con visibilidad `group`.

### Siguiente
- Crear DTOs de response para mantener contrato estable y desacoplar salida de entidades.
- Agregar tests de integracion conectados a infraestructura real para:
  - RLS + ownership en `users`, `memberships`, `user_progress` con Supabase test project
  - Flujos end-to-end de quiz y actualizacion de progreso
  - Flujo completo de chat con multiples buckets y orden temporal
- Agregar migraciones incrementales para evolucion de esquemas sin depender de `ddl-auto`.
- Documentar contrato de API actualizado para frontend Flutter.

## Migrations aplicadas en Supabase
- `core_schema_structures_v1`
- `core_schema_rls_policies_v1`
- `users_role_default_text_fix`
