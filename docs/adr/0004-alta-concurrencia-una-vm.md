# ADR-0004: Estrategia de alta concurrencia en una sola máquina virtual

- **Estado:** Aceptado
- **Fecha:** 2026-11-12

## Contexto
Se exige demostrar escalabilidad de frontend, backend y base de datos, caché y pruebas de carga, con presupuesto mínimo (una EC2 t3.medium, 2 vCPU / 4 GiB).

## Decisión
| Mecanismo | Implementación | Cómo se demuestra |
|---|---|---|
| Escalado horizontal del backend | `docker compose up --scale mpi=3` detrás del gateway | k6 con 1 réplica vs 3 réplicas |
| Réplica de lectura | PostgreSQL primario + réplica por streaming replication; las búsquedas leen de la réplica | Consultas enrutadas a la réplica |
| Particionamiento del MPI | Tabla particionada por rango de DNI | `EXPLAIN` muestra partition pruning |
| Caché | Redis con TTL (localización MPI 10 min) | Latencia con y sin caché |
| Frontend | Archivos estáticos en Cloudflare Pages / CDN | Sin carga sobre la VM |
| Resiliencia | Circuit breaker y timeout por establecimiento | Apagar el simulador de la posta y ver que la clínica sigue operando |

## Consecuencias
- (+) Todos los ítems de escalabilidad se demuestran con costo casi cero.
- (−) Las réplicas comparten CPU; la mejora medida será modesta. Se aclara en el informe que en producción cada réplica va en su propia máquina.
