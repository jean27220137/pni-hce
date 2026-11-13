package pe.edu.unsch.pnihce.gateway.api.dto;

import java.util.List;
import java.util.Map;

public record HistoriaResponse(
    String dni,
    List<EstablecimientoResumen> establecimientos
) {
    public record EstablecimientoResumen(
        String codigoRenipress,
        String nombre,
        String tipo,
        String sector,
        String ultimaAtencion,
        boolean disponible,
        Map<String, Object> resumenFhir
    ) {}
}
