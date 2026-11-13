package mx.gob.gld.tramites.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.tramites.infrastructure.AdjuntoRepository;
import mx.gob.gld.tramites.infrastructure.HistorialEntity;
import mx.gob.gld.tramites.infrastructure.HistorialRepository;
import mx.gob.gld.tramites.infrastructure.TramiteEntity;
import mx.gob.gld.tramites.infrastructure.TramiteRepository;
import mx.gob.gld.tramites.web.TramitesDtos.DictamenRequest;
import mx.gob.gld.tramites.web.TramitesDtos.DocumentoAdjunto;
import mx.gob.gld.tramites.web.TramitesDtos.LicenciaConstruccionRequest;
import mx.gob.gld.tramites.web.TramitesDtos.LicenciaFuncionamientoRequest;
import mx.gob.gld.tramites.web.TramitesDtos.PagadoRequest;
import mx.gob.gld.tramites.web.TramitesDtos.SubsaniarRequest;
import mx.gob.gld.tramites.web.TramitesDtos.TramiteResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TramitesServiceTest {

    private static final UUID CIUDADANO = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID OTRO = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final Instant AHORA = Instant.parse("2026-11-08T15:00:00Z");

    @Mock TramiteRepository tramites;
    @Mock HistorialRepository historial;
    @Mock AdjuntoRepository adjuntos;
    @Mock PagosTramiteCliente pagos;
    @Mock Secuencias secuencias;
    @Mock BitacoraService bitacora;

    private final ObjectMapper json = new ObjectMapper();
    private TramitesService servicio;

    @BeforeEach
    void prepara() {
        servicio = new TramitesService(tramites, historial, adjuntos, pagos, secuencias, bitacora, json,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        TenantContext.set("05021");
        como("CIUDADANO", "Ana Ruiz");
        when(secuencias.folio(eq("TRM"), any())).thenReturn("TRM-2026-000001");
        when(pagos.cotizar(any(), any())).thenReturn(new PagosTramiteCliente.Cotizacion(
                "ABARROTES", new BigDecimal("800.00"), new BigDecimal("12.00"), new BigDecimal("40.00"), new BigDecimal("1280.00")));
        when(pagos.liquidarLicencia(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1"));
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void licenciaDeFuncionamientoQuedaEnRevisionConElTotalCotizado() {
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaFuncionamiento(null));
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaFuncionamiento(new LicenciaFuncionamientoRequest(" ", "ABARROTES", new BigDecimal("40"))));
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaFuncionamiento(new LicenciaFuncionamientoRequest("x".repeat(161), "ABARROTES", new BigDecimal("40"))));
        assertCodigo("GIRO_DESCONOCIDO", () -> servicio.licenciaFuncionamiento(new LicenciaFuncionamientoRequest("Café", " ", new BigDecimal("40"))));
        TramiteResponse respuesta = servicio.licenciaFuncionamiento(
                new LicenciaFuncionamientoRequest(" Café Norte ", "ABARROTES", new BigDecimal("40")));
        assertEquals("TRM-2026-000001", respuesta.folio());
        assertEquals("EN_REVISION", respuesta.estado());
        assertEquals("LICENCIA_FUNCIONAMIENTO", respuesta.tipo());
        assertEquals(0, new BigDecimal("1280.00").compareTo(new BigDecimal(respuesta.payload().get("total").toString())));
        assertEquals(CIUDADANO, respuesta.ciudadanoId());
    }

    @Test
    void obraSeRadicaConAdjuntoYConservaElFolioAlSubsanar() {
        assertCodigo("CLAVE_CATASTRAL_INVALIDA", () -> servicio.licenciaConstruccion(
                obra("OBRA_MENOR", "mala", "Ampliación", List.of(doc("plano.pdf", "application/pdf", "plano")))));
        assertCodigo("ADJUNTO_OBLIGATORIO", () -> servicio.licenciaConstruccion(
                obra("OBRA_MENOR", "05-021-0147-003", "Ampliación", null)));
        assertCodigo("ADJUNTO_OBLIGATORIO", () -> servicio.licenciaConstruccion(
                obra("OBRA_MENOR", "05-021-0147-003", "Ampliación", List.of())));
        assertCodigo("ADJUNTO_OBLIGATORIO", () -> servicio.licenciaConstruccion(
                obra("OBRA_MENOR", "05-021-0147-003", "Ampliación", List.of(new DocumentoAdjunto(" ", "pdf", "YQ==")))));
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaConstruccion(
                obra("OBRA_MENOR", "05-021-0147-003", "Ampliación",
                        List.of(new DocumentoAdjunto("plano.pdf", "pdf", "%%%")))));
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaConstruccion(null));
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaConstruccion(obra(" ", "05-021-0147-003", "Ampliación", List.of(doc("a.pdf", null, "plano")))));
        String tipoLargo = "t".repeat(90);
        assertCodigo("DATO_INVALIDO", () -> servicio.licenciaConstruccion(
                obra(tipoLargo, "05-021-0147-003", "Ampliación", List.of(doc("a.pdf", "pdf", "plano")))));

        TramiteResponse radicado = servicio.licenciaConstruccion(obra(
                " OBRA_MENOR ",
                "05-021-0147-003",
                " Ampliación de losa ",
                List.of(doc("  " + "n".repeat(200) + ".pdf", " " + "c".repeat(90), "plano"))));
        assertEquals("RADICADO", radicado.estado());
        assertEquals("TRM-2026-000001", radicado.folio());
        verify(pagos).exigirPredio("05-021-0147-003");
        verify(adjuntos).saveAll(any());

        TramiteEntity guardado = ultimoGuardado();
        guardado.estado = "POR_SUBSANAR";
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000001")).thenReturn(Optional.of(guardado));
        TramiteResponse subsanado = servicio.subsanar(" TRM-2026-000001 ",
                new SubsaniarRequest(List.of(doc("fachada.pdf", "", "plano"))));
        assertEquals("TRM-2026-000001", subsanado.folio());
        assertEquals("RADICADO", subsanado.estado());
    }

    @Test
    void subsanarFuncionamientoVuelveARevisionYRechazaAjenoOEstado() {
        TramiteEntity funcionamiento = tramite("LICENCIA_FUNCIONAMIENTO", "POR_SUBSANAR");
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000001")).thenReturn(Optional.of(funcionamiento));
        TramiteResponse respuesta = servicio.subsanar("TRM-2026-000001",
                new SubsaniarRequest(List.of(doc("uso.pdf", "application/pdf", "plano"))));
        assertEquals("EN_REVISION", respuesta.estado());
        assertEquals("TRM-2026-000001", respuesta.folio());

        como("CIUDADANO", "Otra");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(OTRO, "otro@gld.demo", "Otra", "CIUDADANO", "05021", "ok"), null));
        assertCodigo("NO_AUTORIZADO", () -> servicio.subsanar("TRM-2026-000001",
                new SubsaniarRequest(List.of(doc("uso.pdf", "pdf", "plano")))));

        como("CIUDADANO", "Ana Ruiz");
        funcionamiento.estado = "RADICADO";
        assertCodigo("ESTADO_NO_PERMITE", () -> servicio.subsanar("TRM-2026-000001",
                new SubsaniarRequest(List.of(doc("uso.pdf", "pdf", "plano")))));
        funcionamiento.estado = "POR_SUBSANAR";
        assertCodigo("ADJUNTO_OBLIGATORIO", () -> servicio.subsanar("TRM-2026-000001", null));
        assertCodigo("TRAMITE_NO_ENCONTRADO", () -> servicio.subsanar(" ", new SubsaniarRequest(List.of())));
        assertCodigo("TRAMITE_NO_ENCONTRADO", () -> servicio.porFolio("no-existe"));
        assertCodigo("TRAMITE_NO_ENCONTRADO", () -> servicio.porFolio(null));
    }

    @Test
    void elServidorObservaYApruebaConservandoOCobrando() {
        assertCodigo("NO_AUTORIZADO", () -> servicio.bandeja());
        assertCodigo("NO_AUTORIZADO", () -> servicio.dictaminar("TRM-2026-000001", new DictamenRequest("APROBAR", null)));
        como("SERVIDOR_PUBLICO", "Laura Gómez");
        assertTrue(servicio.bandeja().isEmpty());
        TramiteEntity enBandeja = tramite("LICENCIA_CONSTRUCCION", "RADICADO");
        when(tramites.findByTenantIdAndEstadoInOrderByCreadoEnAsc(eq("05021"), any())).thenReturn(List.of(enBandeja));
        assertEquals("RADICADO", servicio.bandeja().get(0).estado());

        assertCodigo("DATO_INVALIDO", () -> servicio.dictaminar("TRM-2026-000001", null));
        assertCodigo("DATO_INVALIDO", () -> servicio.dictaminar("TRM-2026-000001", new DictamenRequest(" ", null)));
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000001")).thenReturn(Optional.empty());
        assertCodigo("TRAMITE_NO_ENCONTRADO", () -> servicio.dictaminar("TRM-2026-000001", new DictamenRequest("APROBAR", null)));

        TramiteEntity obra = tramite("LICENCIA_CONSTRUCCION", "PAGADO");
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000001")).thenReturn(Optional.of(obra));
        assertCodigo("ESTADO_NO_PERMITE", () -> servicio.dictaminar("TRM-2026-000001", new DictamenRequest("APROBAR", null)));

        obra.estado = "EN_REVISION";
        assertCodigo("OBSERVACION_OBLIGATORIA", () -> servicio.dictaminar("TRM-2026-000001", new DictamenRequest("observar", " ")));
        TramiteResponse observado = servicio.dictaminar("TRM-2026-000001",
                new DictamenRequest(" observar ", "x".repeat(520)));
        assertEquals("POR_SUBSANAR", observado.estado());
        assertEquals("TRM-2026-000001", observado.folio());

        obra.estado = "POR_SUBSANAR";
        assertCodigo("DATO_INVALIDO", () -> servicio.dictaminar("TRM-2026-000001", new DictamenRequest("RECHAZAR", null)));
        TramiteResponse aprobada = servicio.dictaminar("TRM-2026-000001", new DictamenRequest("aprobar", null));
        assertEquals("APROBADO", aprobada.estado());

        TramiteEntity funcionamiento = tramite("LICENCIA_FUNCIONAMIENTO", "EN_REVISION");
        funcionamiento.folio = "TRM-2026-000009";
        funcionamiento.payload = "{\"total\":1280.00,\"nombreComercial\":\"Café Norte\"}";
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000009")).thenReturn(Optional.of(funcionamiento));
        HistorialEntity primero = new HistorialEntity();
        primero.actor = "Ana Ruiz";
        when(historial.findByTramiteIdOrderByCreadoEnAsc(funcionamiento.id)).thenReturn(List.of(primero));
        TramiteResponse pendiente = servicio.dictaminar("TRM-2026-000009", new DictamenRequest("APROBAR", null));
        assertEquals("APROBADO_PENDIENTE_PAGO", pendiente.estado());
        assertEquals(UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1"), pendiente.liquidacionId());

        when(historial.findByTramiteIdOrderByCreadoEnAsc(funcionamiento.id)).thenReturn(List.of());
        funcionamiento.estado = "RADICADO";
        assertEquals("APROBADO_PENDIENTE_PAGO",
                servicio.dictaminar("TRM-2026-000009", new DictamenRequest("APROBAR", null)).estado());
    }

    @Test
    void elPagoAcreditadoPasaAPagadoYUnRepetidoNoCambia() {
        assertCodigo("TRAMITE_NO_ENCONTRADO", () -> servicio.marcarPagado(null));
        assertCodigo("TRAMITE_NO_ENCONTRADO", () -> servicio.marcarPagado(new PagadoRequest(" ", null)));
        TramiteEntity tramite = tramite("LICENCIA_FUNCIONAMIENTO", "PAGADO");
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000001")).thenReturn(Optional.of(tramite));
        servicio.marcarPagado(new PagadoRequest("TRM-2026-000001", null));
        assertEquals("PAGADO", tramite.estado);

        tramite.estado = "RADICADO";
        assertCodigo("ESTADO_NO_PERMITE", () -> servicio.marcarPagado(new PagadoRequest("TRM-2026-000001", UUID.randomUUID())));
        tramite.estado = "APROBADO_PENDIENTE_PAGO";
        UUID liquidacion = UUID.randomUUID();
        servicio.marcarPagado(new PagadoRequest(" TRM-2026-000001 ", liquidacion));
        assertEquals("PAGADO", tramite.estado);
        assertEquals(liquidacion, tramite.liquidacionId);
    }

    @Test
    void misTramitesYElJsonRoto() throws Exception {
        TramiteEntity tramite = tramite("LICENCIA_FUNCIONAMIENTO", "EN_REVISION");
        when(tramites.findByTenantIdAndCiudadanoIdOrderByCreadoEnDesc("05021", CIUDADANO)).thenReturn(List.of(tramite));
        assertEquals(1, servicio.mios().size());
        when(tramites.findByTenantIdAndFolio("05021", "TRM-2026-000001")).thenReturn(Optional.of(tramite));
        assertEquals("EN_REVISION", servicio.porFolio("TRM-2026-000001").estado());

        ObjectMapper roto = org.mockito.Mockito.mock(ObjectMapper.class);
        when(roto.writeValueAsString(any())).thenThrow(new JsonProcessingException("no") { });
        TramitesService conRoto = new TramitesService(tramites, historial, adjuntos, pagos, secuencias, bitacora, roto,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        assertThrows(IllegalStateException.class, () -> conRoto.licenciaFuncionamiento(
                new LicenciaFuncionamientoRequest("Café", "ABARROTES", new BigDecimal("40"))));

        when(roto.readTree(anyString())).thenThrow(new JsonProcessingException("no") { });
        when(roto.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenThrow(new JsonProcessingException("no") { });
        tramite.payload = "{";
        assertThrows(IllegalStateException.class, () -> conRoto.porFolio("TRM-2026-000001"));
        tramite.estado = "EN_REVISION";
        tramite.tipo = "LICENCIA_FUNCIONAMIENTO";
        assertThrows(IllegalStateException.class, () -> {
            como("SERVIDOR_PUBLICO", "Laura");
            conRoto.dictaminar("TRM-2026-000001", new DictamenRequest("APROBAR", null));
        });
    }

    private TramiteEntity ultimoGuardado() {
        org.mockito.ArgumentCaptor<TramiteEntity> captor = org.mockito.ArgumentCaptor.forClass(TramiteEntity.class);
        verify(tramites, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private void como(String rol, String nombre) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(CIUDADANO, "ana@gld.demo", nombre, rol, "05021", "ok"), null));
    }

    private static void assertCodigo(String codigo, Runnable accion) {
        assertEquals(codigo, assertThrows(ApiException.class, accion::run).getCodigo());
    }

    private TramiteEntity tramite(String tipo, String estado) {
        TramiteEntity tramite = new TramiteEntity();
        tramite.id = UUID.randomUUID();
        tramite.tenantId = "05021";
        tramite.folio = "TRM-2026-000001";
        tramite.tipo = tipo;
        tramite.estado = estado;
        tramite.ciudadanoId = CIUDADANO;
        tramite.titulo = "Titulo";
        try {
            tramite.payload = json.writeValueAsString(java.util.Map.of("total", "1280.00", "nombreComercial", "Café"));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        tramite.creadoEn = AHORA;
        tramite.actualizadoEn = AHORA;
        return tramite;
    }

    private static LicenciaConstruccionRequest obra(String tipo, String clave, String descripcion, List<DocumentoAdjunto> documentos) {
        return new LicenciaConstruccionRequest(tipo, clave, descripcion, documentos);
    }

    private static DocumentoAdjunto doc(String nombre, String tipo, String texto) {
        return new DocumentoAdjunto(nombre, tipo, Base64.getEncoder().encodeToString(texto.getBytes()));
    }
}
