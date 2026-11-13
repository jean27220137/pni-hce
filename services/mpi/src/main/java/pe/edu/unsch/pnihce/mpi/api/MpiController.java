package pe.edu.unsch.pnihce.mpi.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.unsch.pnihce.mpi.api.dto.*;
import pe.edu.unsch.pnihce.mpi.service.MpiService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mpi")
@RequiredArgsConstructor
@Validated
public class MpiController {

    private final MpiService mpiService;

    @PutMapping("/vinculos")
    public ResponseEntity<Void> registrarVinculo(@Valid @RequestBody RegistrarVinculoRequest req) {
        boolean esNuevo = mpiService.registrarVinculo(req);
        if (esNuevo) {
            var location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{dni}")
                    .buildAndExpand(req.dni())
                    .toUri();
            return ResponseEntity.created(location).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pacientes/{dni}/establecimientos")
    public ResponseEntity<VinculoPacienteResponse> buscarPaciente(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
            String dni) {
        return ResponseEntity.ok(mpiService.buscarPorDni(dni));
    }

    @GetMapping("/establecimientos")
    public ResponseEntity<List<EstablecimientoDto>> listarEstablecimientos() {
        return ResponseEntity.ok(mpiService.listarEstablecimientosActivos());
    }
}
