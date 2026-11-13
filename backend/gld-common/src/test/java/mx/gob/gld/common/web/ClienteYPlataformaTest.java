package mx.gob.gld.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.bitacora.BitacoraEntity;
import mx.gob.gld.common.bitacora.BitacoraRepository;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.money.Dinero;
import mx.gob.gld.common.money.Folios;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.CONFLICT;

class ClienteYPlataformaTest {

    private final ObjectMapper json = new ObjectMapper();

    @AfterEach
    void limpia() {
        TenantContext.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void elClienteInternoLeeYEscribeYTraduceFallos() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient http = builder.build();
        server.expect(requestTo("http://servicio/recurso"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer a"))
                .andExpect(header("X-Tenant-ID", "05021"))
                .andRespond(withSuccess("{\"valor\":\"ok\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio/interno"))
                .andExpect(header("X-Internal-Token", "gld-dev-internal"))
                .andRespond(withSuccess("{\"valor\":\"interno\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio/alta"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"valor\":\"creado\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio/interno/alta"))
                .andRespond(withSuccess("{\"valor\":\"interno-post\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://servicio/vacio")).andRespond(withSuccess());
        server.expect(requestTo("http://servicio/conflicto"))
                .andRespond(withStatus(CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"titulo\":\"Voto\",\"status\":409,\"detalle\":\"ya\",\"campo\":null,\"ejemplo\":null,\"codigo\":\"YA_VOTO\"}"));

        assertEquals("ok", ClienteInterno.get(http, json, "http://servicio/recurso", Dato.class, "Bearer a", "05021").valor());
        assertEquals("interno", ClienteInterno.getInterno(
                http, json, "http://servicio/interno", Dato.class, "gld-dev-internal", "05021").valor());
        assertEquals("creado", ClienteInterno.post(
                http, json, "http://servicio/alta", new Dato("x"), Dato.class, "Bearer a", "05021").valor());
        assertEquals("interno-post", ClienteInterno.postInterno(
                http, json, "http://servicio/interno/alta", new Dato("x"), Dato.class, "tok", "05021").valor());
        ClienteInterno.postInternoVacio(http, json, "http://servicio/vacio", new Dato("x"), "tok", "05021");
        ApiException conflicto = assertThrows(ApiException.class, () -> ClienteInterno.get(
                http, json, "http://servicio/conflicto", Dato.class, "Bearer a", "05021"));
        assertEquals("YA_VOTO", conflicto.getCodigo());
        server.verify();
    }

    @Test
    void unCorteDeRedSeVuelveServicioNoDisponible() {
        RestClient http = RestClient.builder().requestFactory((uri, method) -> {
            throw new RestClientException("caido");
        }).build();
        assertEquals("SERVICIO_NO_DISPONIBLE", falla(() -> ClienteInterno.get(
                http, json, "http://servicio/x", Dato.class, "a", "05021")));
        assertEquals("SERVICIO_NO_DISPONIBLE", falla(() -> ClienteInterno.getInterno(
                http, json, "http://servicio/x", Dato.class, "a", "05021")));
        assertEquals("SERVICIO_NO_DISPONIBLE", falla(() -> ClienteInterno.post(
                http, json, "http://servicio/x", new Dato("a"), Dato.class, "a", "05021")));
        assertEquals("SERVICIO_NO_DISPONIBLE", falla(() -> ClienteInterno.postInterno(
                http, json, "http://servicio/x", new Dato("a"), Dato.class, "a", "05021")));
        assertEquals("SERVICIO_NO_DISPONIBLE", falla(() -> ClienteInterno.postInternoVacio(
                http, json, "http://servicio/x", new Dato("a"), "a", "05021")));
    }

    @Test
    void laSolicitudActualLeeEncabezadosYElTenant() {
        assertThrows(IllegalStateException.class, SolicitudActual::autorizacion);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertEquals("", SolicitudActual.autorizacion());
        assertEquals("", SolicitudActual.tenant());
        request.addHeader("Authorization", "Bearer t");
        request.addHeader("X-Tenant-ID", "05021");
        assertEquals("Bearer t", SolicitudActual.autorizacion());
        assertEquals("05021", SolicitudActual.tenant());
        TenantContext.set("05022");
        assertEquals("05022", SolicitudActual.tenant());
        TenantContext.set("  ");
        assertEquals("05021", SolicitudActual.tenant());
    }

    @Test
    void dineroFoliosYSecuencias() {
        assertEquals(new BigDecimal("10.26"), Dinero.dos(new BigDecimal("10.255")));
        assertEquals(new BigDecimal("3.50"), Dinero.dos("3.5"));
        assertEquals(new BigDecimal("0.00"), Dinero.cero());
        assertEquals("INC-2026-000042", Folios.formato("INC", 2026, 42));

        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Secuencias secuencias = new Secuencias(jdbc);
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq("pagos.folio_seq"))).thenReturn(7L);
        int anio = Year.now(ZoneOffset.UTC).getValue();
        assertEquals("PAG-" + anio + "-000007", secuencias.folio("PAG", "pagos.folio_seq"));
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq("pagos.folio_seq"))).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> secuencias.next("pagos.folio_seq"));
        assertThrows(IllegalArgumentException.class, () -> secuencias.next("pagos.otra"));
    }

    @Test
    void laBitacoraRecortaYLaClaveCatastralValidaElFormato() {
        BitacoraRepository repository = mock(BitacoraRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        BitacoraService bitacora = new BitacoraService(repository);
        String largo = "x".repeat(600);
        bitacora.registrar("05021", UUID.randomUUID(), "PAGO", "PAGO", largo, null);
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(fila -> {
            BitacoraEntity entidad = (BitacoraEntity) fila;
            return entidad.referencia.length() == 80 && entidad.detalle == null && entidad.tenantId.equals("05021");
        }));
        bitacora.registrar("05021", null, "ALTA", "PERFIL", "corto", "detalle");
        assertTrue(ClaveCatastral.valida("05-021-0147-003"));
        assertTrue(!ClaveCatastral.valida("05021"));
        assertTrue(!ClaveCatastral.valida(null));
    }

    @Test
    void lasPropiedadesYLaAutoconfiguracionExponenLosBeans() {
        GldProperties properties = new GldProperties();
        properties.setInternalToken("gld-dev-internal");
        properties.setSelloSecret("sello");
        properties.setVerificacionBase("http://localhost:3000/verificar");
        properties.getServices().setIdentity("http://identity");
        properties.getServices().setCatastro("http://catastro");
        properties.getServices().setPagos("http://pagos");
        properties.getServices().setTramites("http://tramites");
        properties.getServices().setDocumentos("http://documentos");
        properties.getServices().setCiudad("http://ciudad");
        properties.getServices().setIncidencias("http://incidencias");
        properties.getJwt().setSecret("gld-dev-secret-cambia-en-produccion-32b");
        assertEquals("gld-dev-internal", properties.getInternalToken());
        assertEquals("sello", properties.getSelloSecret());
        assertEquals("http://localhost:3000/verificar", properties.getVerificacionBase());
        assertEquals("http://identity", properties.getServices().getIdentity());
        assertEquals("http://catastro", properties.getServices().getCatastro());
        assertEquals("http://pagos", properties.getServices().getPagos());
        assertEquals("http://tramites", properties.getServices().getTramites());
        assertEquals("http://documentos", properties.getServices().getDocumentos());
        assertEquals("http://ciudad", properties.getServices().getCiudad());
        assertEquals("http://incidencias", properties.getServices().getIncidencias());
        assertEquals(3600, properties.getJwt().getSessionSeconds());
    }

    private static String falla(Runnable accion) {
        return assertThrows(ApiException.class, accion::run).getCodigo();
    }

    public record Dato(String valor) {
    }
}
