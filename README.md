# 🏥 Study Medical — Backend

Backend del sistema **Study Medical**, una plataforma de estudio médico con soporte para tarjetas de memoria (flashcards), quizzes, casos clínicos y seguimiento de progreso basado en el algoritmo SM-2.

---

## 📖 Descripción

Study Medical es una aplicación móvil y web diseñada para estudiantes de medicina. El backend proporciona una API REST que gestiona la autenticación, el contenido de estudio, el motor de repetición espaciada y las métricas de progreso del usuario.

El sistema integra dos bases de datos (PostgreSQL y MongoDB) para optimizar el almacenamiento de datos estructurados versus datos flexibles, junto con Supabase para autenticación y blob storage.

---

## 🏗️ Arquitectura

El backend sigue una arquitectura **Hexagonal (Ports & Adapters)** adaptada, separando la lógica de negocio de las dependencias externas.

```
backend/
├── domain/          # Entidades y reglas de negocio puras
├── application/     # Casos de uso (Use Cases)
├── infrastructure/  # Adaptadores: REST, Repositorios, Integraciones
└── config/         # Seguridad, JWT, CORS, Beans
```

### Dominio (domain/)

Contiene la lógica pura del negocio sin dependencias de frameworks:

- **Entidades**: User, StudySession, Flashcard, Quiz, ClinicalCase, Group
- **Reglas de negocio**: Algoritmo SM-2, cálculo de progreso, validaciones

### Aplicación (application/)

Orquesta los casos de uso del sistema:

- `StartStudySession`
- `SubmitStudySession`
- `GetUserProgress`
- `GenerateQuiz`
- `SyncUser`

### Infraestructura (infrastructure/)

Adaptadores para sistemas externos:

- **REST Controllers**: Endpoints de la API
- **Repositorios**: Implementaciones JPA para PostgreSQL, MongoDB para MongoDB
- **Integraciones**: Supabase Auth, Supabase Storage (S3), servicios de IA (futuro)

---

## 🔐 Autenticación y Seguridad

El sistema utiliza **Firebase Auth** para la gestión de identidades en el frontend. El backend valida los JWT (ID Tokens) recibidos de Firebase en cada request y sincroniza la información del usuario con la base de datos interna en Supabase.

Flujo de autenticación:
1. Cliente autenticado con Firebase (Email/Pass o Google) → JWT recibido
2. Backend valida firma y expiración del token contra los emisores de Google/Firebase.
3. Usuario mapeado/creado en la base de datos PostgreSQL alojada en **Supabase**.
4. Acceso controlado mediante verificación de roles (`STUDENT`, `TEACHER`, `ADMIN`).

El backend es responsable de:
- Validar tokens JWT de Firebase.
- Sincronizar perfiles de usuario en PostgreSQL.
- Aplicar contextos de RLS (Row Level Security) para consultas a Supabase.

---

## 🗄️ Bases de Datos

El sistema utiliza dos bases de datos complementarias:

### PostgreSQL (via Supabase)

Datos estructurados que requieren integridad referencial:

- `users` — Identidad y configuracion del usuario
- `institutions` — Instituciones academicas
- `groups` — Grupos de estudio
- `memberships` — Membresias y rol por grupo
- `topics` — Estructura academica jerarquica
- `user_progress` — Progreso por usuario y tema
- `embeddings` — Metadatos vectoriales con pgvector

### MongoDB

Datos flexibles y jerárquicos:

- `flashcards` — Tarjetas de estudio
- `quizzes` — Exámenes y preguntas
- `clinical_cases` — Casos clínicos
- `notes` — Notas del usuario
- `chat_messages` — Mensajes (bucket pattern)

### Supabase Storage (S3)

Almacenamiento de archivos:

- Modelos 3D anatómicos
- Imágenes de casos clínicos
- Documentos adjuntos

---

## 🧠 Motor de Estudio

### Algoritmo SM-2 Lite

El backend implementa una versión simplificada del algoritmo SM-2 para gestionar la repetición espaciada de tarjetas de memoria:

- Cálculo de intervalos de repetición
- Determinación de dificultad por tarjeta
- Priorización de contenido basado en rendimiento

### Sistema de Progreso

El progreso se mide en tres niveles:

1. **Nivel Tema**: Contenido específico (ej: "Miembro superior")
2. **Nivel Materia**: Área de estudio (ej: Anatomía)
3. **Nivel Global**: Aggregación total (ej: Medicina general)

Métricas calculadas:
- Accuracy (precisión)
- Número de intentos
- Progreso temporal
- Mejora respecto a sesión anterior

---

## 📡 API REST

La API sigue un esquema de versionado con prefijo `/api/v1/`.

### Endpoints principales

| Módulo | Endpoints |
|--------|-----------|
| **Auth** | `/auth/verify`, `/auth/sync` |
| **User** | `/users/profile`, `/users/settings` |
| **Study** | `/study/session/start`, `/study/session/submit`, `/study/cards` |
| **Content** | `/content/flashcards`, `/content/quizzes`, `/content/cases` |
| **Progress** | `/progress`, `/progress/topics`, `/progress/global` |
| **Groups** | `/groups`, `/groups/{id}/members` |
| **Assets** | `/assets/upload`, `/assets/{id}` |

Todos los endpoints requieren autenticación JWT válida excepto los de verificación inicial.

---

## 🧪 Testing

El proyecto implementa una estrategia de testing en tres capas:

1. **Unit Tests**: Testing de lógica de dominio y servicios puros
2. **Integration Tests**: Testing con bases de datos reales
3. **API Tests**: Testing de endpoints con контра actual del OpenAPI spec

---

## ⚙️ Requisitos y Dependencias

### Runtime
- **Java 17+** con Spring Boot 3.x

### Principales dependencias
- Spring Security + JWT
- Spring Data JPA (PostgreSQL)
- Spring Data MongoDB
- Supabase Java Client
- Lombok
- MapStruct

### Variables de entorno requeridas
- `SUPABASE_URL` — URL del proyecto Supabase
- `SUPABASE_KEY` — Clave de API de Supabase
- `DATABASE_URL` — Conexión PostgreSQL
- `MONGODB_URI` — Conexión MongoDB

---

## 📂 Estructura de commits

El proyecto sigue **Conventional Commits**:

- `feat:` Nuevas funcionalidades
- `fix:` Correcciones de bugs
- `refactor:` Refactorizaciones
- `docs:` Documentación
- `test:` Tests
- `chore:` Tareas de mantenimiento

---

## 📄 Licencia

Este proyecto es propiedad de Study Medical. Todos los derechos reservados.

---

## 📌 Notas

- El frontend (Flutter) NO accede directamente a las bases de datos; siempre consume la API del backend.
- El backend controla toda la lógica de negocio; el frontend solo presenta datos.
- Este documento se actualiza conforme evoluciona el sistema.
