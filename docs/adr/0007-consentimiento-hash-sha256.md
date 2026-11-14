# ADR-0007: Almacenamiento de consentimiento con hash SHA-256 del DNI

- **Estado:** Aceptado
- **Fecha:** 2026-11-13

## Contexto
El servicio de consentimiento necesita relacionar el consentimiento (`GRANTED`/`REVOKED`) con un paciente. La Ley 29733 (Ley de Protección de Datos Personales) prohíbe almacenar datos personales innecesariamente. Almacenar el DNI en claro en la tabla `consentimiento` crearía un registro con dato personal directamente identificable.

## Decisión
Se almacena el hash SHA-256 del DNI (`CHAR(64)`) en lugar del DNI en claro. La función `DniHasher.hash(dni)` aplica `MessageDigest("SHA-256")` sobre el DNI en bytes UTF-8 y devuelve la representación hexadecimal.

## Consecuencias

**Positivas:**
- Cumplimiento de Ley 29733: el hash no es reversible; un atacante que obtenga la base de datos no puede recuperar el DNI directamente.
- El índice único sobre `dni_hash` mantiene rendimiento O(1) en la consulta por paciente.

**Negativas:**
- No es posible consultar "todos los consentimientos de pacientes con DNI que empiezan con 7X…" (búsqueda por rango). Aceptable para el prototipo porque la única operación es GET/PUT por DNI exacto.
- Si el algoritmo SHA-256 se considera insuficiente en el futuro (pre-imagen teórica), será necesaria una migración de hashes. Se mitiga con la baja entropía del espacio DNI peruano, que ya lo hace resistente a rainbow tables sin salt. Para producción real se recomendaría agregar salt por instancia.
