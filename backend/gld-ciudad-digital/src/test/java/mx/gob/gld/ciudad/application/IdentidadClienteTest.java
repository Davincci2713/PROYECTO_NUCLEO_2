package mx.gob.gld.ciudad.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class IdentidadClienteTest {

    @Test
    void totpNuloFalsoOVerdadero() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GldProperties properties = new GldProperties();
        properties.setInternalToken("gld-dev-internal");
        IdentidadCliente cliente = new IdentidadCliente(builder.build(), new ObjectMapper(), properties);
        UUID id = UUID.fromString("11111111-1111-4111-8111-111111111111");
        String url = "http://localhost:8081/api/v1/auth/interno/usuarios/" + id + "/totp";
        server.expect(requestTo(url)).andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        server.expect(requestTo(url)).andRespond(withSuccess("{\"totpActivo\":false}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(url)).andRespond(withSuccess("{\"totpActivo\":true}", MediaType.APPLICATION_JSON));
        assertFalse(cliente.totpActivo(id, "05021"));
        assertFalse(cliente.totpActivo(id, "05021"));
        assertTrue(cliente.totpActivo(id, "05021"));
        assertTrue(new IdentidadCliente.Totp(true).totpActivo());
        server.verify();
    }
}
