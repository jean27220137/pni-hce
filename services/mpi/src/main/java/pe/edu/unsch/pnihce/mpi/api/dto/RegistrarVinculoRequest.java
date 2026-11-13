package pe.edu.unsch.pnihce.mpi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record RegistrarVinculoRequest(
        @NotBlank
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
        String dni,

        @NotBlank
        String codigoRenipress,

        @NotBlank
        String idLocal,

        @NotNull
        LocalDate fechaAtencion
) {}
