# CLAUDE.md — reglas del proyecto PNI-HCE

## Contexto
Plataforma de interoperabilidad de historias clínicas electrónicas entre establecimientos de salud de Ayacucho (piloto). Modelo federado: la plataforma NO almacena historias clínicas, solo el índice DNI ↔ establecimiento y la auditoría.

## Flujo de trabajo (SDD)
1. Leer el `SPEC.md` del servicio antes de tocar código.
2. Si el cambio no está en el SPEC, actualizar primero el SPEC.
3. Cada requisito del SPEC (MPI-RF-xx) debe tener al menos un test que lo cubra.

## Stack
- Java 21, Spring Boot 4, Maven
- PostgreSQL 16, Redis 7
- HAPI FHIR (recursos FHIR R4)
- Angular + PrimeNG
- Docker Compose (no Kubernetes en el prototipo)

## Convenciones
- Paquetes: `pe.edu.unsch.pnihce.<servicio>`
- API: `/api/v1/<recurso>`, JSON, errores con RFC 7807 (Problem Details)
- Nunca registrar DNI ni nombres completos en logs.
- Migraciones de base de datos con Flyway, nunca `ddl-auto=update`.
- Tests: JUnit 5 + Testcontainers para integración.

## Fuera de alcance del prototipo (solo proyección en el informe)
Kafka, Kubernetes, nodos regionales adicionales al nodo Sur, chatbot con IA.
