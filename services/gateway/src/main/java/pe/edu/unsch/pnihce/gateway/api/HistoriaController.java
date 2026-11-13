package pe.edu.unsch.pnihce.gateway.api;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.unsch.pnihce.gateway.api.dto.HistoriaResponse;
import pe.edu.unsch.pnihce.gateway.service.HistoriaService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Validated
public class HistoriaController {

    private final HistoriaService historiaService;

    /**
     * Orquesta MPI → FHIR adapters → respuesta agregada.
     * Las llamadas a los adaptadores se ejecutan en paralelo (Flux.flatMap).
     */
    @GetMapping("/{dni}/historia")
    public Mono<ResponseEntity<HistoriaResponse>> obtenerHistoria(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
            String dni) {
        return historiaService.obtenerHistoria(dni)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
