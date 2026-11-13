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
