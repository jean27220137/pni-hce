package pe.edu.unsch.pnihce.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.unsch.pnihce.gateway.api.dto.HistoriaResponse;
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

    public Mono<HistoriaResponse> obtenerHistoria(String dni) {
        return mpiClient.buscarEstablecimientos(dni)
            .flatMap(mpiResp -> Flux.fromIterable(mpiResp.establecimientos())
                .flatMap(est -> fhirClient.getSummary(est.fhirBaseUrl(), est.idLocal())
                    .map(bundle -> new HistoriaResponse.EstablecimientoResumen(
                        est.codigoRenipress(), est.nombre(), est.tipo(), est.sector(),
                        est.ultimaAtencion(), true, bundle))
                    .defaultIfEmpty(new HistoriaResponse.EstablecimientoResumen(
                        est.codigoRenipress(), est.nombre(), est.tipo(), est.sector(),
                        est.ultimaAtencion(), false, null))
                )
                .collectList()
                .map(lista -> new HistoriaResponse(dni, lista))
            );
    }
}
