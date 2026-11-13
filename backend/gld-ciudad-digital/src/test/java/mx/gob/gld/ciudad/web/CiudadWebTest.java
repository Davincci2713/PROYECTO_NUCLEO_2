package mx.gob.gld.ciudad.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mx.gob.gld.ciudad.application.CiudadService;
import mx.gob.gld.ciudad.web.CiudadDtos.AltaPerfilRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EncuestaResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.EnsayoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.EventoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoDetalle;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoResumen;
import mx.gob.gld.ciudad.web.CiudadDtos.MensajeChatResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.MensajeForoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PerfilResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PreferenciasResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.SalaResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CiudadWebTest {

    @Test
    void elPortalDeCiudadDelegaCadaRuta() throws Exception {
        CiudadService ciudad = mock(CiudadService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        CiudadController controller = new CiudadController(ciudad);
        CiudadInternoController internoController = new CiudadInternoController(ciudad);
        MockMvc publico = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        MockMvc interno = MockMvcBuilders.standaloneSetup(internoController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        UUID id = UUID.randomUUID();
        Instant ahora = Instant.parse("2026-11-08T15:00:00Z");
        when(ciudad.miPerfil()).thenReturn(new PerfilResponse(id, "Ana", "", "Centro", false));
        when(ciudad.actualizarPerfil(any())).thenReturn(new PerfilResponse(id, "Ana", "Hola", "Centro", true));
        when(ciudad.foros()).thenReturn(List.of(new ForoResumen(id, "T", "C", "Ana", ahora, 0)));
        when(ciudad.crearForo(any())).thenReturn(new ForoResumen(id, "T", "C", "Ana", ahora, 0));
        when(ciudad.foro(id)).thenReturn(new ForoDetalle(id, "T", "C", "Ana", ahora, 0, List.of()));
        when(ciudad.comentar(eq(id), any(), anyBoolean())).thenReturn(new MensajeForoResponse(id, "Ana", "Hola", false, ahora));
        when(ciudad.encuestas()).thenReturn(List.of(new EncuestaResponse(id, "¿?", ahora, false, false, List.of())));
        when(ciudad.crearEncuesta(any())).thenReturn(new EncuestaResponse(id, "¿?", ahora, false, false, List.of()));
        when(ciudad.pymes(any())).thenReturn(List.of(new PymeResponse(id, "Café", "Alimentos", "D", "55", "Norte")));
        when(ciudad.crearPyme(any())).thenReturn(new PymeResponse(id, "Café", "Alimentos", "D", "55", "Norte"));
        when(ciudad.salas()).thenReturn(List.of(new SalaResponse(id, "Centro", "Avisos")));
        when(ciudad.crearSala(any())).thenReturn(new SalaResponse(id, "Centro", "Avisos"));
        when(ciudad.mensajes(id)).thenReturn(List.of(new MensajeChatResponse(id, "Ana", "Hola", ahora)));
        when(ciudad.publicar(eq(id), any())).thenReturn(new MensajeChatResponse(id, "Ana", "Hola", ahora));
        when(ciudad.preferencias()).thenReturn(new PreferenciasResponse("5512340001", List.of("SISMO")));
        when(ciudad.guardarPreferencias(any())).thenReturn(new PreferenciasResponse("5512340001", List.of("SISMO")));
        when(ciudad.ensayo(any())).thenReturn(new EnsayoResponse("SISMO", 1));
        when(ciudad.eventos()).thenReturn(List.of(new EventoResponse(id, "Jornada", "D", "Plaza", ahora, false)));
        when(ciudad.crearEvento(any())).thenReturn(new EventoResponse(id, "Jornada", "D", "Plaza", ahora, false));

        publico.perform(get("/api/v1/ciudad/perfiles/me")).andExpect(status().isOk());
        publico.perform(put("/api/v1/ciudad/perfiles/me").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        publico.perform(get("/api/v1/ciudad/foros")).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/foros").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        publico.perform(get("/api/v1/ciudad/foros/{id}", id)).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/foros/{id}/mensajes", id).contentType(MediaType.APPLICATION_JSON).content("{\"cuerpo\":\"Hola\"}")).andExpect(status().isCreated());
        publico.perform(post("/api/v1/ciudad/foros/{id}/respuesta-oficial", id).contentType(MediaType.APPLICATION_JSON).content("{\"cuerpo\":\"Hola\"}")).andExpect(status().isCreated());
        publico.perform(get("/api/v1/ciudad/encuestas")).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/encuestas").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        publico.perform(post("/api/v1/ciudad/encuestas/{id}/votar", id).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isNoContent());
        publico.perform(get("/api/v1/ciudad/pymes").param("q", "cafe")).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/pymes").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        publico.perform(get("/api/v1/ciudad/salas")).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/salas").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        publico.perform(get("/api/v1/ciudad/salas/{id}/mensajes", id)).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/salas/{id}/mensajes", id).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        publico.perform(get("/api/v1/ciudad/alertas/preferencias")).andExpect(status().isOk());
        publico.perform(put("/api/v1/ciudad/alertas/preferencias").contentType(MediaType.APPLICATION_JSON).content("{\"telefono\":\"55\",\"temas\":[]}")).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/alertas/ensayo").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        publico.perform(get("/api/v1/ciudad/eventos")).andExpect(status().isOk());
        publico.perform(post("/api/v1/ciudad/eventos").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
        publico.perform(post("/api/v1/ciudad/eventos/{id}/inscripcion", id)).andExpect(status().isNoContent());
        publico.perform(delete("/api/v1/ciudad/eventos/{id}/inscripcion", id)).andExpect(status().isNoContent());
        interno.perform(post("/api/v1/ciudad/interno/perfiles").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new AltaPerfilRequest(id, "Ana")))).andExpect(status().isNoContent());
        internoController.alta(null);
        verify(ciudad).votar(eq(id), any());
        verify(ciudad).inscribir(id);
        verify(ciudad).cancelarInscripcion(id);
        verify(ciudad).altaPerfil(null, null);
    }
}
