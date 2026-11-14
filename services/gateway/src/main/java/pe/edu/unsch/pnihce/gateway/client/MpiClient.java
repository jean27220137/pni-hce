package pe.edu.unsch.pnihce.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Collections;

@Component
public class MpiClient {

    private final WebClient webClient;
    private final String mpiBaseUrl;

    public MpiClient(WebClient webClient, @Value("${app.mpi.base-url}") String mpiBaseUrl) {
        this.webClient = webClient;
        this.mpiBaseUrl = mpiBaseUrl;
    }

    public Mono<MpiResponse> buscarEstablecimientos(String dni) {
        return webClient.get()
            .uri(mpiBaseUrl + "/api/v1/mpi/pacientes/{dni}/establecimientos", dni)
            .retrieve()
            .bodyToMono(MpiResponse.class)
            .onErrorReturn(new MpiResponse(dni, Collections.emptyList(), "error"));
    }

    public record MpiResponse(String dni, List<EstablecimientoVinculo> establecimientos, String fuente) {}

    public record EstablecimientoVinculo(
        String codigoRenipress,
        String nombre,
        String tipo,
        String sector,
        String fhirBaseUrl,
        String idLocal,
        String ultimaAtencion
    ) {}
}
