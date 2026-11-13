package pe.edu.unsch.pnihce.audit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "acceso", schema = "audit")
@Getter
@Setter
public class Acceso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dni_masked", nullable = false)
    private String dniMasked;

    @Column(name = "establecimiento_origen")
    private String establecimientoOrigen;

    @Column(name = "renipress_consultado", nullable = false)
    private String renipressConsultado;

    @Column(nullable = false)
    private String operacion;

    @Column(nullable = false)
    private boolean exitoso;

    private String detalle;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void prePersist() { creadoEn = OffsetDateTime.now(); }
}
