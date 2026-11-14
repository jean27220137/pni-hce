-- Esquema del Consent (desarrollo). En los servicios se migrará con Flyway.
CREATE SCHEMA IF NOT EXISTS consent;

CREATE TABLE consent.consentimiento (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dni_hash               CHAR(64)    NOT NULL UNIQUE,
    estado                 VARCHAR(10) NOT NULL CHECK (estado IN ('GRANTED','REVOKED')),
    establecimiento_origen VARCHAR(20),
    observacion            TEXT,
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_consentimiento_hash ON consent.consentimiento (dni_hash);

-- Consentimientos pre-cargados para los DNIs de prueba (hashes SHA-256)
INSERT INTO consent.consentimiento (dni_hash, estado, establecimiento_origen, observacion) VALUES
  ('62dba3eb5b6336c066e8157a60e2a1f15bd7f1675c5b0b9525169951053977ff', 'GRANTED', 'POSTA0001', 'Consentimiento inicial cargado en setup'),
  ('6a5ef7bdbf8db33f0192c3a106dbe58fd9651f5d7bf8b094897eebd2b5514194', 'GRANTED', 'POSTA0001', 'Consentimiento inicial cargado en setup'),
  ('ede81c8605421ddd5b89101b9edb68cb83a8c111fe86dde5bd420dd47e130563', 'GRANTED', 'CLINI0001', 'Consentimiento inicial cargado en setup');
