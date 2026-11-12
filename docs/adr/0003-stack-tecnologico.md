# ADR-0003: Stack tecnológico del prototipo

- **Estado:** Aceptado
- **Fecha:** 2026-11-12

## Decisión

| Capa | Tecnología | Motivo |
|---|---|---|
| Estándar clínico | HL7 FHIR R4 | Validado por el MINSA en las Conectatones 2025-2026 |
| Backend | Java 21 + Spring Boot 4 | Experiencia previa del equipo; HAPI FHIR es Java |
| FHIR | HAPI FHIR | Implementación de referencia de FHIR en Java |
| Gateway | Spring Cloud Gateway | Rate limiting, rutas, autenticación centralizada |
| Resiliencia | Resilience4j | Circuit breaker por establecimiento |
| Base de datos | PostgreSQL 16 | Particionamiento nativo y réplica de lectura |
| Caché | Redis 7 | Resultados del MPI y resúmenes clínicos |
| Firma digital | X.509 + JWS (Nimbus JOSE) | Ley 27269; en producción se reemplaza por certificados RENIEC |
| Búsqueda semántica (opcional) | pgvector + modelo de embeddings local | Sin enviar datos clínicos fuera de la plataforma |
| Frontend | Angular + PrimeNG | Experiencia previa del equipo |
| Contenedores | Docker Compose | Suficiente para una sola VM |
| CI/CD | Jenkins + SonarQube (local) y GitHub Actions | Pipeline y calidad sin costo (ADR-0006) |
| Pruebas | JUnit 5, Testcontainers, Playwright, k6 | Unitarias, integración, E2E y carga |
| Nube | AWS EC2 (us-east-1) | Créditos de cuenta nueva; producción solo 2-3 días |
| DNS / CDN | Cloudflare (plan gratuito) | DNS, CDN y SSL sin costo |

## No se implementa en el prototipo (proyección)
Apache Kafka, Kubernetes, nodos regionales adicionales, RDS/ElastiCache administrados.

## Consecuencias
- (+) Costo del prototipo menor a US$5 en producción.
- (−) El informe debe distinguir siempre "implementado" de "proyectado".
