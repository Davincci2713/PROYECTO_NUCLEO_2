package mx.gob.gld.incidencias.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mx.gob.gld.incidencias.application.IncidenciaService;
import mx.gob.gld.incidencias.infrastructure.IncidenciaEntity;
import mx.gob.gld.incidencias.web.IncidenciaDtos.AltaRequest;
import mx.gob.gld.incidencias.web.IncidenciaDtos.BrigadaResponse;
import mx.gob.gld.incidencias.web.IncidenciaDtos.IncidenciaResponse;
import mx.gob.gld.incidencias.web.IncidenciaDtos.VistaPreviaResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidenciaWebTest {

    @Test
    void vistaPreviaReporteYFoto() throws Exception {
        IncidenciaService servicio = mock(IncidenciaService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        IncidenciaController controller = new IncidenciaController(servicio);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper), new ByteArrayHttpMessageConverter()).build();
        when(servicio.vistaPrevia(any(), any())).thenReturn(new VistaPreviaResponse("Referencia"));
        when(servicio.vistaPrevia(null, null)).thenReturn(new VistaPreviaResponse("vacia"));
        when(servicio.reportar(any())).thenReturn(new IncidenciaResponse("INC-2026-000001", "BACHE", "ASIGNADA", "Calle",
                new BigDecimal("19.4"), new BigDecimal("-99.1"), "Bache", new BrigadaResponse("Centro", "Centro"),
                Instant.parse("2026-11-08T15:00:00Z"), null));
        when(servicio.porFolio("INC-2026-000001")).thenReturn(new IncidenciaResponse("INC-2026-000001", "BACHE", "ASIGNADA",
                "Calle", new BigDecimal("19.4"), new BigDecimal("-99.1"), "Bache", new BrigadaResponse("Centro", "Centro"),
                Instant.parse("2026-11-08T15:00:00Z"), true));
        when(servicio.mias()).thenReturn(List.of());
        UUID id = UUID.randomUUID();
        IncidenciaEntity conTipo = new IncidenciaEntity();
        conTipo.foto = new byte[] {1};
        conTipo.fotoContentType = "image/jpeg";
        IncidenciaEntity raro = new IncidenciaEntity();
        raro.foto = new byte[] {2};
        raro.fotoContentType = "no es un tipo";
        IncidenciaEntity sinTipo = new IncidenciaEntity();
        sinTipo.foto = new byte[] {3};
        when(servicio.foto(id)).thenReturn(conTipo, raro, sinTipo);

        mvc.perform(post("/api/v1/incidencias/vista-previa").contentType(MediaType.APPLICATION_JSON)
                .content("{\"lat\":19.43,\"lng\":-99.13}")).andExpect(status().isOk());
        assertEquals("vacia", controller.vistaPrevia(null).direccionReferencia());
        mvc.perform(post("/api/v1/incidencias").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/incidencias/folio/{folio}", "INC-2026-000001")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/incidencias/mias")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/incidencias/{id}/foto", id)).andExpect(content().contentType("image/jpeg"));
        mvc.perform(get("/api/v1/incidencias/{id}/foto", id)).andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        mvc.perform(get("/api/v1/incidencias/{id}/foto", id)).andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        assertNotNull(new AltaRequest("BACHE", BigDecimal.ONE, BigDecimal.ONE, "Calle", "Bache", null, null).tipo());
    }
}
