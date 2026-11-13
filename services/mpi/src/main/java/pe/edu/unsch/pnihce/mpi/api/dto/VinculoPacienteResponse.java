package pe.edu.unsch.pnihce.mpi.api.dto;

import java.util.List;

public record VinculoPacienteResponse(
        String dni,
        List<EstablecimientoVinculoDto> establecimientos,
        String fuente
) {}
