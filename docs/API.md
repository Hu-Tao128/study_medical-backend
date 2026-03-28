# Study Medical Backend API (v1)

## Base URL
`http://<host>:<port>/api/v1`

## Auth model
- Header requerido en endpoints protegidos:
  - `Authorization: Bearer <firebase_id_token>`
- Flujo recomendado:
  1. Login en Firebase desde frontend.
  2. Llamar `POST /api/v1/auth/sync-session`.
  3. Usar el mismo token en el resto de requests.

## Health
- `GET /`
- `GET /api/v1/health`

## Auth / Profile

### `POST /api/v1/auth/sync-session`
Sincroniza o crea usuario en `public.users`.

### `GET /api/v1/profile/me`
Retorna perfil actual.

### `PATCH /api/v1/profile/me`
Actualiza perfil (`displayName`, `photoUrl`, `preferredLanguage`, `theme`, `level`, `semester`, `career`).

## Study loop (core)

### `POST /api/v1/study-sessions/start`
Inicia sesion de estudio para un topic.

Request:
```json
{
  "topicId": "uuid",
  "mode": "FLASHCARDS",
  "limit": 20
}
```

Response:
```json
{
  "sessionId": "uuid",
  "cards": [
    {
      "cardId": "mongo_id",
      "question": "...",
      "tags": ["neuro"]
    }
  ]
}
```

### `POST /api/v1/study-sessions/submit`
Guarda resultados de la sesion y actualiza `user_progress`.

Request:
```json
{
  "sessionId": "uuid",
  "topicId": "uuid",
  "attempts": [
    {
      "cardId": "mongo_id",
      "difficulty": 3,
      "correct": true,
      "timeMs": 12000
    }
  ]
}
```

Response:
```json
{
  "accuracy": 0.7,
  "correctCount": 7,
  "total": 10
}
```

### `GET /api/v1/study-sessions?topicId=<uuid-opcional>`
Historial de sesiones del usuario autenticado (filtrable por topic).

### `GET /api/v1/progress?topicId=<uuid>`
Progreso agregado por topic del usuario autenticado.

Response:
```json
{
  "accuracy": 0.72,
  "attempts": 120,
  "lastStudiedAt": "2026-03-28T10:00:00"
}
```

### `GET /api/v1/progress/radar`
Serie para grafica radar (accuracy por topic).

Response:
```json
{
  "topics": [
    {
      "topicId": "uuid",
      "name": "Neuro",
      "accuracy": 0.8
    }
  ]
}
```

## Notes
- `POST /api/v1/notes`
- `GET /api/v1/notes/{id}`
- `GET /api/v1/notes?topicId=<uuid-opcional>`
- `GET /api/v1/notes/user/{userId}`
- `PUT /api/v1/notes/{id}`
- `PATCH /api/v1/notes/{id}`
- `DELETE /api/v1/notes/{id}`

## Flashcards
- `POST /api/v1/flashcards`
- `GET /api/v1/flashcards/topic/{topicId}`
- `GET /api/v1/flashcards/{id}`
- `PUT /api/v1/flashcards/{id}`
- `DELETE /api/v1/flashcards/{id}`

## Quizzes
- `POST /api/v1/quizzes/ai-generate`
- `GET /api/v1/quizzes/topic/{topicId}`
- `GET /api/v1/quizzes/{id}`
- `PUT /api/v1/quizzes/{id}`
- `DELETE /api/v1/quizzes/{id}`
- `POST /api/v1/quizzes/{quizId}/submit`

## Clinical cases
- `POST /api/v1/cases`
- `GET /api/v1/cases/topic/{topicId}`
- `GET /api/v1/cases/{id}`
- `PUT /api/v1/cases/{id}`
- `DELETE /api/v1/cases/{id}`

## Chat
- `POST /api/v1/chat/{roomId}/messages`
- `GET /api/v1/chat/{roomId}/history`

## Reglas de visibilidad
- `PRIVATE`: solo owner.
- `GROUP`: requiere membresia al grupo (y teacher/admin para crear contenido grupal).
- `PUBLIC`: visible para autenticados.

## HTTP status comunes
- `200` ok
- `204` no content
- `400` request invalido
- `401` no autenticado
- `403` sin permisos
- `404` no encontrado
