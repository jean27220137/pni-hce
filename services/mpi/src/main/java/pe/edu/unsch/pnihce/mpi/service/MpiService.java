package pe.edu.unsch.pnihce.mpi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.unsch.pnihce.mpi.api.dto.*;
import pe.edu.unsch.pnihce.mpi.domain.Establecimiento;
import pe.edu.unsch.pnihce.mpi.domain.VinculoPaciente;
import pe.edu.unsch.pnihce.mpi.repository.EstablecimientoRepository;
import pe.edu.unsch.pnihce.mpi.repository.VinculoPacienteRepository;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpiService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final String CACHE_PREFIX = "mpi:dni:";

    private final EstablecimientoRepository establecimientoRepo;
    private final VinculoPacienteRepository vinculoRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * @return true si es una vinculación nueva, false si fue actualización (MPI-RF-01)
     */
    @Transactional
    public boolean registrarVinculo(RegistrarVinculoRequest req) {
        Establecimiento est = establecimientoRepo
                .findByCodigoRenipress(req.codigoRenipress())
                .filter(Establecimiento::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Establecimiento inexistente o inactivo: " + req.codigoRenipress()));

        var existente = vinculoRepo.findByDniAndEstablecimientoId(req.dni(), est.getId());

        boolean esNuevo;
        if (existente.isPresent()) {
            VinculoPaciente v = existente.get();
            if (req.fechaAtencion().isAfter(v.getUltimaAtencion())) {
                v.setUltimaAtencion(req.fechaAtencion());
                v.setIdLocal(req.idLocal());
            }
            esNuevo = false;
        } else {
            VinculoPaciente v = new VinculoPaciente();
            v.setDni(req.dni());
            v.setEstablecimiento(est);
            v.setIdLocal(req.idLocal());
            v.setPrimeraAtencion(req.fechaAtencion());
            v.setUltimaAtencion(req.fechaAtencion());
            vinculoRepo.save(v);
            esNuevo = true;
        }

        // Invalidar caché (MPI-RF-05)
        redisTemplate.delete(CACHE_PREFIX + req.dni());
        log.info("Vinculación registrada dni={} renipress={} nuevo={}", mask(req.dni()), req.codigoRenipress(), esNuevo);

        return esNuevo;
    }

    /** MPI-RF-02, MPI-RF-05, MPI-RF-06 */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public VinculoPacienteResponse buscarPorDni(String dni) {
        String cacheKey = CACHE_PREFIX + dni;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof VinculoPacienteResponse resp) {
            return resp;
        }

        List<VinculoPaciente> vinculos = vinculoRepo.findByDniOrderByUltimaAtencionDesc(dni);

        List<EstablecimientoVinculoDto> dtos = vinculos.stream()
                .map(v -> new EstablecimientoVinculoDto(
                        v.getEstablecimiento().getCodigoRenipress(),
                        v.getEstablecimiento().getNombre(),
                        v.getEstablecimiento().getTipo().name(),
                        v.getEstablecimiento().getSector().name(),
                        v.getEstablecimiento().getFhirBaseUrl(),
                        v.getIdLocal(),
                        v.getUltimaAtencion()))
                .toList();

        VinculoPacienteResponse response = new VinculoPacienteResponse(dni, dtos, "db");
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        log.debug("Búsqueda dni={} establecimientos={} fuente=db", mask(dni), dtos.size());
        return response;
    }

    /** MPI-RF-02 — lista establecimientos activos */
    @Transactional(readOnly = true)
    public List<EstablecimientoDto> listarEstablecimientosActivos() {
        return establecimientoRepo.findByActivoTrueOrderByNombre().stream()
                .map(e -> new EstablecimientoDto(
                        e.getId(),
                        e.getCodigoRenipress(),
                        e.getNombre(),
                        e.getTipo().name(),
                        e.getSector().name(),
                        e.getFhirBaseUrl()))
                .toList();
    }

    /** MPI-RNF-03: nunca loggear DNI completo */
    static String mask(String dni) {
        if (dni == null || dni.length() < 4) return "****";
        return dni.charAt(0) + "***" + dni.substring(dni.length() - 3);
    }
}
