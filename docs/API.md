# Study Medical - API Documentation

## Base URL
```
http://localhost:8080/api/v1
```

---

## Autenticación

### Desarrollo (Dev Login)
**Endpoint:** `POST /api/v1/auth/dev-login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "name": "Nombre de Usuario",
  "userId": "opcional-id"
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "dev-user-123456",
    "email": "user@example.com",
    "displayName": "Nombre de Usuario",
    "role": "STUDENT"
  }
}
```

---

### Sincronizar sesion Firebase/Supabase
Sincroniza el usuario autenticado en `users` usando el token Bearer recibido.

**Endpoint:** `POST /api/v1/auth/sync-session`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "id": "f0dd5f74-2dc3-4f66-8ae2-6a57a9f2d829",
  "authId": "5f94dc6f-4f7f-4b31-a5d2-46de2925f9a9",
  "email": "user@example.com",
  "displayName": "Nombre de Usuario",
  "role": "STUDENT",
  "lastLoginAt": "2026-03-26T00:25:40"
}
```

---

### Profile (Usuario actual)
Obtiene el perfil del usuario autenticado. El usuario se sincroniza automáticamente en PostgreSQL la primera vez que accede.

**Endpoint:** `GET /api/v1/profile/me`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200):**
```json
{
  "id": "dev-user-123456",
  "email": "user@example.com",
  "displayName": "Nombre de Usuario",
  "photoUrl": "",
  "role": "STUDENT",
  "createdAt": "2026-03-24T13:17:10"
}
```

---

## Endpoints Públicos

### Health Check
**Endpoint:** `GET /api/v1/health`

**Response (200):**
```json
{
  "status": "UP",
  "service": "Study Medical Backend"
}
```

---

## Endpoints de Contenido

### Flashcards
- `POST /api/v1/flashcards`
- `GET /api/v1/flashcards/topic/{topicId}`

`POST /api/v1/flashcards` soporta visibilidad:
- `PRIVATE`: solo owner
- `GROUP`: requiere `groupId` y rol teacher en ese grupo para crear
- `PUBLIC`: visible para usuarios autenticados

### Quizzes
- `POST /api/v1/quizzes/ai-generate`
- `GET /api/v1/quizzes/topic/{topicId}`
- `POST /api/v1/quizzes/{quizId}/submit`

`POST /api/v1/quizzes/{quizId}/submit` valida:
- owner para quizzes `PRIVATE`
- membresia para quizzes `GROUP`
- acceso libre autenticado para quizzes `PUBLIC`

### Casos clinicos
- `POST /api/v1/cases`
- `GET /api/v1/cases/topic/{topicId}`

`POST /api/v1/cases` con visibilidad `GROUP` exige `groupId` y rol teacher en ese grupo.

### Notas
- `POST /api/v1/notes`
- `PUT /api/v1/notes/{id}`
- `GET /api/v1/notes/user/{userId}`

### Chat
- `POST /api/v1/chat/{roomId}/messages`
- `GET /api/v1/chat/{roomId}/history`

---

## Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado |
| 400 | Bad Request - Datos inválidos |
| 401 | Unauthorized - No autenticado |
| 403 | Forbidden - Sin permisos |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error del servidor |

---

## Flujo de Producción (Supabase)

En producción, el flujo será:

1. **Flutter** inicia sesión con Firebase
2. **Supabase** recibe el login de Firebase y genera su propio JWT
3. **Flutter** usa el JWT de Supabase para las peticiones
4. **Backend** valida el JWT de Supabase usando `oauth2ResourceServer.jwt()`

### Endpoints en Producción

```dart
// Flutter - Usar JWT de Supabase
final supabase = Supabase.instance.client;
final token = supabase.auth.session().accessToken;

// Peticiones al backend
await http.get(
  Uri.parse('$BASE_URL/api/v1/profile/me'),
  headers: {'Authorization': 'Bearer $token'},
);
```

---

## Configuración

### Desarrollo (application-dev.yaml)
- Base de datos: H2 en memoria
- JWT: Generado localmente
- Endpoint dev-login: Habilitado

### Producción (application.yaml)
- Base de datos: PostgreSQL (Supabase)
- JWT: Validado contra Supabase (`https://spxgotrytjkofqinsklw.supabase.co/auth/v1`)
- Endpoint dev-login: Deshabilitado
