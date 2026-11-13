package pe.edu.unsch.pnihce.mpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsch.pnihce.mpi.domain.Establecimiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, UUID> {

    Optional<Establecimiento> findByCodigoRenipress(String codigoRenipress);

    List<Establecimiento> findByActivoTrueOrderByNombre();
}
