# Roadmap temporal - Study loop (se borra al cerrar)

## Estado actual
- Paso actual: 5 (roadmap backend completado, listo para integrar frontend)
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
- [ ] Integrar flujo en frontend Study (consumo start/submit/progress/radar)
- [ ] Confirmar en QA con datos reales de Mongo + Postgres

## Cierre backend
- [x] Prioridad 1 completa (study sessions + progress)
- [x] Fase 2 completa (CRUD complementario de contenido)
- [x] API backend actualizada para handoff a frontend

## Nota
- Este roadmap temporal se puede eliminar cuando termine la integracion frontend + QA final.
