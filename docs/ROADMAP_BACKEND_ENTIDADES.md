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
- Se incorporo control de acceso por visibilidad en `submitQuiz` para bloquear intentos sobre quizzes no visibles para el usuario.
- Se agregaron tests de integracion con Testcontainers:
  - `SubmitQuizUseCaseIntegrationTest` (PostgreSQL + Mongo)
  - `ChatBucketIntegrationTest` (Mongo bucket pattern)
- Se detecto que Gradle 8.10.2 falla con Java 25 en este entorno; para tests se ejecuto con `JAVA_HOME=/usr/lib/jvm/java-17-temurin-jdk`.
- Se agrego workflow CI en GitHub Actions fijando Java 17 (`.github/workflows/ci.yml`) para asegurar ejecucion estable de tests en push/PR.
- Se agrego test de integracion opcional para RLS de Supabase (`SupabaseRlsIntegrationTest`) usando JDBC y `request.jwt.claims`.

### En progreso
- Endurecer reglas por tipo de contenido para `GROUP` (teacher del grupo para escribir, miembro para leer) en todos los endpoints faltantes.
- Actualizar documentacion funcional del contrato API para visibilidad y reglas de acceso.
- Completar tests de integracion de Supabase para `memberships` y `user_progress` (requiere env vars).

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
- Agregar tests de integracion end-to-end de quiz y chat con visibilidad `private/group/public` y roles.
- Agregar migraciones incrementales para evolucion de esquemas sin depender de `ddl-auto`.
- Documentar contrato de API actualizado para frontend Flutter.
- Agregar tests unitarios de controllers con MockMvc para visibilidad (private/group/public).

## Migrations aplicadas en Supabase
- `core_schema_structures_v1`
- `core_schema_rls_policies_v1`
- `users_role_default_text_fix`
