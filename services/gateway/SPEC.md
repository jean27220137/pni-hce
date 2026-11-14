# SPEC — Gateway (Orquestador de Historia Clínica)

- **Versión:** 0.1 · **Estado:** Borrador
- **Paquete:** `pe.edu.unsch.pnihce.gateway`
- **Puerto (dev):** 8080

## 1. Propósito
Punto único de entrada para los clientes (frontend, apps móviles). Orquesta el flujo: valida consentimiento → consulta MPI → llama en paralelo a los adaptadores FHIR → agrega respuesta. Nunca almacena información clínica.

## 2. Flujo principal

```
Cliente → GET /api/v1/pacientes/{dni}/historia
  1. ConsentClient.tieneConsentimiento(dni)
     → false: 403 Forbidden (ProblemDetail)
  2. MpiClient.buscarEstablecimientos(dni)
     → lista de establecimientos con fhirBaseUrl + idLocal
  3. Flux.flatMap: FhirClient.getSummary(fhirBaseUrl, idLocal) [paralelo]
     → disponible=true con bundle, o disponible=false si falla
  4. AuditClient.registrar(dniMasked, renipress, exitoso) [fire-and-forget]
  5. HistoriaResponse con lista de EstablecimientoResumen
```

## 3. Requisitos funcionales

| ID | Descripción |
|---|---|
| GW-RF-01 | `GET /api/v1/pacientes/{dni}/historia` devuelve la historia agregada. |
| GW-RF-02 | Si el paciente no tiene consentimiento, devuelve 403 con ProblemDetail. |
| GW-RF-03 | DNI debe tener exactamente 8 dígitos; si no, 400 con ProblemDetail. |
| GW-RF-04 | Las llamadas a los adaptadores FHIR se ejecutan en paralelo. |
| GW-RF-05 | Si un adaptador FHIR falla, el establecimiento aparece con `disponible=false` (soft circuit break). |
| GW-RF-06 | El DNI nunca se registra en logs: solo la forma enmascarada. |
| GW-RF-07 | Cada acceso queda registrado en el servicio Audit (fire-and-forget). |

## 4. Requisitos no funcionales

| ID | Descripción |
|---|---|
| GW-RNF-01 | Tiempo de respuesta p95 < 2000ms con todos los adaptadores disponibles (200 VUs). |
| GW-RNF-02 | El fallo del servicio Audit o Consent no bloquea la respuesta al usuario (fail-open). |
| GW-RNF-03 | Virtual threads habilitados (`spring.threads.virtual.enabled=true`). |
