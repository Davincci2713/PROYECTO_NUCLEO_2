package mx.gob.gld.pagos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mx.gob.gld.pagos.application.PagosService;
import mx.gob.gld.pagos.web.PagosDtos.AguaRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroResponse;
import mx.gob.gld.pagos.web.PagosDtos.CotizacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.GiroResponse;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaCotizacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaInternaRequest;
import mx.gob.gld.pagos.web.PagosDtos.LiquidacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.MultaLiquidacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.MultasResponse;
import mx.gob.gld.pagos.web.PagosDtos.PredialRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagosWebTest {

    private final PagosService pagos = mock(PagosService.class);
    private MockMvc publico;
    private MockMvc interno;

    @BeforeEach
    void prepara() {
        publico = mvc(new PagosController(pagos));
        interno = mvc(new PagosInternoController(pagos));
    }

    @Test
    void lasRutasDeleganEnElServicio() throws Exception {
        when(pagos.giros()).thenReturn(List.of(new GiroResponse("ABARROTES", new BigDecimal("800.00"), new BigDecimal("12.00"))));
        when(pagos.cotizar(any())).thenReturn(new CotizacionResponse("ABARROTES", new BigDecimal("800"), new BigDecimal("12"), new BigDecimal("40"), new BigDecimal("1280.00")));
        when(pagos.buscarMultas(any(), any())).thenReturn(new MultasResponse(List.of()));
        LiquidacionResponse liquidacion = new LiquidacionResponse(UUID.randomUUID(), "PREDIAL", "ref", "concepto", "ACTIVA",
                new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("0"), 2, new BigDecimal("1"), Instant.parse("2026-11-11T15:00:00Z"), List.of());
        when(pagos.liquidarPredial(any())).thenReturn(liquidacion);
        when(pagos.liquidarAgua(any())).thenReturn(liquidacion);
        when(pagos.liquidarMulta(any())).thenReturn(liquidacion);
        when(pagos.consultar(any())).thenReturn(liquidacion);
        when(pagos.cobrar(any(), any())).thenReturn(new CobroResponse("AUTORIZADO", liquidacion.id(), "PAGADA", "PAG-2026-000001",
                UUID.randomUUID(), UUID.randomUUID(), null, "concepto", new BigDecimal("1"), "TARJETA"));
        when(pagos.crearLicencia(any())).thenReturn(liquidacion);

        publico.perform(get("/api/v1/pagos/cotizaciones/giros")).andExpect(status().isOk());
        publico.perform(post("/api/v1/pagos/cotizaciones/licencia").contentType(MediaType.APPLICATION_JSON)
                .content("{\"giro\":\"ABARROTES\",\"superficieM2\":40}")).andExpect(status().isOk());
        publico.perform(get("/api/v1/pagos/multas").param("placa", "ABC-123")).andExpect(status().isOk());
        publico.perform(post("/api/v1/pagos/liquidaciones/predial").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        publico.perform(post("/api/v1/pagos/liquidaciones/agua").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        publico.perform(post("/api/v1/pagos/liquidaciones/multa").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        publico.perform(get("/api/v1/pagos/liquidaciones/{id}", liquidacion.id())).andExpect(status().isOk());
        publico.perform(post("/api/v1/pagos/liquidaciones/{id}/cobrar", liquidacion.id()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"medio\":\"TARJETA\",\"simular\":\"AUTORIZADO\"}")).andExpect(status().isOk());
        interno.perform(post("/api/v1/pagos/interno/liquidaciones/licencia").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        verify(pagos).liquidarPredial(any(PredialRequest.class));
        verify(pagos).liquidarAgua(any(AguaRequest.class));
        verify(pagos).liquidarMulta(any(MultaLiquidacionRequest.class));
        verify(pagos).cobrar(any(), any(CobroRequest.class));
        verify(pagos).cotizar(any(LicenciaCotizacionRequest.class));
        verify(pagos).crearLicencia(any(LicenciaInternaRequest.class));
    }

    private static MockMvc mvc(Object controller) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }
}
