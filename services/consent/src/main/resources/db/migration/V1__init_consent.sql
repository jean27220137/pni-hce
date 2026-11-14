CREATE SCHEMA IF NOT EXISTS consent;

CREATE TABLE consent.consentimiento (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    -- SHA-256(DNI) — nunca el DNI en claro (Ley 29733)
    dni_hash               CHAR(64)    NOT NULL UNIQUE,
    estado                 VARCHAR(10) NOT NULL CHECK (estado IN ('GRANTED','REVOKED')),
    establecimiento_origen VARCHAR(20),
    observacion            TEXT,
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_consentimiento_hash ON consent.consentimiento (dni_hash);
