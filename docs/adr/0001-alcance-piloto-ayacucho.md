# ADR-0001: Alcance del piloto en Ayacucho con diseño escalable

- **Estado:** Aceptado
- **Fecha:** 2026-11-12

## Contexto
El docente aceptó el problema de comunicación entre dos establecimientos de salud que no comparten historias clínicas. El proyecto debe implementarse y desplegarse dentro del semestre 2026-2, con presupuesto académico mínimo. La propuesta inicial era nacional (5 nodos regionales).

## Decisión
- El prototipo implementa la comunicación entre **dos establecimientos de Ayacucho**: una posta del MINSA y una clínica privada, ambas simuladas.
- La arquitectura se diseña para escalar a nivel nacional, pero solo se despliega el **nodo Sur**.
- Concurrencia objetivo del piloto: **400-500 usuarios simultáneos** (≈709,000 hab., Censo 2025; proporción nacional de personal de salud). Proyección nacional: 15,000-25,000.

## Consecuencias
- (+) Alcance construible en el semestre y demostrable en vivo.
- (+) Las pruebas de carga con k6 usan una cifra sustentada (400-500 VUs).
- (−) La escalabilidad nacional se demuestra con diseño y mecanismos (réplicas, particionamiento), no con 5 nodos reales.
