package mx.gob.gld.incidencias.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

public final class IncidenciaDtos {

    private IncidenciaDtos() {
    }

    public record VistaPreviaRequest(BigDecimal lat, BigDecimal lng) {
    }

    public record VistaPreviaResponse(String direccionReferencia) {
    }

    public record AltaRequest(
            String tipo,
            BigDecimal lat,
            BigDecimal lng,
            String direccionReferencia,
            String descripcion,
            String fotoBase64,
            String fotoContentType
    ) {
    }

    public record BrigadaResponse(String nombre, String zona) {
    }

    public record IncidenciaResponse(
            String folio,
            String tipo,
            String estado,
            String direccionReferencia,
            BigDecimal lat,
            BigDecimal lng,
            String descripcion,
            BrigadaResponse brigada,
            Instant creadaEn,
            @JsonInclude(JsonInclude.Include.NON_NULL) Boolean tieneFoto
    ) {
    }
}
