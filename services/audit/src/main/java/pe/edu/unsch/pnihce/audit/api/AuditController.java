package pe.edu.unsch.pnihce.audit.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.unsch.pnihce.audit.api.dto.RegistrarAccesoRequest;
import pe.edu.unsch.pnihce.audit.domain.Acceso;
import pe.edu.unsch.pnihce.audit.repository.AccesoRepository;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AccesoRepository repo;

    @PostMapping("/accesos")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistrarAccesoRequest req) {
        Acceso a = new Acceso();
        a.setDniMasked(req.dniMasked());
        a.setEstablecimientoOrigen(req.establecimientoOrigen());
        a.setRenipressConsultado(req.renipressConsultado());
        a.setOperacion(req.operacion());
        a.setExitoso(req.exitoso());
        a.setDetalle(req.detalle());
        repo.save(a);
        return ResponseEntity.created(null).build();
    }
}
