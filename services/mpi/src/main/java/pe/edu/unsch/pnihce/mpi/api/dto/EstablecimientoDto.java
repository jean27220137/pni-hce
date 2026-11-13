package pe.edu.unsch.pnihce.mpi.api.dto;

import java.util.UUID;

public record EstablecimientoDto(
        UUID id,
        String codigoRenipress,
        String nombre,
        String tipo,
        String sector,
        String fhirBaseUrl
) {}
