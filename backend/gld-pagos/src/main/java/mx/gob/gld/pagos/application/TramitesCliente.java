package mx.gob.gld.pagos.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.web.ClienteInterno;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class TramitesCliente {

    private final RestClient http;
    private final ObjectMapper json;
    private final GldProperties properties;

    public TramitesCliente(RestClient http, ObjectMapper json, GldProperties properties) {
        this.http = http;
        this.json = json;
        this.properties = properties;
    }

    public void pagado(String tenant, String folio, UUID liquidacionId) {
        ClienteInterno.postInternoVacio(http, json,
                properties.getServices().getTramites() + "/api/v1/tramites/interno/pagado",
                new Pagado(folio, liquidacionId), properties.getInternalToken(), tenant);
    }

    public record Pagado(String folio, UUID liquidacionId) {
    }
}
