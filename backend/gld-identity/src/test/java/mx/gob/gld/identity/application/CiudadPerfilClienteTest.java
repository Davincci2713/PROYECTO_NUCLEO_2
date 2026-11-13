package mx.gob.gld.identity.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CiudadPerfilClienteTest {

    @Test
    void crearPerfilUsaElTokenInterno() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GldProperties properties = new GldProperties();
        properties.setInternalToken("gld-dev-internal");
        CiudadPerfilCliente cliente = new CiudadPerfilCliente(builder.build(), new ObjectMapper(), properties);
        UUID id = UUID.fromString("11111111-1111-4111-8111-111111111111");
        server.expect(requestTo("http://localhost:8086/api/v1/ciudad/interno/perfiles"))
                .andExpect(method(POST))
                .andExpect(header("X-Internal-Token", "gld-dev-internal"))
                .andExpect(header("X-Tenant-ID", "05021"))
                .andRespond(withSuccess());
        cliente.crear(id, "Ana Ruiz", "05021");
        assert id.equals(new CiudadPerfilCliente.Alta(id, "Ana Ruiz").usuarioId());
        server.verify();
    }
}
