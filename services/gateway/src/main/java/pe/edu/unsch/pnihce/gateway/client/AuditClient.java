package pe.edu.unsch.pnihce.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuditClient {

    private final WebClient webClient;
    private final String auditBaseUrl;

    public AuditClient(WebClient webClient,
                       @Value("${app.audit.base-url}") String auditBaseUrl) {
        this.webClient = webClient;
        this.auditBaseUrl = auditBaseUrl;
    }

    /** Fire-and-forget: no bloquear el flujo si audit falla */
    public void registrar(String dniMasked, String renipressConsultado, boolean exitoso) {
        webClient.post()
            .uri(auditBaseUrl + "/api/v1/audit/accesos")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "dniMasked", dniMasked,
                "renipressConsultado", renipressConsultado,
                "operacion", "CONSULTA_HISTORIA",
                "exitoso", exitoso
            ))
            .retrieve()
            .toBodilessEntity()
            .onErrorComplete()
            .subscribe();
    }

    public static String mask(String dni) {
        if (dni == null || dni.length() < 4) return "****";
        return dni.charAt(0) + "***" + dni.substring(dni.length() - 3);
    }
}
