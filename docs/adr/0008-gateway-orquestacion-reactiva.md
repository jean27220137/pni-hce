# ADR-0008: Gateway con orquestación reactiva (WebMVC + WebClient + Reactor)

- **Estado:** Aceptado
- **Fecha:** 2026-11-13

## Contexto
El gateway necesita llamar en paralelo a N adaptadores FHIR (uno por establecimiento donde el paciente tiene historial). Con un modelo bloqueante (RestTemplate / RestClient), cada llamada a un adaptador ocuparía un hilo durante toda la espera de red. Con 500 VUs concurrentes y 2 establecimientos, eso serían 1000 hilos bloqueados simultáneamente.

Hay dos opciones principales:
1. **Spring WebFlux completo** — el controlador también devuelve `Mono`/`Flux`, todo reactivo end-to-end.
2. **Spring WebMVC + WebClient + virtual threads** — el controlador es MVC clásico (puede devolver `Mono` gracias al soporte reactivo de Spring Framework 7), y `WebClient` gestiona las llamadas HTTP de forma no bloqueante.

## Decisión
Se usa la opción 2: **Spring WebMVC con soporte de tipos reactivos + WebClient**. El controlador devuelve `Mono<ResponseEntity<HistoriaResponse>>`. Las llamadas a MPI y FHIR se ejecutan con `Flux.flatMap` (paralelo). Los virtual threads (`spring.threads.virtual.enabled=true`) complementan la estrategia para operaciones de I/O residuales.

## Consecuencias

**Positivas:**
- El equipo conoce Spring MVC; la curva de aprendizaje de WebFlux completo es alta para un proyecto académico de semestre.
- `Flux.flatMap` sobre la lista de establecimientos ejecuta las llamadas FHIR en paralelo sin bloquear hilos de OS.
- El `@ExceptionHandler` de WebMVC funciona normalmente incluso con return types `Mono<>`.
- Virtual threads absorben cualquier bloqueo residual (Flyway, JPA) sin cambios de código.

**Negativas:**
- Mezclar MVC y WebFlux en el mismo módulo puede confundir a quien no conozca esta configuración. Se documenta en el SPEC del gateway.
- El backpressure nativo de WebFlux no está disponible en el controlador MVC; en producción real con throughput muy alto se recomendaría migrar a WebFlux puro.
