# PNI-HCE

**Arquitectura de Software de Alta Concurrencia para la Interoperabilidad de Historias Clínicas Electrónicas**
Piloto: región Ayacucho · IS-488 Arquitectura de Software · UNSCH 2026-2

Plataforma que permite que dos establecimientos de salud distintos (una posta del MINSA y una clínica privada) intercambien historias clínicas electrónicas en tiempo real mediante HL7 FHIR R4, sin centralizar los datos (modelo federado).

---

## Inicio rápido — desarrollo local

### Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Docker Desktop | 4.x (con compose plugin) |
| Java (JDK) | 21 |
| Node.js | 20 LTS |
| Maven | 3.9+ (o usar `./mvnw` incluido en cada servicio) |
| IDE | IntelliJ IDEA 2024.1+ **o** VS Code con extensiones Java |

---

### Paso 1 — Levantar la infraestructura con Docker

```bash
docker compose -f infra/docker-compose.dev.yml up -d postgres redis posta-adapter clinica-adapter
```

Esto inicia:
- **PostgreSQL 16** en `localhost:5432` — esquemas `mpi`, `audit`, `consent` creados automáticamente desde `infra/postgres/init/`
- **Redis 7** en `localhost:6379` — caché para el MPI
- **posta-adapter** en `localhost:8091` — simulador FHIR de la Posta de Salud San Juan Bautista
- **clinica-adapter** en `localhost:8092` — simulador FHIR de la Clínica Santa María

Verificar que todo está sano:
```bash
docker compose -f infra/docker-compose.dev.yml ps
```

**Datos de prueba precargados** (disponibles sin ninguna acción extra):

| DNI | Establecimientos | Consentimiento |
|---|---|---|
| `71234567` | Posta `POSTA-HC-000981` + Clínica `CLINI-HC-000101` | GRANTED |
| `45678901` | Posta `POSTA-HC-000982` | GRANTED |
| `48900123` | Clínica `CLINI-HC-000102` | GRANTED |

---

### Paso 2 — Abrir el proyecto en el IDE

#### IntelliJ IDEA

1. `File → Open` → seleccionar **`pom.xml` en la raíz del repositorio**.
2. IntelliJ detectará los 4 módulos Maven (`mpi`, `audit`, `consent`, `gateway`) automáticamente.
3. Las configuraciones de ejecución aparecerán en el menú desplegable:

```
0 — Docker Infra (postgres+redis+simulators)   ← docker compose desde el IDE (opcional)
1 — MPI :8081
2 — Audit :8082
3 — Consent :8083
4 — Gateway :8080
5 — Frontend :4200
```

Orden de arranque recomendado: **0 (si no levantaste Docker por terminal) → 1 → 2 → 3 → 4 → 5**

> **Nota:** los servicios MPI, Audit y Consent arrancan con `-Dspring.profiles.active=dev`,
> que apunta a `localhost:5432`. Gateway no necesita perfil porque no tiene base de datos.

#### VS Code

1. `File → Open Folder` → raíz del repositorio.
2. VS Code sugerirá instalar las extensiones recomendadas (`.vscode/extensions.json`). Aceptar.
3. Esperar a que el servidor de lenguaje Java indexe los 4 proyectos Maven (~2 min primera vez).
4. Abrir `Run and Debug` (Ctrl+Shift+D) → seleccionar y lanzar cada configuración en orden:
   - `1 — MPI :8081`
   - `2 — Audit :8082`
   - `3 — Consent :8083`
   - `4 — Gateway :8080`
5. Para lanzar todos los servicios Java a la vez: usar el **compound** `Todos los servicios (sin infra Docker)`.

---

### Paso 3 — Levantar el frontend Angular

```bash
cd frontend
npm install          # solo la primera vez
npm start            # equivale a: ng serve --proxy-config proxy.conf.json
```

El frontend queda disponible en **http://localhost:4200**

El `proxy.conf.json` enruta las llamadas API:

| Ruta | Destino |
|---|---|
| `/api/v1/mpi/**` | `http://localhost:8081` |
| `/api/v1/pacientes/**` | `http://localhost:8080` (gateway) |
| `/api/v1/consent/**` | `http://localhost:8083` |

---

### Resumen de puertos

| Servicio | Puerto | Perfil Spring |
|---|---|---|
| Gateway | 8080 | — |
| MPI | 8081 | `dev` |
| Audit | 8082 | `dev` |
| Consent | 8083 | `dev` |
| posta-adapter (FHIR) | 8091 | — (Docker) |
| clinica-adapter (FHIR) | 8092 | — (Docker) |
| Frontend | 4200 | — (Angular) |
| PostgreSQL | 5432 | — (Docker) |
| Redis | 6379 | — (Docker) |

---

### Flujo de demostración (DNI de prueba)

1. Abrir **http://localhost:4200/buscar**
2. Buscar DNI `71234567` → aparecen 2 establecimientos
3. Clic en **"Ver historia clínica completa"** → abre `/historia/71234567`
4. Se ven los 2 paneles FHIR (Posta + Clínica) con datos del simulador

---

### Tests

```bash
# Tests unitarios + integración de todos los servicios (requiere Docker para Testcontainers)
make test

# Solo un servicio
cd services/mpi && ./mvnw verify

# Tests E2E Playwright (requiere todo el stack corriendo)
make e2e

# Tests de carga k6 (requiere todo el stack corriendo + k6 instalado)
make test-load-mpi
make test-load-historia
make test-load-consent
```

---

## Estructura del repositorio

| Carpeta | Contenido |
|---|---|
| `services/gateway` | Orquestador: consent → MPI → FHIR adapters → audit |
| `services/mpi` | Índice Maestro de Pacientes (DNI ↔ establecimiento) |
| `services/consent` | Consentimiento informado (SHA-256 del DNI, Ley 29733) |
| `services/audit` | Registro de accesos inmutable |
| `simulators/` | HIS ficticios con adaptador FHIR R4 |
| `frontend/` | Angular 22 + PrimeNG 22 |
| `infra/` | Docker Compose (dev/prod), SQL de inicialización |
| `tests/e2e/` | Playwright |
| `tests/load/` | k6 |
| `docs/adr/` | Decisiones de arquitectura (ADR) |
| `.run/` | Configuraciones de ejecución compartidas (IntelliJ) |
| `.vscode/` | Configuraciones de ejecución compartidas (VS Code) |

Cada servicio tiene su `SPEC.md` (desarrollo basado en especificaciones): se escribe y aprueba antes de codificar.

---

## CI/CD

- **GitHub Actions** (`.github/workflows/ci.yml`): validación SQL, build + tests de los 4 servicios en paralelo, build Angular. Se ejecuta en cada push a `develop` y `main`.
- **Jenkins** (`Jenkinsfile`): build, tests, SonarQube, Quality Gate y despliegue manual desde `main`. Corre en la laptop: ver `infra/ci/README.md`.

## Ramas

```
feature/* → develop (pruebas) → main (producción)
```

## Ambientes

| Ambiente | Dónde | Comando |
|---|---|---|
| Desarrollo | Laptop (IDE + Docker infra) | ver arriba |
| QA / CI | GitHub Actions | automático en cada push |
| Producción | AWS EC2 | aprobación manual en `main` |
