# SPEC — Servicio Audit (Registro de Accesos)

- **Versión:** 0.1 · **Estado:** Borrador
- **Paquete:** `pe.edu.unsch.pnihce.audit`
- **Puerto (dev):** 8082 · **Ruta en gateway:** `/api/v1/audit/**`

## 1. Propósito
Registrar cada intento de acceso a una historia clínica para cumplir con el trazabilidad requerida por Ley 29733. El DNI nunca se registra en claro: solo la versión enmascarada (`7***567`).

## 2. Modelo de datos

### `acceso`
| Campo | Tipo | Nota |
|---|---|---|
| `id` | UUID PK | |
| `dni_masked` | VARCHAR(10) NOT NULL | DNI enmascarado (nunca en claro) |
| `establecimiento_origen` | VARCHAR(20) | Establecimiento que inició la consulta |
| `renipress_consultado` | VARCHAR(20) NOT NULL | Establecimiento cuya HCE fue consultada |
| `operacion` | VARCHAR(30) NOT NULL | Ej: `CONSULTA_HISTORIA` |
| `exitoso` | BOOLEAN NOT NULL | Si la consulta al adaptador FHIR fue exitosa |
| `detalle` | TEXT | Información adicional opcional |
| `creado_en` | TIMESTAMPTZ NOT NULL | Inmutable |

## 3. Requisitos funcionales

| ID | Descripción |
|---|---|
| AUD-RF-01 | Registrar un evento de acceso (`POST /accesos`). Devuelve 201 sin cuerpo. |
| AUD-RF-02 | El campo `dniMasked` debe cumplir el formato enmascarado (1 dígito + 3 asteriscos + 3-4 dígitos). |
| AUD-RF-03 | Los campos `renipressConsultado` y `operacion` son obligatorios; si faltan, 400 con ProblemDetail. |
| AUD-RF-04 | Cada registro es inmutable (`creado_en` se asigna en `@PrePersist`, no actualizable). |

## 4. Requisitos no funcionales

| ID | Descripción |
|---|---|
| AUD-RNF-01 | El gateway llama al audit con fire-and-forget: un fallo del servicio no bloquea la respuesta al usuario. |
