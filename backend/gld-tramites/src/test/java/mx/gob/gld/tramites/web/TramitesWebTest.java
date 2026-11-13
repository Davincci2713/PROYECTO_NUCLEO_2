package mx.gob.gld.tramites.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mx.gob.gld.tramites.application.TramitesService;
import mx.gob.gld.tramites.web.TramitesDtos.PagadoRequest;
import mx.gob.gld.tramites.web.TramitesDtos.TramiteResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TramitesWebTest {

    @Test
    void elControladorCubreAltasConsultaDictamenYPago() throws Exception {
        TramitesService tramites = mock(TramitesService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MockMvc publico = MockMvcBuilders.standaloneSetup(new TramitesController(tramites))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        MockMvc interno = MockMvcBuilders.standaloneSetup(new TramitesInternoController(tramites))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        TramiteResponse respuesta = new TramiteResponse("TRM-2026-000001", "LICENCIA_FUNCIONAMIENTO", "EN_REVISION",
                "Licencia", UUID.randomUUID(), null, Instant.parse("2026-11-08T15:00:00Z"), Map.of(), List.of());
        when(tramites.licenciaFuncionamiento(any())).thenReturn(respuesta);
        when(tramites.licenciaConstruccion(any())).thenReturn(respuesta);
        when(tramites.porFolio("TRM-2026-000001")).thenReturn(respuesta);
        when(tramites.mios()).thenReturn(List.of(respuesta));
        when(tramites.bandeja()).thenReturn(List.of(respuesta));
        when(tramites.dictaminar(any(), any())).thenReturn(respuesta);
        when(tramites.subsanar(any(), any())).thenReturn(respuesta);
        publico.perform(post("/api/v1/tramites/licencias-funcionamiento").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
        publico.perform(post("/api/v1/tramites/licencias-construccion").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());
        publico.perform(get("/api/v1/tramites/folio/{folio}", "TRM-2026-000001")).andExpect(status().isOk());
        publico.perform(get("/api/v1/tramites/mios")).andExpect(status().isOk());
        publico.perform(get("/api/v1/tramites/bandeja")).andExpect(status().isOk());
        publico.perform(post("/api/v1/tramites/folio/{folio}/dictamen", "TRM-2026-000001")
                .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"APROBAR\"}")).andExpect(status().isOk());
        publico.perform(post("/api/v1/tramites/folio/{folio}/subsanar", "TRM-2026-000001")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        interno.perform(post("/api/v1/tramites/interno/pagado").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new PagadoRequest("TRM-2026-000001", UUID.randomUUID()))))
                .andExpect(status().isNoContent());
        verify(tramites).marcarPagado(any());
    }
}
