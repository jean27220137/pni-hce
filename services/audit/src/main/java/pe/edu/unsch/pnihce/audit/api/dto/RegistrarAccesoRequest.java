package pe.edu.unsch.pnihce.audit.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrarAccesoRequest(
    @NotBlank
    @Pattern(regexp = "\\d{1}\\*{3}\\d{3,4}", message = "Se requiere DNI enmascarado")
    String dniMasked,

    String establecimientoOrigen,

    @NotBlank
    String renipressConsultado,

    @NotBlank
    String operacion,

    boolean exitoso,

    String detalle
) {}
