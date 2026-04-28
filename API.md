# API Documentation (Frontend Guide)

Base URL: `http://<backend-host>/api/v1`  
All endpoints return JSON. Most require JWT authentication.

---

## Authentication

### Token Format
Include your JWT token in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <your-jwt-token>
```

### Get Token (Dev Mode Only)
Use this endpoint to generate a test token (only works with `app.dev-mode=true`):

```javascript
// Frontend example
const getDevToken = async () => {
  const response = await fetch('/api/v1/auth/dev-login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId: 'optional-custom-uuid',
      email: 'dev@example.com',
      name: 'Dev User'
    })
  });
  const data = await response.json();
  localStorage.setItem('jwt_token', data.accessToken);
  return data;
};
```

---

## Endpoint Reference

### 1. Health & System Endpoints

#### GET `/` (Root Info)
- Auth: No
- Returns service info and endpoint map
- Success: 200

```javascript
fetch('/').then(r => r.json()) // Returns service metadata
```

#### GET `/health`
- Auth: No
- Check service status
- Success: 200 `{ "status": "UP", "service": "Study Medical Backend" }`

---

### 2. Search Endpoint (Updated)

#### GET `/search`
- Auth: **Required** (JWT Bearer)
- Description: Search medical content across NIH/local sources with caching

**Query Parameters:**

| Param | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| `q` | string | Yes | - | Non-empty, max 100 chars, trimmed |
| `source` | string | No | `all` | Must be `all`, `nih`, or `local` |
| `limit` | number | No | 5 | Max 20 |
| `page` | number | No | 1 | Min 1 |

**Success Response (200):** `SearchResponse` (contains results, pagination, sources)

**Error Status Codes:**

| Code | Reason | Response Body |
|------|--------|---------------|
| 400 | Invalid params | `{ "error": "El parámetro 'q' es requerido" }` / `{ "error": "Source inválido. Valores permitidos: [all, nih, local]" }` / max length error |
| 401 | Invalid/missing token | `{ "error": "Token invalido" }` |
| 429 | Rate limit exceeded | `{ "error": "Demasiadas solicitudes, intenta en unos segundos" }` |
| 500 | Server error | `{ "error": "Error interno del servidor" }` |

**Frontend Usage:**

```javascript
const searchMedical = async (query, source = 'all', limit = 5, page = 1) => {
  const token = localStorage.getItem('jwt_token');
  const url = new URL('/api/v1/search', window.location.origin);
  url.searchParams.set('q', query.trim());
  url.searchParams.set('source', source);
  url.searchParams.set('limit', limit);
  url.searchParams.set('page', page);

  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (response.status === 429) {
    alert('Too many requests, please wait');
    return;
  }
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error);
  }
  return response.json(); // Cached results will return instantly
};
```

---

### 3. Profile Endpoints

#### GET `/profile/me`
- Auth: Required
- Returns current user profile
- Success: 200 `ProfileResponseDto`
- Error: 401 (invalid token)

#### PATCH `/profile/me`
- Auth: Required
- Body: `UpdateProfileRequest` (displayName, photoUrl, preferredLanguage, theme, level, semester, career)
- Success: 200 updated profile
- Error: 401, 404 (user not found)

---

### 4. Topic Endpoints

#### GET `/topics`
- Auth: No
- Returns list of available study topics
- Success: 200 `[ { "id": "uuid", "name": "Topic Name" } ]`

---

### 5. Note Endpoints

#### GET `/notes`
- Auth: Required
- Query Param: `topicId` (optional)
- Returns notes for current user, filtered by topic if provided

#### POST `/notes`
- Auth: Required
- Body: `UpsertNoteRequest` (title, contentMd, topicId, tags, etc.)
- Success: 200 created note

#### GET/PUT/PATCH/DELETE `/notes/{id}`
- Auth: Required
- Standard CRUD operations for notes
- Error: 404 (note not found), 403 (no access)

---

### 6. Quiz Endpoints

#### POST `/quizzes/ai-generate`
- Auth: Required (TEACHER/ADMIN role)
- Body: `GenerateQuizRequest` (title, topicId, questions, visibility)
- Generates AI quiz

#### POST `/quizzes/{quizId}/submit`
- Auth: Required
- Body: `SubmitQuizRequest` (userId, answers)
- Returns quiz results

#### GET `/quizzes/topic/{topicId}`
- Auth: Required
- Returns quizzes for a topic, filtered by visibility

---

### 7. Flashcard Endpoints

#### POST `/flashcards`
- Auth: Required (TEACHER/ADMIN role)
- Body: `CreateFlashcardRequest` (question, answer, difficulty, topicId)
- Creates flashcards

#### GET `/flashcards/topic/{topicId}`
- Auth: Required
- Returns flashcards for a topic, filtered by visibility

---

## Common Status Codes

| Code | Meaning | Frontend Handling |
|------|---------|-------------------|
| 200 | Success | Parse response JSON |
| 400 | Bad Request | Show validation error to user |
| 401 | Unauthorized | Redirect to login |
| 403 | Forbidden | Show "No access" message |
| 404 | Not Found | Show "Resource not found" |
| 429 | Rate Limited | Show retry message, backoff |
| 500 | Server Error | Show generic error, log to monitoring |

---

## Notes

- Search results are cached in-memory: repeated identical queries return instantly
- All authenticated endpoints use `Jwt` tokens from OAuth2/your auth provider
- Dev mode endpoints are disabled in production (`app.dev-mode=false`)
- Search cache uses adaptive TTL: popular queries (diabetes, hypertension, etc.) cached for 60 min, others for 10 min
