# ADR-0005: Firma digital de los registros clínicos

- **Estado:** Aceptado
- **Fecha:** 2026-11-12

## Contexto
La Ley 30024 define la historia clínica electrónica como la refrendada con firma digital del profesional tratante; sin firma es solo "informatizada". La Ley 27269 y la RM 462-2023/MINSA regulan su uso en actos médicos.

## Decisión
- El establecimiento origen firma cada documento clínico (Bundle FHIR) con el certificado del profesional, en formato **JWS** (RS256), y lo adjunta en `Bundle.signature`.
- PNI-HCE **verifica** la firma al recibir el documento y lo rechaza si es inválida (RF-11).
- **Prototipo:** autoridad certificadora propia creada con OpenSSL; certificados X.509 para médicos ficticios.
- **Producción:** certificados de la ECEP-RENIEC (DNIe) y la plataforma Firma Perú (Agente para firmar, Validador para verificar).

## Consecuencias
- (+) Integridad y no repudio verificables de extremo a extremo.
- (−) La CA propia no tiene validez legal; se declara explícitamente como simulación.
