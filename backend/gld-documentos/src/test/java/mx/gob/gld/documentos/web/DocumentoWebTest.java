package mx.gob.gld.documentos.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mx.gob.gld.documentos.application.DocumentoService;
import mx.gob.gld.documentos.web.DocumentoDtos.DocumentoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitidoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitirRequest;
import mx.gob.gld.documentos.web.DocumentoDtos.VerificacionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentoWebTest {

    @Test
    void verificarConsultarPdfYEmitir() throws Exception {
        DocumentoService documentos = mock(DocumentoService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MockMvc publico = MockMvcBuilders.standaloneSetup(new DocumentoController(documentos))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper), new ByteArrayHttpMessageConverter()).build();
        MockMvc interno = MockMvcBuilders.standaloneSetup(new DocumentoInternoController(documentos))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper), new ByteArrayHttpMessageConverter()).build();
        UUID id = UUID.randomUUID();
        when(documentos.verificar("PAG-2026-000001")).thenReturn(new VerificacionResponse(
                true, "PAG-2026-000001", "Predial", new BigDecimal("10.00"), "Ana", Instant.parse("2026-11-08T15:00:00Z"), "abc"));
        when(documentos.consultar(id)).thenReturn(new DocumentoResponse(
                id, "PAG-2026-000001", "RECIBO_PAGO", UUID.randomUUID(), "Ana", "Predial",
                new BigDecimal("10.00"), "abc", "http://localhost/verificar/PAG-2026-000001", Instant.parse("2026-11-08T15:00:00Z")));
        when(documentos.pdf(id)).thenReturn(new byte[] {'%', 'P'});
        when(documentos.emitir(any())).thenReturn(new EmitidoResponse(id, "PAG-2026-000001"));
        publico.perform(get("/api/v1/documentos/verificar/{folio}", "PAG-2026-000001")).andExpect(status().isOk());
        publico.perform(get("/api/v1/documentos/{id}", id)).andExpect(status().isOk());
        publico.perform(get("/api/v1/documentos/{id}/pdf", id)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));
        interno.perform(post("/api/v1/documentos/interno/emitir").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new EmitirRequest("PAG-2026-000001", "RECIBO_PAGO", UUID.randomUUID(), null, "Ana", "Predial", new BigDecimal("10")))))
                .andExpect(status().isCreated());
        verify(documentos).emitir(any());
    }
}
