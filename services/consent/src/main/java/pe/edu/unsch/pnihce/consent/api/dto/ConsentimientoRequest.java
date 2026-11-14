package pe.edu.unsch.pnihce.consent.api.dto;

import jakarta.validation.constraints.NotNull;
import pe.edu.unsch.pnihce.consent.domain.EstadoConsentimiento;

public record ConsentimientoRequest(
    @NotNull
    EstadoConsentimiento estado,

    String establecimientoOrigen,
    String observacion
) {}
