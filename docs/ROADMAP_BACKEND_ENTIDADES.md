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
- Se implemento visibilidad real de contenido `private | group | public` en `flashcards`, `quizzes` y `clinical_cases`.
- Se agrego `groupId` para contenido de alcance grupal y filtrado de lectura por visibilidad + membresia.
- Se reforzo acceso de chat por pertenencia a grupo (room_id) para enviar y leer historial.
- Se verificaron cambios recientes de DTOs de response y se corrigio consistencia de `ProfileResponseDto` con el modelo real (`String level`, `LocalDateTime`).
- Se implemento validacion de rol efectivo dentro del grupo para creacion de contenido `GROUP` (`requireGroupTeacherOrAdmin`).
- Se ampliaron pruebas unitarias de autorizacion para visibilidad `private/group/public` en `RoleAuthorizationServiceTest`.

### En progreso
- Endurecer reglas por tipo de contenido para `GROUP` (teacher del grupo para escribir, miembro para leer) en todos los endpoints faltantes.
- Incorporar validacion de pertenencia de grupo tambien en flujo de `submitQuiz` cuando el quiz sea `GROUP`.

### Completado
- Crear DTOs de response para mantener contrato estable y desacoplar salida de entidades:
  - `ProfileResponseDto`
  - `FlashcardResponseDto`
  - `QuizResponseDto` (incluye `QuizQuestionDto`)
  - `ClinicalCaseResponseDto` (incluye `CaseQuestionDto`)
  - `NoteResponseDto`
  - `ChatMessageBucketResponseDto` (incluye `MessageDto`)
- Actualizados controllers para usar los nuevos DTOs de response en lugar de entidades directas.

### Siguiente
- Agregar tests de integracion conectados a infraestructura real para:
  - RLS + ownership en `users`, `memberships`, `user_progress` con Supabase test project
  - Flujos end-to-end de quiz y actualizacion de progreso con visibilidad `private/group/public`
  - Flujo completo de chat con multiples buckets, orden temporal y acceso por membresia
- Agregar migraciones incrementales para evolucion de esquemas sin depender de `ddl-auto`.
- Documentar contrato de API actualizado para frontend Flutter.
- Agregar tests unitarios adicionales de autorizacion por visibilidad para controllers (private/group/public).

## Migrations aplicadas en Supabase
- `core_schema_structures_v1`
- `core_schema_rls_policies_v1`
- `users_role_default_text_fix`
