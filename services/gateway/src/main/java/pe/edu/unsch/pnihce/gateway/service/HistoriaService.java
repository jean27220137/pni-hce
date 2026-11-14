package pe.edu.unsch.pnihce.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.unsch.pnihce.gateway.api.dto.HistoriaResponse;
import pe.edu.unsch.pnihce.gateway.client.AuditClient;
import pe.edu.unsch.pnihce.gateway.exception.AccesoDenegadoException;
import pe.edu.unsch.pnihce.gateway.client.ConsentClient;
import pe.edu.unsch.pnihce.gateway.client.FhirClient;
import pe.edu.unsch.pnihce.gateway.client.MpiClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoriaService {

    private final MpiClient mpiClient;
    private final FhirClient fhirClient;
    private final ConsentClient consentClient;
    private final AuditClient auditClient;

    public Mono<HistoriaResponse> obtenerHistoria(String dni) {
        String dniMasked = AuditClient.mask(dni);

        return consentClient.tieneConsentimiento(dni)
            .flatMap(tieneConsent -> {
                if (!tieneConsent) {
                    log.info("Acceso denegado por falta de consentimiento para DNI {}", dniMasked);
                    return Mono.error(new AccesoDenegadoException());
                }
                return mpiClient.buscarEstablecimientos(dni)
                    .flatMap(mpiResp -> Flux.fromIterable(mpiResp.establecimientos())
                        .flatMap(est -> fhirClient.getSummary(est.fhirBaseUrl(), est.idLocal())
                            .map(bundle -> new HistoriaResponse.EstablecimientoResumen(
                                est.codigoRenipress(), est.nombre(), est.tipo(), est.sector(),
                                est.ultimaAtencion(), true, bundle))
                            .defaultIfEmpty(new HistoriaResponse.EstablecimientoResumen(
                                est.codigoRenipress(), est.nombre(), est.tipo(), est.sector(),
                                est.ultimaAtencion(), false, null))
                            .doOnNext(r -> auditClient.registrar(dniMasked, est.codigoRenipress(), r.disponible()))
                        )
                        .collectList()
                        .map(lista -> new HistoriaResponse(dni, lista))
                    );
            });
    }
}
