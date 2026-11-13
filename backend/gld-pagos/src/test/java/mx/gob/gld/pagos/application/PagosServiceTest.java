package mx.gob.gld.pagos.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.persistence.EntityManager;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.pagos.application.CatastroCliente.ContratoRemoto;
import mx.gob.gld.pagos.application.CatastroCliente.PeriodoRemoto;
import mx.gob.gld.pagos.application.CatastroCliente.PredioRemoto;
import mx.gob.gld.pagos.infrastructure.LiquidacionEntity;
import mx.gob.gld.pagos.infrastructure.LiquidacionRepository;
import mx.gob.gld.pagos.infrastructure.MultaEntity;
import mx.gob.gld.pagos.infrastructure.MultaRepository;
import mx.gob.gld.pagos.infrastructure.TarifaLicenciaEntity;
import mx.gob.gld.pagos.infrastructure.TarifaLicenciaRepository;
import mx.gob.gld.pagos.infrastructure.TransaccionRepository;
import mx.gob.gld.pagos.web.PagosDtos.AguaRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroResponse;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaCotizacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaInternaRequest;
import mx.gob.gld.pagos.web.PagosDtos.Linea;
import mx.gob.gld.pagos.web.PagosDtos.LiquidacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.MultaLiquidacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.PredialRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PagosServiceTest {

    private static final UUID CIUDADANO = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final Instant AHORA = Instant.parse("2026-11-08T15:00:00Z");

    @Mock TarifaLicenciaRepository tarifas;
    @Mock MultaRepository multas;
    @Mock LiquidacionRepository liquidaciones;
    @Mock TransaccionRepository transacciones;
    @Mock CatastroCliente catastro;
    @Mock TramitesCliente tramites;
    @Mock DocumentosCliente documentos;
    @Mock Secuencias secuencias;
    @Mock BitacoraService bitacora;
    @Mock EntityManager entityManager;
    @Mock PlatformTransactionManager transactionManager;

    private final ObjectMapper json = new ObjectMapper();
    private final Clock clock = Clock.fixed(AHORA, ZoneOffset.UTC);
    private PagosService servicio;

    @BeforeEach
    void prepara() {
        servicio = servicio(json);
        TenantContext.set("05021");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(CIUDADANO, "ciudadano@gld.demo", "María López", "CIUDADANO", "05021", "ok"), null));
        when(liquidaciones.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(liquidaciones.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(liquidaciones.findByTenantIdAndTipoAndReferenciaAndEstado(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(secuencias.folio(eq("PAG"), any())).thenReturn("PAG-2026-000001");
        when(documentos.emitir(any(), any())).thenReturn(UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1"));
        TransactionStatus status = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(status);
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void predialDosPeriodosGuardaElDescuentoDelDosPorCiento() {
        when(catastro.predio("05-021-0147-003")).thenReturn(predio());
        LiquidacionResponse respuesta = servicio.liquidarPredial(
                new PredialRequest("05-021-0147-003", List.of("2025-6", " 2026-1", "2025-6", " ")));
        assertEquals(new BigDecimal("3180.00"), respuesta.subtotal());
        assertEquals(new BigDecimal("180.00"), respuesta.recargos());
        assertEquals(new BigDecimal("63.60"), respuesta.descuento());
        assertEquals(new BigDecimal("3116.40"), respuesta.total());
        assertEquals(2, respuesta.descuentoPorcentaje());
        assertEquals("ACTIVA", respuesta.estado());
        assertEquals(AHORA.plusSeconds(72 * 3600), respuesta.venceEn());
        assertEquals("2025-6", respuesta.desglose().get(0).periodo());
        assertEquals(0, new BigDecimal("1680.00").compareTo(respuesta.desglose().get(0).importe()));
        assertTrue(respuesta.concepto().contains("2025-6"));
    }

    @Test
    void predialRechazaClavePeriodoPagadoCuerpoYReutilizaLaActiva() {
        assertCodigo("DATO_INVALIDO", () -> servicio.liquidarPredial(null));
        assertCodigo("CLAVE_CATASTRAL_INVALIDA", () -> servicio.liquidarPredial(new PredialRequest("mala", List.of("2025-6"))));
        assertCodigo("PERIODO_NO_DISPONIBLE", () -> servicio.liquidarPredial(new PredialRequest("05-021-0147-003", null)));
        assertCodigo("PERIODO_NO_DISPONIBLE", () -> servicio.liquidarPredial(new PredialRequest("05-021-0147-003", java.util.Arrays.asList(" ", null))));
        when(catastro.predio("05-021-0147-003")).thenReturn(predio());
        assertCodigo("PERIODO_NO_DISPONIBLE", () -> servicio.liquidarPredial(new PredialRequest("05-021-0147-003", List.of("2099-1"))));
        PeriodoRemoto pagado = new PeriodoRemoto("2024-1", new BigDecimal("1"), new BigDecimal("0"),
                new BigDecimal("1"), LocalDate.of(2024, 6, 30), true, true);
        when(catastro.predio("05-021-0147-003")).thenReturn(new PredioRemoto(
                "05-021-0147-003", "María", "Calle", new BigDecimal("80"), List.of(pagado)));
        assertCodigo("PERIODO_NO_DISPONIBLE", () -> servicio.liquidarPredial(new PredialRequest("05-021-0147-003", List.of("2024-1"))));

        LiquidacionEntity previa = liquidacion("PREDIAL", "05-021-0147-003:2025-6", "ACTIVA", AHORA.plusSeconds(3600));
        when(liquidaciones.findByTenantIdAndTipoAndReferenciaAndEstado(any(), eq("PREDIAL"), any(), eq("ACTIVA")))
                .thenReturn(Optional.of(previa));
        when(catastro.predio("05-021-0147-003")).thenReturn(predio());
        assertEquals(previa.id, servicio.liquidarPredial(new PredialRequest("05-021-0147-003", List.of("2025-6"))).id());

        previa.venceEn = AHORA.minusSeconds(1);
        LiquidacionResponse nueva = servicio.liquidarPredial(new PredialRequest("05-021-0147-003", List.of("2025-6")));
        assertEquals("EXPIRADA", previa.estado);
        assertEquals("ACTIVA", nueva.estado());
    }

    @Test
    void aguaCobraElImporteYRechazaSaldoOContratoVacio() {
        assertCodigo("CONTRATO_NO_ENCONTRADO", () -> servicio.liquidarAgua(new AguaRequest("  ")));
        assertCodigo("CONTRATO_NO_ENCONTRADO", () -> servicio.liquidarAgua(new AguaRequest(null)));
        when(catastro.contrato("CTA-1001")).thenReturn(contrato(true));
        assertCodigo("SALDO_AL_CORRIENTE", () -> servicio.liquidarAgua(new AguaRequest(" CTA-1001 ")));
        when(catastro.contrato("CTA-1001")).thenReturn(contrato(false));
        LiquidacionResponse respuesta = servicio.liquidarAgua(new AguaRequest("CTA-1001"));
        assertEquals(new BigDecimal("563.00"), respuesta.total());
        assertEquals(0, respuesta.descuentoPorcentaje());
        assertEquals("AGUA", respuesta.tipo());
    }

    @Test
    void multaAplicaElCincuentaPorCientoDentroDeDiezDiasYNadaFuera() {
        assertCodigo("MULTA_NO_ENCONTRADA", () -> servicio.liquidarMulta(new MultaLiquidacionRequest(" ")));
        when(multas.findByTenantIdAndFolio(any(), eq("MUL-1"))).thenReturn(Optional.empty());
        assertCodigo("MULTA_NO_ENCONTRADA", () -> servicio.liquidarMulta(new MultaLiquidacionRequest("MUL-1")));
        MultaEntity pagada = multa("MUL-2", LocalDate.of(2026, 11, 1), true);
        when(multas.findByTenantIdAndFolio(any(), eq("MUL-2"))).thenReturn(Optional.of(pagada));
        assertCodigo("LIQUIDACION_NO_ACTIVA", () -> servicio.liquidarMulta(new MultaLiquidacionRequest("MUL-2")));

        MultaEntity reciente = multa("MUL-3", LocalDate.of(2026, 11, 1), false);
        when(multas.findByTenantIdAndFolio(any(), eq("MUL-3"))).thenReturn(Optional.of(reciente));
        LiquidacionResponse conDescuento = servicio.liquidarMulta(new MultaLiquidacionRequest(" MUL-3 "));
        assertEquals(new BigDecimal("400.00"), conDescuento.descuento());
        assertEquals(new BigDecimal("400.00"), conDescuento.total());
        assertEquals(50, conDescuento.descuentoPorcentaje());
        assertTrue(conDescuento.concepto().contains("50 %"));

        MultaEntity vieja = multa("MUL-4", LocalDate.of(2026, 10, 1), false);
        when(multas.findByTenantIdAndFolio(any(), eq("MUL-4"))).thenReturn(Optional.of(vieja));
        LiquidacionResponse sinDescuento = servicio.liquidarMulta(new MultaLiquidacionRequest("MUL-4"));
        assertEquals(new BigDecimal("0.00"), sinDescuento.descuento());
        assertEquals(new BigDecimal("800.00"), sinDescuento.total());
        assertEquals(0, sinDescuento.descuentoPorcentaje());
    }

    @Test
    void buscarMultasExigePlacaOFolio() {
        assertCodigo("DATO_INVALIDO", () -> servicio.buscarMultas(" ", " "));
        when(multas.findByTenantIdAndFolio(any(), eq("MUL-3"))).thenReturn(Optional.of(multa("MUL-3", LocalDate.of(2026, 11, 1), false)));
        assertEquals(1, servicio.buscarMultas(null, " MUL-3 ").resultados().size());
        when(multas.findByTenantIdAndPlacaOrderByFechaInfraccionDesc(any(), eq("ABC-123")))
                .thenReturn(List.of(multa("MUL-4", LocalDate.of(2026, 10, 1), false)));
        assertEquals("MUL-4", servicio.buscarMultas(" ABC-123 ", null).resultados().get(0).folio());
        assertEquals("ABC-123", servicio.buscarMultas("ABC-123", null).resultados().get(0).placa());
    }

    @Test
    void cotizaLicenciaDeMilDoscientosOchentaYListaGiros() {
        TarifaLicenciaEntity tarifa = tarifa();
        when(tarifas.findByTenantIdOrderByGiroAsc("05021")).thenReturn(List.of(tarifa));
        assertEquals("ABARROTES", servicio.giros().get(0).giro());
        assertEquals(new BigDecimal("800.00"), servicio.giros().get(0).tarifaBase());
        when(tarifas.findByTenantIdAndGiro("05021", "ABARROTES")).thenReturn(Optional.of(tarifa));
        assertEquals(new BigDecimal("1280.00"), servicio.cotizar(new LicenciaCotizacionRequest(" abarrotes ", new BigDecimal("40"))).total());
        assertCodigo("DATO_INVALIDO", () -> servicio.cotizar(null));
        assertCodigo("GIRO_DESCONOCIDO", () -> servicio.cotizar(new LicenciaCotizacionRequest(" ", new BigDecimal("40"))));
        when(tarifas.findByTenantIdAndGiro(any(), eq("OTRO"))).thenReturn(Optional.empty());
        assertCodigo("GIRO_DESCONOCIDO", () -> servicio.cotizar(new LicenciaCotizacionRequest("OTRO", new BigDecimal("40"))));
        assertCodigo("DATO_INVALIDO", () -> servicio.cotizar(new LicenciaCotizacionRequest("ABARROTES", BigDecimal.ZERO)));
        assertCodigo("DATO_INVALIDO", () -> servicio.cotizar(new LicenciaCotizacionRequest("ABARROTES", null)));
    }

    @Test
    void licenciaInternaExigeFolioNombreYTotal() {
        assertCodigo("DATO_INVALIDO", () -> servicio.crearLicencia(null));
        assertCodigo("DATO_INVALIDO", () -> servicio.crearLicencia(licencia(" ", "Café", new BigDecimal("10"), null)));
        assertCodigo("DATO_INVALIDO", () -> servicio.crearLicencia(licencia("TRM-2026-000001", " ", new BigDecimal("10"), null)));
        assertCodigo("DATO_INVALIDO", () -> servicio.crearLicencia(licencia("TRM-2026-000001", "Café", BigDecimal.ZERO, null)));
        assertCodigo("DATO_INVALIDO", () -> servicio.crearLicencia(licencia("TRM-2026-000001", "Café", null, null)));
        LiquidacionResponse sinTitular = servicio.crearLicencia(licencia("TRM-2026-000001", " Café ", new BigDecimal("1280"), null));
        assertTrue(sinTitular.concepto().contains("Café"));
        assertEquals(new BigDecimal("1280.00"), sinTitular.total());
        LiquidacionResponse conTitular = servicio.crearLicencia(
                licencia("TRM-2026-000002", "Café", new BigDecimal("1280"), "Ana"));
        assertEquals("TRM-2026-000002", conTitular.referencia());
        assertTrue(conTitular.concepto().startsWith("Licencia"));
    }

    @Test
    void consultarMarcaExpiradaYConservaUnaPagada() {
        LiquidacionEntity activa = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA.minusSeconds(1));
        when(liquidaciones.findByTenantIdAndId("05021", activa.id)).thenReturn(Optional.of(activa));
        assertEquals("EXPIRADA", servicio.consultar(activa.id).estado());
        assertEquals("EXPIRADA", activa.estado);

        LiquidacionEntity pagada = liquidacion("AGUA", "CTA-1001", "PAGADA", AHORA.plusSeconds(10));
        when(liquidaciones.findByTenantIdAndId("05021", pagada.id)).thenReturn(Optional.of(pagada));
        assertEquals("PAGADA", servicio.consultar(pagada.id).estado());
        assertCodigo("LIQUIDACION_NO_ENCONTRADA", () -> servicio.consultar(UUID.randomUUID()));

        LiquidacionEntity rota = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA.plusSeconds(10));
        rota.desglose = "no-json";
        when(liquidaciones.findByTenantIdAndId("05021", rota.id)).thenReturn(Optional.of(rota));
        assertThrows(IllegalStateException.class, () -> servicio.consultar(rota.id));

        LiquidacionEntity sinLineas = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA.plusSeconds(10));
        sinLineas.desglose = "{\"titular\":\"Ana\",\"lineas\":{}}";
        when(liquidaciones.findByTenantIdAndId("05021", sinLineas.id)).thenReturn(Optional.of(sinLineas));
        assertTrue(servicio.consultar(sinLineas.id).desglose().isEmpty());
    }

    @Test
    void cobroAutorizadoPagaPredialAguaLicenciaYMultaYElRechazoLaDejaActiva() {
        LiquidacionEntity predial = liquidacion("PREDIAL", "05-021-0147-003:2025-6,2026-1", "ACTIVA", AHORA.plusSeconds(3600));
        when(liquidaciones.findByTenantIdAndId("05021", predial.id)).thenReturn(Optional.of(predial));
        CobroResponse autorizado = servicio.cobrar(predial.id, new CobroRequest(" TARJETA ", " AUTORIZADO "));
        assertEquals("AUTORIZADO", autorizado.estadoPago());
        assertEquals("PAGADA", autorizado.estadoLiquidacion());
        assertEquals("PAG-2026-000001", autorizado.folio());
        assertNull(autorizado.motivo());
        verify(catastro).marcarPredial(eq("05021"), eq("05-021-0147-003"), eq(List.of("2025-6", "2026-1")));

        LiquidacionEntity agua = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA.plusSeconds(3600));
        when(liquidaciones.findByTenantIdAndId("05021", agua.id)).thenReturn(Optional.of(agua));
        servicio.cobrar(agua.id, new CobroRequest("PSE", "AUTORIZADO"));
        verify(catastro).marcarAgua("05021", "CTA-1001");

        UUID tramite = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1");
        LiquidacionEntity licencia = liquidacion("LICENCIA", "TRM-2026-000001", "ACTIVA", AHORA.plusSeconds(3600));
        licencia.desglose = "{\"titular\":\"Ana\",\"tramiteId\":\"" + tramite + "\",\"lineas\":[]}";
        when(liquidaciones.findByTenantIdAndId("05021", licencia.id)).thenReturn(Optional.of(licencia));
        servicio.cobrar(licencia.id, new CobroRequest("TARJETA", "AUTORIZADO"));
        verify(tramites).pagado("05021", "TRM-2026-000001", licencia.id);
        ArgumentCaptor<DocumentosCliente.Emitir> emitir = ArgumentCaptor.forClass(DocumentosCliente.Emitir.class);
        verify(documentos, org.mockito.Mockito.atLeastOnce()).emitir(eq("05021"), emitir.capture());
        assertTrue(emitir.getAllValues().stream().anyMatch(item -> tramite.equals(item.tramiteId())));

        MultaEntity multa = multa("MUL-3", LocalDate.of(2026, 11, 1), false);
        when(multas.findByTenantIdAndFolio("05021", "MUL-3")).thenReturn(Optional.of(multa));
        LiquidacionEntity deMulta = liquidacion("MULTA", "MUL-3", "ACTIVA", AHORA.plusSeconds(3600));
        when(liquidaciones.findByTenantIdAndId("05021", deMulta.id)).thenReturn(Optional.of(deMulta));
        servicio.cobrar(deMulta.id, new CobroRequest("TARJETA", "AUTORIZADO"));
        assertTrue(multa.pagada);

        when(multas.findByTenantIdAndFolio("05021", "MUL-9")).thenReturn(Optional.empty());
        LiquidacionEntity sinMulta = liquidacion("MULTA", "MUL-9", "ACTIVA", AHORA.plusSeconds(3600));
        when(liquidaciones.findByTenantIdAndId("05021", sinMulta.id)).thenReturn(Optional.of(sinMulta));
        servicio.cobrar(sinMulta.id, new CobroRequest("TARJETA", "AUTORIZADO"));

        LiquidacionEntity otra = liquidacion("OTRO", "X", "ACTIVA", AHORA.plusSeconds(3600));
        otra.desglose = "{\"titular\":\"  \",\"tramiteId\":\"\",\"lineas\":[]}";
        when(liquidaciones.findByTenantIdAndId("05021", otra.id)).thenReturn(Optional.of(otra));
        CobroResponse conNombre = servicio.cobrar(otra.id, new CobroRequest("PSE", "AUTORIZADO"));
        assertEquals("PAGADA", conNombre.estadoLiquidacion());

        LiquidacionEntity rechazada = liquidacion("AGUA", "CTA-9", "ACTIVA", AHORA.plusSeconds(3600));
        when(liquidaciones.findByTenantIdAndId("05021", rechazada.id)).thenReturn(Optional.of(rechazada));
        CobroResponse rechazo = servicio.cobrar(rechazada.id, new CobroRequest("TARJETA", "RECHAZADO"));
        assertEquals("RECHAZADO", rechazo.estadoPago());
        assertEquals("ACTIVA", rechazo.estadoLiquidacion());
        assertEquals("ACTIVA", rechazada.estado);
        assertTrue(rechazo.motivo().contains("pasarela"));
    }

    @Test
    void cobroRechazaMedioSimulacionExpiradaYEstadoDistintoDeActiva() {
        assertCodigo("DATO_INVALIDO", () -> servicio.cobrar(UUID.randomUUID(), null));
        assertCodigo("MEDIO_INVALIDO", () -> servicio.cobrar(UUID.randomUUID(), new CobroRequest("EFECTIVO", "AUTORIZADO")));
        assertCodigo("MEDIO_INVALIDO", () -> servicio.cobrar(UUID.randomUUID(), new CobroRequest(null, "AUTORIZADO")));
        assertCodigo("DATO_INVALIDO", () -> servicio.cobrar(UUID.randomUUID(), new CobroRequest("TARJETA", "DUDOSO")));
        assertCodigo("DATO_INVALIDO", () -> servicio.cobrar(UUID.randomUUID(), new CobroRequest("TARJETA", null)));

        LiquidacionEntity vencida = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA.minusSeconds(5));
        when(liquidaciones.findByTenantIdAndId("05021", vencida.id)).thenReturn(Optional.of(vencida));
        when(liquidaciones.findById(vencida.id)).thenReturn(Optional.of(vencida));
        assertCodigo("LIQUIDACION_EXPIRADA", () -> servicio.cobrar(vencida.id, new CobroRequest("TARJETA", "AUTORIZADO")));
        assertEquals("EXPIRADA", vencida.estado);
        verify(entityManager).detach(vencida);

        when(liquidaciones.findById(vencida.id)).thenReturn(Optional.empty());
        LiquidacionEntity otraVencida = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA);
        when(liquidaciones.findByTenantIdAndId("05021", otraVencida.id)).thenReturn(Optional.of(otraVencida));
        when(liquidaciones.findById(otraVencida.id)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> servicio.cobrar(otraVencida.id, new CobroRequest("TARJETA", "RECHAZADO")));

        LiquidacionEntity yaExpirada = liquidacion("AGUA", "CTA-1001", "EXPIRADA", AHORA.plusSeconds(10));
        when(liquidaciones.findByTenantIdAndId("05021", yaExpirada.id)).thenReturn(Optional.of(yaExpirada));
        assertCodigo("LIQUIDACION_EXPIRADA", () -> servicio.cobrar(yaExpirada.id, new CobroRequest("TARJETA", "AUTORIZADO")));

        LiquidacionEntity pagada = liquidacion("AGUA", "CTA-1001", "PAGADA", AHORA.plusSeconds(10));
        when(liquidaciones.findByTenantIdAndId("05021", pagada.id)).thenReturn(Optional.of(pagada));
        assertCodigo("LIQUIDACION_NO_ACTIVA", () -> servicio.cobrar(pagada.id, new CobroRequest("PSE", "AUTORIZADO")));
    }

    @Test
    void unJacksonRotoAlGuardarOLeerLineasFallaEnSeco() throws Exception {
        ObjectMapper roto = mock(ObjectMapper.class);
        when(roto.writeValueAsString(any())).thenThrow(new JsonProcessingException("no") { });
        PagosService conJsonRoto = servicio(roto);
        when(catastro.contrato("CTA-1001")).thenReturn(contrato(false));
        assertThrows(IllegalStateException.class, () -> conJsonRoto.liquidarAgua(new AguaRequest("CTA-1001")));

        ObjectReader lector = mock(ObjectReader.class);
        when(roto.readTree(anyString())).thenReturn(json.readTree("{\"lineas\":[{\"periodo\":\"a\"}]}"));
        when(roto.readerForListOf(Linea.class)).thenReturn(lector);
        doThrow(new JsonProcessingException("no") { }).when(lector).readValue(any(com.fasterxml.jackson.databind.JsonNode.class));
        LiquidacionEntity entidad = liquidacion("AGUA", "CTA-1001", "ACTIVA", AHORA.plusSeconds(10));
        when(liquidaciones.findByTenantIdAndId("05021", entidad.id)).thenReturn(Optional.of(entidad));
        assertThrows(IllegalStateException.class, () -> conJsonRoto.consultar(entidad.id));
    }

    private PagosService servicio(ObjectMapper mapper) {
        return new PagosService(tarifas, multas, liquidaciones, transacciones, catastro, tramites, documentos,
                secuencias, bitacora, mapper, clock, entityManager, transactionManager);
    }

    private static void assertCodigo(String codigo, Runnable accion) {
        assertEquals(codigo, assertThrows(ApiException.class, accion::run).getCodigo());
    }

    private static PredioRemoto predio() {
        return new PredioRemoto("05-021-0147-003", "María López", "Calle 1", new BigDecimal("120"), List.of(
                new PeriodoRemoto("2025-6", new BigDecimal("1500.00"), new BigDecimal("180.00"),
                        new BigDecimal("1680.00"), LocalDate.of(2025, 12, 31), true, false),
                new PeriodoRemoto("2026-1", new BigDecimal("1500.00"), new BigDecimal("0.00"),
                        new BigDecimal("1500.00"), LocalDate.of(2026, 6, 30), false, false)));
    }

    private static ContratoRemoto contrato(boolean pagado) {
        return new ContratoRemoto("CTA-1001", "Ana", "Norte", "2026-2", 100, 128, 28,
                new BigDecimal("18.50"), new BigDecimal("45.00"), new BigDecimal("563.00"), pagado);
    }

    private static MultaEntity multa(String folio, LocalDate fecha, boolean pagada) {
        MultaEntity multa = new MultaEntity();
        multa.id = UUID.randomUUID();
        multa.tenantId = "05021";
        multa.folio = folio;
        multa.placa = "ABC-123";
        multa.descripcion = "Estacionamiento";
        multa.importe = new BigDecimal("800.00");
        multa.fechaInfraccion = fecha;
        multa.pagada = pagada;
        return multa;
    }

    private static TarifaLicenciaEntity tarifa() {
        TarifaLicenciaEntity tarifa = new TarifaLicenciaEntity();
        tarifa.id = UUID.randomUUID();
        tarifa.tenantId = "05021";
        tarifa.giro = "ABARROTES";
        tarifa.tarifaBase = new BigDecimal("800.00");
        tarifa.factorM2 = new BigDecimal("12.00");
        return tarifa;
    }

    private LiquidacionEntity liquidacion(String tipo, String referencia, String estado, Instant vence) {
        LiquidacionEntity entidad = new LiquidacionEntity();
        entidad.id = UUID.randomUUID();
        entidad.tenantId = "05021";
        entidad.tipo = tipo;
        entidad.referencia = referencia;
        entidad.concepto = "Concepto";
        entidad.subtotal = new BigDecimal("10.00");
        entidad.recargos = BigDecimal.ZERO;
        entidad.descuento = BigDecimal.ZERO;
        entidad.total = new BigDecimal("10.00");
        entidad.estado = estado;
        entidad.ciudadanoId = CIUDADANO;
        entidad.desglose = "{\"titular\":\"Ana Ruiz\",\"tramiteId\":null,\"lineas\":[]}";
        entidad.creadaEn = AHORA.minusSeconds(10);
        entidad.venceEn = vence;
        return entidad;
    }

    private static LicenciaInternaRequest licencia(String folio, String nombre, BigDecimal total, String titular) {
        return new LicenciaInternaRequest(CIUDADANO, titular, folio,
                UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1"), nombre, total);
    }
}
