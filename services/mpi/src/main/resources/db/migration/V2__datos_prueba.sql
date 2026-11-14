-- Datos de prueba para desarrollo y tests de integración.
-- En producción esta migración se omite con spring.flyway.target=1 o usando perfiles.

INSERT INTO mpi.establecimiento (codigo_renipress, nombre, tipo, sector, fhir_base_url) VALUES
  ('POSTA0001', 'Puesto de Salud San Juan Bautista (Ayacucho)', 'POSTA',   'MINSA',   'http://posta-adapter:8091/fhir'),
  ('CLINI0001', 'Clínica Santa María (Ayacucho)',               'CLINICA', 'PRIVADO', 'http://clinica-adapter:8092/fhir')
ON CONFLICT (codigo_renipress) DO NOTHING;

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '71234567', id, 'POSTA-HC-000981', '2024-03-15', '2026-10-05'
FROM mpi.establecimiento WHERE codigo_renipress = 'POSTA0001'
ON CONFLICT (dni, establecimiento_id) DO NOTHING;

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '71234567', id, 'CLINI-HC-000101', '2025-06-01', '2026-09-15'
FROM mpi.establecimiento WHERE codigo_renipress = 'CLINI0001'
ON CONFLICT (dni, establecimiento_id) DO NOTHING;

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '45678901', id, 'POSTA-HC-000982', '2023-01-20', '2026-07-10'
FROM mpi.establecimiento WHERE codigo_renipress = 'POSTA0001'
ON CONFLICT (dni, establecimiento_id) DO NOTHING;

INSERT INTO mpi.vinculo_paciente (dni, establecimiento_id, id_local, primera_atencion, ultima_atencion)
SELECT '48900123', id, 'CLINI-HC-000102', '2026-08-20', '2026-08-20'
FROM mpi.establecimiento WHERE codigo_renipress = 'CLINI0001'
ON CONFLICT (dni, establecimiento_id) DO NOTHING;
