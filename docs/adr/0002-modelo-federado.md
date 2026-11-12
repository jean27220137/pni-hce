# ADR-0002: Modelo federado en lugar de repositorio centralizado

- **Estado:** Aceptado
- **Fecha:** 2026-11-12

## Contexto
Hay dos formas de compartir historias clínicas entre establecimientos:
1. **Centralizado:** copiar todas las historias a una base de datos única.
2. **Federado:** cada establecimiento conserva sus datos; la plataforma solo sabe dónde están y los trae al momento.

La Ley 31750 asigna al RENHICE (MINSA) un repositorio de respaldo. La Ley 29733 exige minimizar el tratamiento de datos personales.

## Decisión
PNI-HCE usa el **modelo federado**:
- El **MPI** guarda solo la vinculación DNI ↔ establecimiento (y el identificador local del paciente).
- Las historias se consultan en vivo al adaptador FHIR del establecimiento origen.
- El repositorio de respaldo es función del RENHICE, fuera del alcance de esta plataforma.

Referentes: HCEN de Uruguay (federado) y TEFCA de EE. UU. (localización de registros + FHIR).

## Alternativas descartadas
- **Centralizado:** más simple de consultar, pero concentra datos sensibles, duplica responsabilidad con el RENHICE y obliga a sincronizar copias.

## Consecuencias
- (+) Menor superficie de riesgo sobre datos sensibles.
- (+) Cada establecimiento mantiene su sistema actual; solo agrega un adaptador.
- (−) Si un establecimiento está caído, su historial no está disponible → se mitiga con circuit breaker y caché de resúmenes (ADR-0004).
