package pe.edu.unsch.pnihce.consent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsch.pnihce.consent.domain.Consentimiento;

import java.util.Optional;
import java.util.UUID;

public interface ConsentimientoRepository extends JpaRepository<Consentimiento, UUID> {
    Optional<Consentimiento> findByDniHash(String dniHash);
}
