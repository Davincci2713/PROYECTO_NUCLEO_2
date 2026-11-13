package mx.gob.gld.catastro.application;

import mx.gob.gld.catastro.infrastructure.ContratoAguaEntity;
import mx.gob.gld.catastro.infrastructure.ContratoAguaRepository;
import mx.gob.gld.catastro.infrastructure.PeriodoPredialEntity;
import mx.gob.gld.catastro.infrastructure.PeriodoPredialRepository;
import mx.gob.gld.catastro.infrastructure.PredioEntity;
import mx.gob.gld.catastro.infrastructure.PredioRepository;
import mx.gob.gld.catastro.web.CatastroDtos.ContratoResponse;
import mx.gob.gld.catastro.web.CatastroDtos.PredioResponse;
import mx.gob.gld.catastro.web.CatastroDtos.TitularResponse;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatastroServiceTest {

    @Mock PredioRepository predios;
    @Mock PeriodoPredialRepository periodos;
    @Mock ContratoAguaRepository contratos;

    private CatastroService servicio;

    @BeforeEach
    void prepara() {
        servicio = new CatastroService(predios, periodos, contratos,
                Clock.fixed(Instant.parse("2026-11-08T15:00:00Z"), ZoneOffset.UTC));
        TenantContext.set("05021");
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void elPredioSumaBaseMasRecargoYMarcaVencido() {
        assertEquals("CLAVE_CATASTRAL_INVALIDA", codigo(() -> servicio.predio("mala")));
        when(predios.findByTenantIdAndClaveCatastral("05021", "05-021-0147-003")).thenReturn(Optional.empty());
        assertEquals("PREDIOS_NO_ENCONTRADOS", codigo(() -> servicio.predio("05-021-0147-003")));

        PredioEntity predio = new PredioEntity();
        predio.id = UUID.randomUUID();
        predio.claveCatastral = "05-021-0147-003";
        predio.propietario = "María López";
        predio.direccion = "Calle 1";
        predio.superficieM2 = new BigDecimal("120");
        when(predios.findByTenantIdAndClaveCatastral("05021", "05-021-0147-003")).thenReturn(Optional.of(predio));
        PeriodoPredialEntity vencido = periodo("2025-6", LocalDate.of(2025, 12, 31), false);
        PeriodoPredialEntity vigente = periodo("2026-1", LocalDate.of(2026, 12, 31), false);
        when(periodos.findByPredioIdOrderByFechaVencimientoAsc(predio.id)).thenReturn(List.of(vencido, vigente));
        PredioResponse respuesta = servicio.predio("05-021-0147-003");
        assertEquals("María López", respuesta.propietario());
        assertEquals(new BigDecimal("1680.00"), respuesta.periodos().get(0).importePeriodo());
        assertTrue(respuesta.periodos().get(0).vencido());
        assertFalse(respuesta.periodos().get(1).vencido());
        assertEquals(new BigDecimal("120.00"), respuesta.superficieM2());
    }

    @Test
    void marcarPredialYAguaExigeDatosYDejaPagado() {
        assertEquals("CLAVE_CATASTRAL_INVALIDA", codigo(() -> servicio.marcarPredialPagado("x", List.of("2025-6"))));
        assertEquals("PERIODO_NO_DISPONIBLE", codigo(() -> servicio.marcarPredialPagado("05-021-0147-003", List.of())));
        assertEquals("PERIODO_NO_DISPONIBLE", codigo(() -> servicio.marcarPredialPagado("05-021-0147-003", null)));
        when(predios.findByTenantIdAndClaveCatastral("05021", "05-021-0147-003")).thenReturn(Optional.empty());
        assertEquals("PREDIOS_NO_ENCONTRADOS", codigo(() -> servicio.marcarPredialPagado("05-021-0147-003", List.of("2025-6"))));

        PredioEntity predio = new PredioEntity();
        predio.id = UUID.randomUUID();
        when(predios.findByTenantIdAndClaveCatastral("05021", "05-021-0147-003")).thenReturn(Optional.of(predio));
        PeriodoPredialEntity periodo = periodo("2025-6", LocalDate.of(2025, 12, 31), false);
        when(periodos.findByPredioIdOrderByFechaVencimientoAsc(predio.id)).thenReturn(List.of(periodo));
        assertEquals("PERIODO_NO_DISPONIBLE", codigo(() -> servicio.marcarPredialPagado("05-021-0147-003", List.of("2099-1"))));
        servicio.marcarPredialPagado("05-021-0147-003", List.of("2025-6"));
        assertTrue(periodo.pagado);

        assertEquals("CONTRATO_NO_ENCONTRADO", codigo(() -> servicio.contrato(" ")));
        assertEquals("CONTRATO_NO_ENCONTRADO", codigo(() -> servicio.contrato(null)));
        when(contratos.findByTenantIdAndNumeroContrato("05021", "CTA-1001")).thenReturn(Optional.empty());
        assertEquals("CONTRATO_NO_ENCONTRADO", codigo(() -> servicio.contrato("CTA-1001")));
        assertEquals("TOKEN_INVALIDO", codigo(servicio::contratos));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(UUID.randomUUID(), "c@gld.demo", "Ana", "CIUDADANO", "05021", "ok"), null));
        assertEquals("NO_AUTORIZADO", codigo(servicio::contratos));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(UUID.randomUUID(), "s@gld.demo", "Laura", "SERVIDOR_PUBLICO", "05021", "ok"), null));
        ContratoAguaEntity contrato = contrato();
        when(contratos.findByTenantIdOrderByNumeroContratoAsc("05021")).thenReturn(List.of(contrato));
        when(contratos.findByTenantIdAndNumeroContrato("05021", "CTA-1001")).thenReturn(Optional.of(contrato));
        List<ContratoResponse> lista = servicio.contratos();
        assertEquals(28, lista.get(0).consumoM3());
        assertEquals(new BigDecimal("563.00"), lista.get(0).importe());
        assertEquals("CTA-1001", servicio.contrato(" CTA-1001 ").numeroContrato());
        servicio.marcarAguaPagado("CTA-1001");
        assertTrue(contrato.pagado);
        assertEquals("Ana", new TitularResponse("Ana").titular());
    }

    private static String codigo(Runnable accion) {
        return assertThrows(ApiException.class, accion::run).getCodigo();
    }

    private static PeriodoPredialEntity periodo(String clave, LocalDate vence, boolean pagado) {
        PeriodoPredialEntity periodo = new PeriodoPredialEntity();
        periodo.id = UUID.randomUUID();
        periodo.periodo = clave;
        periodo.importeBase = new BigDecimal("1500.00");
        periodo.recargoMora = new BigDecimal("180.00");
        periodo.fechaVencimiento = vence;
        periodo.pagado = pagado;
        return periodo;
    }

    private static ContratoAguaEntity contrato() {
        ContratoAguaEntity contrato = new ContratoAguaEntity();
        contrato.id = UUID.randomUUID();
        contrato.numeroContrato = "CTA-1001";
        contrato.titular = "Ana";
        contrato.direccion = "Norte";
        contrato.periodo = "2026-2";
        contrato.lecturaAnterior = 100;
        contrato.lecturaActual = 128;
        contrato.tarifaM3 = new BigDecimal("18.50");
        contrato.cuotaFija = new BigDecimal("45");
        contrato.pagado = false;
        return contrato;
    }
}
