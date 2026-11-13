package mx.gob.gld.ciudad.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.web.ClienteInterno;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class IdentidadCliente {

    private final RestClient http;
    private final ObjectMapper json;
    private final GldProperties properties;

    public IdentidadCliente(RestClient http, ObjectMapper json, GldProperties properties) {
        this.http = http;
        this.json = json;
        this.properties = properties;
    }

    public boolean totpActivo(UUID usuarioId, String tenant) {
        Totp totp = ClienteInterno.getInterno(http, json,
                properties.getServices().getIdentity() + "/api/v1/auth/interno/usuarios/" + usuarioId + "/totp",
                Totp.class, properties.getInternalToken(), tenant);
        return totp != null && totp.totpActivo();
    }

    public record Totp(boolean totpActivo) {
    }
}
