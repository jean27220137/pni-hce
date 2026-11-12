# ADR-0006: Integración continua con Jenkins y SonarQube

- **Estado:** Aceptado
- **Fecha:** 2026-11-12

## Contexto
El curso exige CI/CD y análisis de calidad. No hay servidor en casa y el presupuesto es mínimo.

## Decisión
- **Jenkins** es el pipeline principal (`Jenkinsfile` en la raíz): build, tests, cobertura JaCoCo, análisis SonarQube, Quality Gate y despliegue manual a producción desde `main`.
- **SonarQube Community** analiza bugs, vulnerabilidades, *code smells*, duplicación y cobertura. El Quality Gate bloquea el pipeline si falla.
- Ambos corren **en la laptop con Docker Compose** (`infra/ci/`) y se apagan cuando no se usan.
- Sin URL pública no hay webhook de GitHub: Jenkins usa *polling* cada 5 minutos.
- **GitHub Actions** se mantiene como verificación rápida en la nube (esquema SQL), visible en cada push.

## Alternativas descartadas
- **SonarCloud:** gratis en repos públicos, pero el curso pide SonarQube en el flujo con Jenkins.
- **Jenkins en AWS:** encendido todo el semestre costaría ~US$30/mes.

## Consecuencias
- (+) Costo $0 y mismo flujo usado en proyectos anteriores.
- (−) El pipeline solo corre con la laptop encendida y Docker activo.
- (−) Requiere ~3-4 GB de RAM libres mientras corren.
