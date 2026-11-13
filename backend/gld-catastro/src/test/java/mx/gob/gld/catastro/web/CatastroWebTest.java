package mx.gob.gld.catastro.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mx.gob.gld.catastro.application.CatastroService;
import mx.gob.gld.catastro.web.CatastroDtos.ContratoResponse;
import mx.gob.gld.catastro.web.CatastroDtos.MarcarAguaRequest;
import mx.gob.gld.catastro.web.CatastroDtos.MarcarPredialRequest;
import mx.gob.gld.catastro.web.CatastroDtos.PredioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatastroWebTest {

    @Test
    void consultaYMarcadoPasanPorElControlador() throws Exception {
        CatastroService catastro = mock(CatastroService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MockMvc predios = MockMvcBuilders.standaloneSetup(new PredioController(catastro))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        MockMvc agua = MockMvcBuilders.standaloneSetup(new AguaController(catastro))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        MockMvc interno = MockMvcBuilders.standaloneSetup(new CatastroInternoController(catastro))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        when(catastro.predio("05-021-0147-003")).thenReturn(new PredioResponse(
                "05-021-0147-003", "María", "Calle", new BigDecimal("120"), List.of()));
        when(catastro.contratos()).thenReturn(List.of(new ContratoResponse(
                "CTA-1001", "Ana", "Norte", "2026-2", 100, 128, 28, new BigDecimal("18.50"),
                new BigDecimal("45.00"), new BigDecimal("563.00"), false)));
        when(catastro.contrato("CTA-1001")).thenReturn(new ContratoResponse(
                "CTA-1001", "Ana", "Norte", "2026-2", 100, 128, 28, new BigDecimal("18.50"),
                new BigDecimal("45.00"), new BigDecimal("563.00"), false));
        predios.perform(get("/api/v1/predios/{clave}", "05-021-0147-003")).andExpect(status().isOk());
        agua.perform(get("/api/v1/agua/contratos")).andExpect(status().isOk());
        agua.perform(get("/api/v1/agua/contratos/{numero}", "CTA-1001")).andExpect(status().isOk());
        interno.perform(post("/api/v1/predios/interno/pagado").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new MarcarPredialRequest("05-021-0147-003", List.of("2025-6")))))
                .andExpect(status().isNoContent());
        interno.perform(post("/api/v1/agua/interno/pagado").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new MarcarAguaRequest("CTA-1001"))))
                .andExpect(status().isNoContent());
        verify(catastro).marcarPredialPagado(any(), any());
        verify(catastro).marcarAguaPagado("CTA-1001");
        assert LocalDate.of(2026, 11, 8).getYear() == 2026;
    }
}
