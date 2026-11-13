package mx.gob.gld.tramites.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PagosTramiteClienteTest {

    private MockRestServiceServer server;
    private PagosTramiteCliente cliente;

    @BeforeEach
    void prepara() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        GldProperties properties = new GldProperties();
        properties.setInternalToken("gld-dev-internal");
        cliente = new PagosTramiteCliente(builder.build(), new ObjectMapper(), properties);
        TenantContext.set("05021");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer t");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void cotizaExigePredioYLiquidaLaLicencia() {
        server.expect(requestTo("http://localhost:8083/api/v1/pagos/cotizaciones/licencia"))
                .andExpect(header("Authorization", "Bearer t"))
                .andRespond(withSuccess("""
                        {"giro":"ABARROTES","tarifaBase":800.00,"factorM2":12.00,"superficieM2":40.00,"total":1280.00}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8082/api/v1/predios/05-021-0147-003"))
                .andRespond(withSuccess("{\"propietario\":\"María\"}", MediaType.APPLICATION_JSON));
        UUID id = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1");
        UUID tramite = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1");
        server.expect(requestTo("http://localhost:8083/api/v1/pagos/interno/liquidaciones/licencia"))
                .andExpect(header("X-Internal-Token", "gld-dev-internal"))
                .andRespond(withSuccess("{\"id\":\"" + id + "\"}", MediaType.APPLICATION_JSON));
        PagosTramiteCliente.Cotizacion cotizacion = cliente.cotizar("ABARROTES", new BigDecimal("40"));
        assertEquals(new BigDecimal("1280.00"), cotizacion.total());
        assertEquals("ABARROTES", new PagosTramiteCliente.CotizacionPedido("ABARROTES", new BigDecimal("40")).giro());
        cliente.exigirPredio("05-021-0147-003");
        assertEquals(id, cliente.liquidarLicencia("05021", id, "Ana", "TRM-2026-000001", tramite, "Café", new BigDecimal("1280.00")));
        assertEquals("Café", new PagosTramiteCliente.LicenciaPedido(id, "Ana", "TRM-2026-000001", tramite, "Café", new BigDecimal("1280")).nombreComercial());
        server.verify();
    }
}
