package pe.edu.unsch.pnihce.consent.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.unsch.pnihce.consent.api.dto.ConsentimientoRequest;
import pe.edu.unsch.pnihce.consent.api.dto.ConsentimientoResponse;
import pe.edu.unsch.pnihce.consent.domain.Consentimiento;
import pe.edu.unsch.pnihce.consent.repository.ConsentimientoRepository;
import pe.edu.unsch.pnihce.consent.util.DniHasher;

@RestController
@RequestMapping("/api/v1/consent")
@RequiredArgsConstructor
@Validated
public class ConsentController {

    private final ConsentimientoRepository repo;

    /** Consulta si el paciente tiene consentimiento activo. */
    @GetMapping("/pacientes/{dni}")
    @Transactional(readOnly = true)
    public ResponseEntity<ConsentimientoResponse> consultar(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
            String dni) {

        return repo.findByDniHash(DniHasher.hash(dni))
            .map(c -> ResponseEntity.ok(new ConsentimientoResponse(c.getEstado(), c.getActualizadoEn())))
            .orElse(ResponseEntity.notFound().build());
    }

    /** Registra o actualiza el consentimiento (idempotente). */
    @PutMapping("/pacientes/{dni}")
    @Transactional
    public ResponseEntity<ConsentimientoResponse> registrar(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
            String dni,
            @Valid @RequestBody ConsentimientoRequest req) {

        String hash = DniHasher.hash(dni);
        Consentimiento c = repo.findByDniHash(hash).orElseGet(Consentimiento::new);

        boolean esNuevo = c.getId() == null;
        c.setDniHash(hash);
        c.setEstado(req.estado());
        c.setEstablecimientoOrigen(req.establecimientoOrigen());
        c.setObservacion(req.observacion());
        repo.save(c);

        var resp = ResponseEntity.ok(new ConsentimientoResponse(c.getEstado(), c.getActualizadoEn()));
        return esNuevo ? ResponseEntity.status(201).body(resp.getBody()) : resp;
    }
}
