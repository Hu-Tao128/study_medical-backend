# Roadmap temporal - Study loop (se borra al cerrar)

## Estado actual
- Paso actual: 4 (Fase 2 completada, iniciando integracion)
- Objetivo: cerrar loop `start -> submit -> progress`

## Hecho
- [x] Definido alcance de Prioridad 1
- [x] Crear entidad/repositorio `study_sessions`
- [x] Implementar `POST /api/v1/study-sessions/start`
- [x] Implementar `POST /api/v1/study-sessions/submit`
- [x] Implementar `GET /api/v1/study-sessions?topicId=...`
- [x] Implementar `GET /api/v1/progress?topicId=...`
- [x] Actualizar `user_progress` con accuracy, attempts, streak y tiempo
- [x] Agregar tests base para casos de uso
- [x] Migrar `study_sessions` para incluir `accuracy`

## En progreso
- [ ] Integrar flujo en frontend Study (consumo start/submit/progress)
- [ ] Confirmar en QA con datos reales de Mongo + Postgres
- [x] Fase 2 backend: CRUD complementario notes/flashcards/quizzes/clinical-cases

## Falta (despues de Prioridad 1)
- [ ] Completar CRUD faltante de notes/flashcards/quizzes
- [ ] Ajustar radar endpoint agregado por topicos
