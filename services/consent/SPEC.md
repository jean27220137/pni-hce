# SPEC — Servicio Consent (Gestión de Consentimiento)

- **Versión:** 0.1 · **Estado:** Borrador
- **Paquete:** `pe.edu.unsch.pnihce.consent`
- **Puerto (dev):** 8083 · **Ruta en gateway:** `/api/v1/consent/**`

## 1. Propósito
Registrar y consultar el consentimiento del paciente para que sus datos clínicos sean accedidos a través de la plataforma. Aplica Ley 29733 (Protección de Datos Personales): el DNI nunca se almacena en claro; se usa el hash SHA-256.

## 2. Modelo de datos

### `consentimiento`
| Campo | Tipo | Nota |
|---|---|---|
| `id` | UUID PK | |
| `dni_hash` | CHAR(64) UNIQUE | SHA-256 del DNI |
| `estado` | VARCHAR(10) | `GRANTED` o `REVOKED` |
| `establecimiento_origen` | VARCHAR(20) | Establecimiento que registró el consentimiento |
| `observacion` | TEXT | Texto libre opcional |
| `creado_en` | TIMESTAMPTZ | Inmutable |
| `actualizado_en` | TIMESTAMPTZ | Actualiza en cada PUT |

## 3. Requisitos funcionales

| ID | Descripción |
|---|---|
| CONS-RF-01 | Consultar consentimiento por DNI (`GET /pacientes/{dni}`). Devuelve 404 si no existe. |
| CONS-RF-02 | Registrar consentimiento (`PUT /pacientes/{dni}`). Devuelve 201 si es nuevo, 200 si actualiza. |
| CONS-RF-03 | El DNI en el path se valida: exactamente 8 dígitos; si no, 400 con ProblemDetail. |
| CONS-RF-04 | El estado en el body acepta solo `GRANTED` o `REVOKED`; si no, 400 con ProblemDetail. |
| CONS-RF-05 | El DNI nunca se almacena en claro: se persiste el hash SHA-256. |

## 4. Requisitos no funcionales

| ID | Descripción |
|---|---|
| CONS-RNF-01 | Tiempo de respuesta p95 < 100ms (consulta con índice sobre `dni_hash`). |
