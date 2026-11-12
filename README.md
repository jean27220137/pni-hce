# PNI-HCE

**Arquitectura de Software de Alta Concurrencia para la Interoperabilidad de Historias Clínicas Electrónicas**
Piloto: región Ayacucho · Curso IS-488 Arquitectura de Software · UNSCH 2026-2

Plataforma que permite que dos establecimientos de salud distintos (por ejemplo, una posta del MINSA y una clínica privada) intercambien historias clínicas electrónicas en tiempo real mediante HL7 FHIR R4, sin centralizar los datos (modelo federado).

## Flujo principal

1. El médico de la clínica busca al paciente por DNI.
2. El MPI responde en qué establecimientos tiene historial.
3. La plataforma consulta en vivo al adaptador FHIR de la posta.
4. Se verifica la firma digital del registro, el consentimiento del paciente y se registra el acceso en auditoría.
5. El historial llega al médico de la clínica.

## Estructura

| Carpeta | Contenido |
|---|---|
| `docs/adr/` | Decisiones de arquitectura |
| `services/gateway` | Spring Cloud Gateway |
| `services/mpi` | Índice Maestro de Pacientes |
| `services/fhir` | Servicio FHIR y verificación de firma digital |
| `services/consent` | Consentimiento del paciente |
| `services/audit` | Auditoría inmutable |
| `simulators/posta`, `simulators/clinica` | HIS ficticios con su adaptador FHIR |
| `frontend/` | Angular + PrimeNG |
| `infra/` | Docker Compose por ambiente, scripts SQL |
| `tests/e2e`, `tests/load` | Playwright y k6 |

Cada servicio tiene su `SPEC.md` (desarrollo basado en especificaciones, SDD): se escribe y aprueba antes de codificar.

## Ambientes

| Ambiente | Dónde | Comando |
|---|---|---|
| Desarrollo | Laptop | `docker compose -f infra/docker-compose.dev.yml up -d` |
| Pruebas | Laptop (perfil `qa`) y GitHub Actions | automático en cada push a `develop` |
| Producción | AWS EC2, solo de D-2 a D+1 de la presentación | aprobación manual sobre `main` |

## CI/CD

- **Jenkins** (`Jenkinsfile`): build, tests, cobertura, SonarQube, Quality Gate y despliegue manual desde `main`.
- **SonarQube**: calidad y cobertura.
- Ambos corren en la laptop: ver `infra/ci/README.md`.
- **GitHub Actions**: verificación rápida en la nube en cada push.

## Ramas

`feature/*` → `develop` (pruebas) → `main` (producción)

## Requisitos locales

Docker Desktop, Java 21, Node 20, Git.
