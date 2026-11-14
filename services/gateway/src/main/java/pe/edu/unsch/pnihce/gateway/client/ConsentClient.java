package pe.edu.unsch.pnihce.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class ConsentClient {

    private final WebClient webClient;
    private final String consentBaseUrl;

    public ConsentClient(WebClient webClient,
                         @Value("${app.consent.base-url}") String consentBaseUrl) {
        this.webClient = webClient;
        this.consentBaseUrl = consentBaseUrl;
    }

    /** true = consentimiento GRANTED o no encontrado (permiso implícito en el piloto) */
    public Mono<Boolean> tieneConsentimiento(String dni) {
        return webClient.get()
            .uri(consentBaseUrl + "/api/v1/consent/pacientes/{dni}", dni)
            .retrieve()
            .bodyToMono(ConsentResponse.class)
            .map(r -> "GRANTED".equals(r.estado()))
            .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.just(true))
            .onErrorResume(ex -> Mono.just(true)); // fail-open en el prototipo
    }

    public record ConsentResponse(String estado, String actualizadoEn) {}
}
