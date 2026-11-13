package pe.edu.unsch.pnihce.mpi.api.dto;

import java.time.LocalDate;

public record EstablecimientoVinculoDto(
        String codigoRenipress,
        String nombre,
        String tipo,
        String sector,
        String fhirBaseUrl,
        String idLocal,
        LocalDate ultimaAtencion
) {}
