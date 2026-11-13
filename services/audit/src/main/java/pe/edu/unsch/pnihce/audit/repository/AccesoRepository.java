package pe.edu.unsch.pnihce.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.unsch.pnihce.audit.domain.Acceso;

import java.util.UUID;

public interface AccesoRepository extends JpaRepository<Acceso, UUID> {}
