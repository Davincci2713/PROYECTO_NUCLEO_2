package mx.gob.gld.common;

import mx.gob.gld.common.security.GldProperties;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GldCommonAutoConfigurationTest {

    @Test
    void publicaJwtRelojSecuenciasYCliente() {
        GldProperties properties = new GldProperties();
        properties.getJwt().setSecret("gld-dev-secret-cambia-en-produccion-32b");
        GldCommonAutoConfiguration auto = new GldCommonAutoConfiguration();
        assertNotNull(auto.gldJwtService(properties));
        assertNotNull(auto.secuencias(mock(JdbcTemplate.class)));
        assertNotNull(auto.gldClock());
        assertNotNull(auto.gldRestClient(RestClient.builder()));
    }
}
