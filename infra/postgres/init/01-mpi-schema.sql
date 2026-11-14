-- Esquema del MPI (desarrollo). En los servicios se migrará con Flyway.
CREATE SCHEMA IF NOT EXISTS mpi;

CREATE TABLE mpi.establecimiento (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_renipress VARCHAR(20)  NOT NULL UNIQUE,
    nombre           VARCHAR(200) NOT NULL,
    tipo             VARCHAR(20)  NOT NULL CHECK (tipo IN ('POSTA','CENTRO_SALUD','HOSPITAL','CLINICA')),
    sector           VARCHAR(20)  NOT NULL CHECK (sector IN ('MINSA','ESSALUD','PRIVADO','FFAA_PNP')),
    fhir_base_url    VARCHAR(300) NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Tabla particionada por rango de DNI (MPI-RNF-02, ADR-0004)
CREATE TABLE mpi.vinculo_paciente (
    id                 UUID        NOT NULL DEFAULT gen_random_uuid(),
    dni                CHAR(8)     NOT NULL CHECK (dni ~ '^[0-9]{8}$'),
    establecimiento_id UUID        NOT NULL REFERENCES mpi.establecimiento(id),
    id_local           VARCHAR(64) NOT NULL,
    primera_atencion   DATE        NOT NULL,
    ultima_atencion    DATE        NOT NULL,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (dni, id),
    UNIQUE (dni, establecimiento_id)
) PARTITION BY RANGE (dni);

CREATE TABLE mpi.vinculo_paciente_p0 PARTITION OF mpi.vinculo_paciente
    FOR VALUES FROM ('00000000') TO ('40000000');
CREATE TABLE mpi.vinculo_paciente_p1 PARTITION OF mpi.vinculo_paciente
    FOR VALUES FROM ('40000000') TO ('70000000');
CREATE TABLE mpi.vinculo_paciente_p2 PARTITION OF mpi.vinculo_paciente
    FOR VALUES FROM ('70000000') TO (MAXVALUE);

CREATE INDEX idx_vinculo_dni ON mpi.vinculo_paciente (dni);

-- Establecimientos ficticios de Ayacucho
INSERT INTO mpi.establecimiento (codigo_renipress, nombre, tipo, sector, fhir_base_url) VALUES
  ('POSTA0001', 'Puesto de Salud San Juan Bautista (Ayacucho)', 'POSTA',   'MINSA',   'http://posta-adapter:8091/fhir'),
  ('CLINI0001', 'Clínica Santa María (Ayacucho)',               'CLINICA', 'PRIVADO', 'http://clinica-adapter:8092/fhir');

-- Vínculos de prueba para demo y tests de carga
-- DNI 71234567: María Flores Torres — atendida en ambos establecimientos
-- DNI 45678901: Juan Carlos Ayala — solo en la posta
-- DNI 48900123: Rosa Medina Quispe — solo en la clínica
INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '71234567', id, 'POSTA-HC-000981', '2024-03-15', '2026-10-05'
FROM mpi.establecimiento WHERE codigo_renipress = 'POSTA0001';

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '71234567', id, 'CLINI-HC-000101', '2025-06-01', '2026-09-15'
FROM mpi.establecimiento WHERE codigo_renipress = 'CLINI0001';

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '45678901', id, 'POSTA-HC-000982', '2023-01-20', '2026-07-10'
FROM mpi.establecimiento WHERE codigo_renipress = 'POSTA0001';

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '48900123', id, 'CLINI-HC-000102', '2026-08-20', '2026-08-20'
FROM mpi.establecimiento WHERE codigo_renipress = 'CLINI0001';
