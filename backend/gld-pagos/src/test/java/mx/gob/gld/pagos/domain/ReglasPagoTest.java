package mx.gob.gld.pagos.domain;

import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.money.Folios;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasPagoTest {

    @Test
    void predialDosPeriodosConDescuentoEnLinea() {
        PredialCalculo.Totales totales = PredialCalculo.calcular(List.of(
                new PredialCalculo.Linea("2025-6", new BigDecimal("1500.00"), new BigDecimal("180.00")),
                new PredialCalculo.Linea("2026-1", new BigDecimal("1500.00"), new BigDecimal("0.00"))));
        assertEquals(new BigDecimal("3180.00"), totales.subtotal());
        assertEquals(new BigDecimal("180.00"), totales.recargos());
        assertEquals(new BigDecimal("63.60"), totales.descuento());
        assertEquals(new BigDecimal("3116.40"), totales.total());
    }

    @Test
    void licenciaAbarrotesCuarentaMetros() {
        assertEquals(new BigDecimal("1280.00"), LicenciaCalculo.total(
                new BigDecimal("800.00"), new BigDecimal("12.00"), new BigDecimal("40")));
    }

    @Test
    void multaDentroDeDiezDiasCobraLaMitadYFueraNoDescuenta() {
        LocalDate hoy = LocalDate.of(2026, 11, 8);
        assertTrue(MultaCalculo.dentroDeProntoPago(hoy.minusDays(3), hoy));
        assertTrue(MultaCalculo.dentroDeProntoPago(hoy.minusDays(10), hoy));
        assertFalse(MultaCalculo.dentroDeProntoPago(hoy.minusDays(11), hoy));
        assertFalse(MultaCalculo.dentroDeProntoPago(hoy.minusDays(40), hoy));
        assertEquals(new BigDecimal("400.00"), MultaCalculo.descuento(new BigDecimal("800.00"), true));
        assertEquals(new BigDecimal("400.00"), MultaCalculo.total(new BigDecimal("800.00"), true));
        assertEquals(new BigDecimal("0.00"), MultaCalculo.descuento(new BigDecimal("1200.00"), false));
        assertEquals(new BigDecimal("1200.00"), MultaCalculo.total(new BigDecimal("1200.00"), false));
    }

    @Test
    void aLasSetentaYDosHorasQuedaExpiradaYCobrarResponde409() {
        Instant creada = Instant.parse("2026-11-07T18:00:00Z");
        Instant vence = LiquidacionVigencia.venceEn(creada);
        assertEquals(creada.plus(Duration.ofHours(72)), vence);
        assertEquals("ACTIVA", LiquidacionVigencia.estadoVisible("ACTIVA", vence, vence.minusSeconds(1)));
        assertEquals("EXPIRADA", LiquidacionVigencia.estadoVisible("ACTIVA", vence, vence));
        ApiException ex = assertThrows(ApiException.class,
                () -> LiquidacionVigencia.exigirCobrable("ACTIVA", vence, vence));
        assertEquals(409, ex.getStatus());
        assertEquals("LIQUIDACION_EXPIRADA", ex.getCodigo());
    }

    @Test
    void rechazoSimuladoDejaLaLiquidacionActiva() {
        assertEquals("ACTIVA", ResultadoCobro.estadoLiquidacion("RECHAZADO"));
        assertEquals("PAGADA", ResultadoCobro.estadoLiquidacion("AUTORIZADO"));
        assertFalse(ResultadoCobro.autorizado("RECHAZADO"));
        assertTrue(ResultadoCobro.autorizado("AUTORIZADO"));
        assertEquals("AUTORIZADA", ResultadoCobro.estadoTransaccion("AUTORIZADO"));
        assertEquals("RECHAZADA", ResultadoCobro.estadoTransaccion("RECHAZADO"));
        assertEquals("DATO_INVALIDO", assertThrows(ApiException.class, () -> ResultadoCobro.estadoLiquidacion("DUDOSO")).getCodigo());
        assertEquals("DATO_INVALIDO", assertThrows(ApiException.class, () -> ResultadoCobro.estadoTransaccion(null)).getCodigo());
        assertEquals("DATO_INVALIDO", assertThrows(ApiException.class, () -> ResultadoCobro.autorizado("DUDOSO")).getCodigo());
        LiquidacionVigencia.exigirCobrable("ACTIVA", venceDePrueba(), venceDePrueba().minusSeconds(5));
        assertEquals("PAGADA", LiquidacionVigencia.estadoVisible("PAGADA", venceDePrueba(), venceDePrueba().plusSeconds(1)));
        assertEquals("LIQUIDACION_NO_ACTIVA", assertThrows(ApiException.class,
                () -> LiquidacionVigencia.exigirCobrable("PAGADA", venceDePrueba(), venceDePrueba().minusSeconds(1))).getCodigo());
        assertEquals("LIQUIDACION_EXPIRADA", assertThrows(ApiException.class,
                () -> LiquidacionVigencia.exigirCobrable("EXPIRADA", venceDePrueba(), venceDePrueba().minusSeconds(1))).getCodigo());
    }

    private static Instant venceDePrueba() {
        return Instant.parse("2026-11-10T18:00:00Z");
    }

    @Test
    void foliosDePagoTramiteEIncidencia() {
        assertEquals("PAG-2026-000001", Folios.formato("PAG", 2026, 1));
        assertEquals("TRM-2026-000001", Folios.formato("TRM", 2026, 1));
        assertEquals("INC-2026-000001", Folios.formato("INC", 2026, 1));
    }
}
