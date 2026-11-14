package pe.edu.unsch.pnihce.consent.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consentimiento", schema = "consent")
@Getter
@Setter
public class Consentimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dni_hash", nullable = false, unique = true, length = 64)
    private String dniHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConsentimiento estado;

    @Column(name = "establecimiento_origen")
    private String establecimientoOrigen;

    private String observacion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = actualizadoEn = OffsetDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
