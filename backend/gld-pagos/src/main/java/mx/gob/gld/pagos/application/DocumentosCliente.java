package mx.gob.gld.pagos.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.web.ClienteInterno;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DocumentosCliente {

    private final RestClient http;
    private final ObjectMapper json;
    private final GldProperties properties;

    public DocumentosCliente(RestClient http, ObjectMapper json, GldProperties properties) {
        this.http = http;
        this.json = json;
        this.properties = properties;
    }

    public UUID emitir(String tenant, Emitir cuerpo) {
        Emitido emitido = ClienteInterno.postInterno(http, json,
                properties.getServices().getDocumentos() + "/api/v1/documentos/interno/emitir",
                cuerpo, Emitido.class, properties.getInternalToken(), tenant);
        if (emitido == null || emitido.id() == null) {
            throw Errores.servicioNoDisponible();
        }
        return emitido.id();
    }

    public record Emitir(
            String folio,
            String tipo,
            UUID transaccionId,
            UUID tramiteId,
            String titular,
            String concepto,
            BigDecimal monto
    ) {
    }

    public record Emitido(UUID id, String folio) {
    }
}
