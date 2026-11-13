package mx.gob.gld.pagos.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.common.web.SolicitudActual;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClientesPagoTest {

    private RestClient http;
    private MockRestServiceServer server;
    private GldProperties properties;
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void prepara() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        http = builder.build();
        properties = new GldProperties();
        properties.setInternalToken("gld-dev-internal");
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
    void catastroTramitesYDocumentosHablanPorElClienteInterno() {
        CatastroCliente catastro = new CatastroCliente(http, json, properties);
        server.expect(requestTo("http://localhost:8082/api/v1/predios/05-021-0147-003"))
                .andExpect(method(GET)).andExpect(header("Authorization", "Bearer t"))
                .andRespond(withSuccess("""
                        {"claveCatastral":"05-021-0147-003","propietario":"María","direccion":"Calle","superficieM2":120,
                         "periodos":[{"periodo":"2025-6","importeBase":1500,"recargoMora":180,"importePeriodo":1680,
                         "fechaVencimiento":"2025-12-31","vencido":true,"pagado":false}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8082/api/v1/agua/contratos/CTA-1001"))
                .andRespond(withSuccess("""
                        {"numeroContrato":"CTA-1001","titular":"Ana","direccion":"Norte","periodo":"2026-2",
                         "lecturaAnterior":100,"lecturaActual":128,"consumoM3":28,"tarifaM3":18.50,"cuotaFija":45,
                         "importe":563.00,"pagado":false}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8082/api/v1/predios/interno/pagado")).andExpect(method(POST)).andRespond(withSuccess());
        server.expect(requestTo("http://localhost:8082/api/v1/agua/interno/pagado")).andRespond(withSuccess());
        UUID documento = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1");
        server.expect(requestTo("http://localhost:8084/api/v1/tramites/interno/pagado")).andRespond(withSuccess());
        server.expect(requestTo("http://localhost:8085/api/v1/documentos/interno/emitir"))
                .andRespond(withSuccess("{\"id\":\"" + documento + "\",\"folio\":\"PAG-2026-000001\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8085/api/v1/documentos/interno/emitir"))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8085/api/v1/documentos/interno/emitir"))
                .andRespond(withSuccess("{\"folio\":\"PAG\"}", MediaType.APPLICATION_JSON));

        CatastroCliente.PredioRemoto predio = catastro.predio("05-021-0147-003");
        assertEquals("María", predio.propietario());
        assertEquals("2025-6", predio.periodos().get(0).periodo());
        assertEquals(true, predio.periodos().get(0).vencido());
        assertEquals(new BigDecimal("563.00"), catastro.contrato("CTA-1001").importe());
        catastro.marcarPredial("05021", "05-021-0147-003", List.of("2025-6"));
        catastro.marcarAgua("05021", "CTA-1001");
        assertEquals("05-021-0147-003", new CatastroCliente.MarcarPredial("05-021-0147-003", List.of("2025-6")).claveCatastral());
        assertEquals("CTA-1001", new CatastroCliente.MarcarAgua("CTA-1001").numeroContrato());

        UUID liquidacion = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1");
        TramitesCliente tramites = new TramitesCliente(http, json, properties);
        tramites.pagado("05021", "TRM-2026-000001", liquidacion);
        assertEquals(liquidacion, new TramitesCliente.Pagado("TRM-2026-000001", liquidacion).liquidacionId());

        DocumentosCliente documentos = new DocumentosCliente(http, json, properties);
        DocumentosCliente.Emitir emitir = new DocumentosCliente.Emitir("PAG-2026-000001", "RECIBO_PAGO", UUID.randomUUID(), null,
                "Ana", "Predial", new BigDecimal("10"));
        assertEquals(documento, documentos.emitir("05021", emitir));
        assertEquals("Ana", emitir.titular());
        assertEquals("SERVICIO_NO_DISPONIBLE", assertThrows(ApiException.class, () -> documentos.emitir("05021", emitir)).getCodigo());
        assertEquals("SERVICIO_NO_DISPONIBLE", assertThrows(ApiException.class, () -> documentos.emitir("05021", emitir)).getCodigo());
        assertEquals("Bearer t", SolicitudActual.autorizacion());
        server.verify();
    }
}
