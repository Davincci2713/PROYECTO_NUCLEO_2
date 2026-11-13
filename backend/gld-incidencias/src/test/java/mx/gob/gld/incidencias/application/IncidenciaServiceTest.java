package mx.gob.gld.incidencias.application;

import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.incidencias.infrastructure.BrigadaEntity;
import mx.gob.gld.incidencias.infrastructure.BrigadaRepository;
import mx.gob.gld.incidencias.infrastructure.IncidenciaEntity;
import mx.gob.gld.incidencias.infrastructure.IncidenciaRepository;
import mx.gob.gld.incidencias.web.IncidenciaDtos.AltaRequest;
import mx.gob.gld.incidencias.web.IncidenciaDtos.IncidenciaResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncidenciaServiceTest {

    private static final UUID CIUDADANO = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final Instant AHORA = Instant.parse("2026-11-08T15:00:00Z");

    @Mock IncidenciaRepository incidencias;
    @Mock BrigadaRepository brigadas;
    @Mock Secuencias secuencias;
    @Mock BitacoraService bitacora;

    private IncidenciaService servicio;

    @BeforeEach
    void prepara() {
        servicio = new IncidenciaService(incidencias, brigadas, secuencias, bitacora, Clock.fixed(AHORA, ZoneOffset.UTC));
        TenantContext.set("05021");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(CIUDADANO, "ana@gld.demo", "Ana Ruiz", "CIUDADANO", "05021", "ok"), null));
        when(secuencias.folio(eq("INC"), any())).thenReturn("INC-2026-000001");
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void laVistaPreviaYElReporteAsignanBrigadaSegunElTipo() {
        assertTrue(servicio.vistaPrevia(new BigDecimal("19.4326"), new BigDecimal("-99.1332")).direccionReferencia().contains("19.432600"));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.vistaPrevia(null, new BigDecimal("0"))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.vistaPrevia(new BigDecimal("10"), new BigDecimal("181"))));

        assertEquals("DATO_INVALIDO", codigo(() -> servicio.reportar(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.reportar(alta("HUECO", "19.4", "-99.1", "Calle", "Bache", null, null))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.reportar(alta("bache", "91", "-99.1", "Calle", "Bache", null, null))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.reportar(alta("bache", "19.4", "-99.1", " ", "Bache", null, null))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.reportar(alta("bache", "19.4", "-99.1", "Calle", "x".repeat(801), null, null))));
        when(brigadas.findByTenantIdAndTipo("05021", "ALUMBRADO")).thenReturn(Optional.of(brigada("ALUMBRADO")));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.reportar(alta(" alumbrado ", "19.4", "-99.1", "Calle", "Foco", "%%%", null))));
        when(brigadas.findByTenantIdAndTipo("05021", "BACHE")).thenReturn(Optional.empty());
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.reportar(alta("bache", "19.4326", "-99.1332", "Av. Reforma 120", "Bache profundo", null, null))));

        BrigadaEntity brigada = brigada("BACHE");
        when(brigadas.findByTenantIdAndTipo("05021", "BACHE")).thenReturn(Optional.of(brigada));
        IncidenciaResponse sinFoto = servicio.reportar(alta(" BACHE ", "19.4326", "-99.1332", " Av. Reforma 120 ", " Bache profundo ", " ", null));
        assertEquals("INC-2026-000001", sinFoto.folio());
        assertEquals("ASIGNADA", sinFoto.estado());
        assertEquals("Cuadrilla centro", sinFoto.brigada().nombre());
        assertNull(sinFoto.tieneFoto());

        String foto = Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});
        when(brigadas.findByTenantIdAndTipo("05021", "AGUA")).thenReturn(Optional.of(brigada("AGUA")));
        IncidenciaResponse conFoto = servicio.reportar(alta("agua", "19.4326", "-99.1332", "Calle", "Fuga", foto, " " + "t".repeat(90)));
        assertNull(conFoto.tieneFoto());
        when(brigadas.findByTenantIdAndTipo("05021", "BASURA")).thenReturn(Optional.of(brigada("BASURA")));
        servicio.reportar(alta("basura", "19.4326", "-99.1332", "Calle", "Acumulación", foto, "image/jpeg"));
        IncidenciaResponse esquina = servicio.reportar(alta("basura", "-90", "180", "Calle", "Acumulación", foto, null));
        assertEquals("BASURA", esquina.tipo());
    }

    @Test
    void consultaPorFolioListaYFoto() {
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.porFolio("INC-2026-000009")));
        IncidenciaEntity incidencia = incidencia(new byte[] {9});
        when(incidencias.findByTenantIdAndFolio("05021", "INC-2026-000001")).thenReturn(Optional.of(incidencia));
        when(brigadas.findById(incidencia.brigadaId)).thenReturn(Optional.empty());
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.porFolio("INC-2026-000001")));
        BrigadaEntity brigada = brigada("BACHE");
        brigada.id = incidencia.brigadaId;
        when(brigadas.findById(incidencia.brigadaId)).thenReturn(Optional.of(brigada));
        assertTrue(servicio.porFolio("INC-2026-000001").tieneFoto());

        incidencia.foto = new byte[0];
        assertEquals(Boolean.FALSE, servicio.porFolio("INC-2026-000001").tieneFoto());
        when(incidencias.findByTenantIdAndCiudadanoIdOrderByCreadaEnDesc("05021", CIUDADANO)).thenReturn(List.of(incidencia));
        assertEquals(1, servicio.mias().size());

        when(incidencias.findByTenantIdAndId("05021", incidencia.id)).thenReturn(Optional.empty());
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.foto(incidencia.id)));
        incidencia.foto = null;
        when(incidencias.findByTenantIdAndId("05021", incidencia.id)).thenReturn(Optional.of(incidencia));
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.foto(incidencia.id)));
        incidencia.foto = new byte[] {9};
        assertEquals(9, servicio.foto(incidencia.id).foto[0]);
    }

    private static String codigo(Runnable accion) {
        return assertThrows(ApiException.class, accion::run).getCodigo();
    }

    private static AltaRequest alta(String tipo, String lat, String lng, String direccion, String descripcion, String foto, String contentType) {
        return new AltaRequest(tipo, new BigDecimal(lat), new BigDecimal(lng), direccion, descripcion, foto, contentType);
    }

    private static BrigadaEntity brigada(String tipo) {
        BrigadaEntity brigada = new BrigadaEntity();
        brigada.id = UUID.randomUUID();
        brigada.tipo = tipo;
        brigada.nombre = "Cuadrilla centro";
        brigada.zona = "Centro";
        return brigada;
    }

    private static IncidenciaEntity incidencia(byte[] foto) {
        IncidenciaEntity incidencia = new IncidenciaEntity();
        incidencia.id = UUID.randomUUID();
        incidencia.folio = "INC-2026-000001";
        incidencia.tipo = "BACHE";
        incidencia.estado = "ASIGNADA";
        incidencia.direccionReferencia = "Calle";
        incidencia.lat = new BigDecimal("19.432600");
        incidencia.lng = new BigDecimal("-99.133200");
        incidencia.descripcion = "Bache";
        incidencia.brigadaId = UUID.randomUUID();
        incidencia.creadaEn = AHORA;
        incidencia.foto = foto;
        return incidencia;
    }
}
