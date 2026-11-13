package pe.edu.unsch.pnihce.gateway.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class FhirClient {

    private final WebClient webClient;

    public FhirClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getSummary(String fhirBaseUrl, String idLocal) {
        return webClient.get()
            .uri(fhirBaseUrl + "/Patient/{id}/$summary", idLocal)
            .retrieve()
            .bodyToMono((Class<Map<String, Object>>) (Class<?>) Map.class)
            .onErrorResume(WebClientResponseException.NotFound.class,
                ex -> Mono.empty())
            .onErrorResume(ex -> {
                // Adaptador no disponible — circuit break suave: devolver vacío
                return Mono.empty();
            });
    }
}
