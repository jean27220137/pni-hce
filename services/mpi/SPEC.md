# SPEC — Servicio MPI (Índice Maestro de Pacientes)

- **Versión:** 0.1 · **Estado:** Borrador para aprobación
- **Paquete:** `pe.edu.unsch.pnihce.mpi`
- **Puerto (dev):** 8081 · **Ruta en gateway:** `/api/v1/mpi/**`

## 1. Propósito
Saber **dónde** tiene historial clínico cada paciente. El MPI vincula el DNI con los establecimientos donde fue atendido y con el identificador local del paciente en cada uno. **No almacena información clínica** (ADR-0002).

## 2. Modelo de datos

### `establecimiento`
| Campo | Tipo | Nota |
|---|---|---|
| `id` | UUID PK | |
| `codigo_renipress` | VARCHAR(20) UNIQUE | Código oficial IPRESS |
| `nombre` | VARCHAR(200) | |
| `tipo` | VARCHAR(20) | `POSTA`, `CENTRO_SALUD`, `HOSPITAL`, `CLINICA` |
| `sector` | VARCHAR(20) | `MINSA`, `ESSALUD`, `PRIVADO`, `FFAA_PNP` |
| `fhir_base_url` | VARCHAR(300) | Endpoint del adaptador FHIR |
| `activo` | BOOLEAN | |
| `creado_en` | TIMESTAMPTZ | |

### `vinculo_paciente` (particionada por rango de DNI)
| Campo | Tipo | Nota |
|---|---|---|
| `id` | UUID | |
| `dni` | CHAR(8) | Clave de partición |
| `establecimiento_id` | UUID FK | |
| `id_local` | VARCHAR(64) | ID del paciente en el HIS del establecimiento |
| `primera_atencion` | DATE | |
| `ultima_atencion` | DATE | |
| `creado_en` | TIMESTAMPTZ | |

- PK: `(dni, id)` · UNIQUE `(dni, establecimiento_id)`
- Particiones: `00000000-39999999`, `40000000-69999999`, `70000000-99999999`

## 3. Requisitos

| ID | Requisito |
|---|---|
| MPI-RF-01 | Registrar o actualizar la vinculación de un DNI con un establecimiento (idempotente por `dni + establecimiento`). |
| MPI-RF-02 | Buscar por DNI y devolver la lista de establecimientos con historial, ordenada por `ultima_atencion` descendente. |
| MPI-RF-03 | Validar que el DNI tenga exactamente 8 dígitos; si no, responder 400. |
| MPI-RF-04 | Rechazar vinculaciones a establecimientos inexistentes o inactivos (422). |
| MPI-RF-05 | Cachear el resultado de la búsqueda en Redis por 10 minutos e invalidarlo al registrar una vinculación de ese DNI. |
| MPI-RF-06 | Las búsquedas leen de la réplica de lectura; las escrituras van al primario. |
| MPI-RNF-01 | Búsqueda por DNI: p95 < 200 ms con caché y < 500 ms sin caché, con 500 usuarios concurrentes. |
| MPI-RNF-02 | El servicio es *stateless* y debe poder ejecutarse en N réplicas. |
| MPI-RNF-03 | Nunca escribir el DNI completo en logs (enmascarar: `7***1234`). |

## 4. API

### `PUT /api/v1/mpi/vinculos`
Registra o actualiza una vinculación (la llama el adaptador del establecimiento tras cada atención).
```json
{
  "dni": "71234567",
  "codigoRenipress": "00012345",
  "idLocal": "POSTA-HC-000981",
  "fechaAtencion": "2026-11-10"
}
```
- `200 OK` vinculación actualizada · `201 Created` nueva
- `400` DNI inválido · `422` establecimiento inexistente o inactivo

### `GET /api/v1/mpi/pacientes/{dni}/establecimientos`
```json
{
  "dni": "71234567",
  "establecimientos": [
    {
      "codigoRenipress": "00012345",
      "nombre": "Puesto de Salud Ejemplo",
      "tipo": "POSTA",
      "sector": "MINSA",
      "fhirBaseUrl": "http://posta-adapter:8091/fhir",
      "idLocal": "POSTA-HC-000981",
      "ultimaAtencion": "2026-11-10"
    }
  ],
  "fuente": "cache"
}
```
- `200 OK` (lista vacía si no hay historial) · `400` DNI inválido
- `fuente`: `cache` o `db` (sirve para demostrar el efecto de la caché)

### `GET /api/v1/mpi/establecimientos`
Lista de establecimientos activos.

## 5. Criterios de aceptación (base de los tests)
1. Registrar el mismo `dni + establecimiento` dos veces deja **un** registro y actualiza `ultima_atencion`.
2. Buscar un DNI sin vínculos devuelve `200` con lista vacía.
3. `GET` con DNI `"1234"` devuelve `400` con Problem Details.
4. La segunda búsqueda del mismo DNI devuelve `"fuente": "cache"`.
5. Tras un `PUT` del mismo DNI, la búsqueda siguiente devuelve `"fuente": "db"`.
6. Un DNI que empieza con `7` se guarda en la partición `70000000-99999999`.

## 6. Fuera de alcance
- Emparejamiento probabilístico de pacientes sin DNI (proyección).
- Consentimiento y auditoría (servicios propios; el gateway los orquesta).
