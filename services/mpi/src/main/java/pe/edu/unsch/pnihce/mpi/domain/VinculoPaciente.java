package pe.edu.unsch.pnihce.mpi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vinculo_paciente", schema = "mpi")
@IdClass(VinculoPacienteId.class)
@Getter
@Setter
public class VinculoPaciente {

    @Id
    @Column(length = 8)
    private String dni;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "id_local", nullable = false)
    private String idLocal;

    @Column(name = "primera_atencion", nullable = false)
    private LocalDate primeraAtencion;

    @Column(name = "ultima_atencion", nullable = false)
    private LocalDate ultimaAtencion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = OffsetDateTime.now();
    }
}
