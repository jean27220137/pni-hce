package pe.edu.unsch.pnihce.consent.api.dto;

import pe.edu.unsch.pnihce.consent.domain.EstadoConsentimiento;

import java.time.OffsetDateTime;

public record ConsentimientoResponse(
    EstadoConsentimiento estado,
    OffsetDateTime actualizadoEn
) {}
