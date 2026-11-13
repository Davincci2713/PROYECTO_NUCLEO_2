package mx.gob.gld.identity.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.web.ClienteInterno;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CiudadPerfilCliente {

    private final RestClient http;
    private final ObjectMapper json;
    private final GldProperties properties;

    public CiudadPerfilCliente(RestClient http, ObjectMapper json, GldProperties properties) {
        this.http = http;
        this.json = json;
        this.properties = properties;
    }

    public void crear(UUID usuarioId, String nombre, String tenant) {
        ClienteInterno.postInternoVacio(
                http,
                json,
                properties.getServices().getCiudad() + "/api/v1/ciudad/interno/perfiles",
                new Alta(usuarioId, nombre),
                properties.getInternalToken(),
                tenant);
    }

    public record Alta(UUID usuarioId, String nombre) {
    }
}
