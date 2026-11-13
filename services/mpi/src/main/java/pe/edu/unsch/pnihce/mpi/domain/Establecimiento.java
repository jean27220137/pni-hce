package pe.edu.unsch.pnihce.mpi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "establecimiento", schema = "mpi")
@Getter
@Setter
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codigo_renipress", nullable = false, unique = true)
    private String codigoRenipress;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEstablecimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SectorEstablecimiento sector;

    @Column(name = "fhir_base_url", nullable = false)
    private String fhirBaseUrl;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = OffsetDateTime.now();
    }
}
