package mx.gob.gld.identity.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.identity.application.AuthService;
import mx.gob.gld.identity.web.AuthDtos.CodigoRequest;
import mx.gob.gld.identity.web.AuthDtos.LoginRequest;
import mx.gob.gld.identity.web.AuthDtos.LoginResponse;
import mx.gob.gld.identity.web.AuthDtos.RegistroRequest;
import mx.gob.gld.identity.web.AuthDtos.RegistroResponse;
import mx.gob.gld.identity.web.AuthDtos.TotpActivacionResponse;
import mx.gob.gld.identity.web.AuthDtos.TotpResponse;
import mx.gob.gld.identity.web.AuthDtos.UsuarioResponse;
import mx.gob.gld.identity.web.AuthDtos.VerificarCorreoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthWebTest {

    private final AuthService auth = mock(AuthService.class);
    private final MockMvc publico = MockMvcBuilders.standaloneSetup(new AuthController(auth)).build();
    private final MockMvc interno = MockMvcBuilders.standaloneSetup(new AuthInternoController(auth)).build();

    @Test
    void registroLoginYTotpLleganAlServicio() throws Exception {
        UUID id = UUID.fromString("11111111-1111-4111-8111-111111111111");
        when(auth.registrar("Ana", "ana@correo.test", "Demo123!", "5512340001"))
                .thenReturn(new RegistroResponse(id, "ana@correo.test", "482193"));
        when(auth.login("ana@correo.test", "Demo123!")).thenReturn(
                new LoginResponse("token", "Bearer", 3600, false, new UsuarioResponse(id, "Ana", "ana@correo.test", "CIUDADANO", "05021")));
        when(auth.verificarTotp("123456")).thenReturn(new LoginResponse("token", "Bearer", 3600, false, null));
        when(auth.activarTotp()).thenReturn(new TotpActivacionResponse("SECRETO", "otpauth://totp/GLD"));
        when(auth.yo()).thenReturn(new UsuarioResponse(id, "Ana", "ana@correo.test", "CIUDADANO", "05021"));
        when(auth.totpDe(id)).thenReturn(new TotpResponse(true));

        publico.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new RegistroRequest("Ana", "ana@correo.test", "Demo123!", "5512340001"))))
                .andExpect(status().isCreated());
        publico.perform(post("/api/v1/auth/verificar-correo").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new VerificarCorreoRequest("ana@correo.test", "482193"))))
                .andExpect(status().isNoContent());
        publico.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new LoginRequest("ana@correo.test", "Demo123!"))))
                .andExpect(status().isOk());
        publico.perform(post("/api/v1/auth/2fa/verificar").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CodigoRequest("123456"))))
                .andExpect(status().isOk());
        publico.perform(post("/api/v1/auth/2fa/activar")).andExpect(status().isOk());
        publico.perform(post("/api/v1/auth/2fa/confirmar").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CodigoRequest("123456"))))
                .andExpect(status().isNoContent());
        publico.perform(get("/api/v1/auth/me")).andExpect(status().isOk());
        interno.perform(get("/api/v1/auth/interno/usuarios/{id}/totp", id)).andExpect(status().isOk());
        verify(auth).verificarCorreo("ana@correo.test", "482193");
        verify(auth).confirmarTotp("123456");
    }
}
