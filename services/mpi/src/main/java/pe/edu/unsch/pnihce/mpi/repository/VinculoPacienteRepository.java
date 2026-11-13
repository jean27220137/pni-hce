package pe.edu.unsch.pnihce.mpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsch.pnihce.mpi.domain.VinculoPaciente;
import pe.edu.unsch.pnihce.mpi.domain.VinculoPacienteId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VinculoPacienteRepository extends JpaRepository<VinculoPaciente, VinculoPacienteId> {

    List<VinculoPaciente> findByDniOrderByUltimaAtencionDesc(String dni);

    Optional<VinculoPaciente> findByDniAndEstablecimientoId(String dni, UUID establecimientoId);
}
