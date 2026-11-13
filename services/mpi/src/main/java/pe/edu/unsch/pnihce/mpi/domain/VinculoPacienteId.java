package pe.edu.unsch.pnihce.mpi.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VinculoPacienteId implements Serializable {
    private String dni;
    private UUID id;
}
