CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE audit.acceso (
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dni_masked             VARCHAR(10) NOT NULL,
    establecimiento_origen VARCHAR(20),
    renipress_consultado   VARCHAR(20) NOT NULL,
    operacion              VARCHAR(30) NOT NULL,
    exitoso                BOOLEAN     NOT NULL,
    detalle                TEXT,
    creado_en              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_acceso_dni ON audit.acceso (dni_masked);
CREATE INDEX idx_acceso_fecha ON audit.acceso (creado_en DESC);
