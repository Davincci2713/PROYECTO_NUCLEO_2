package mx.gob.gld.identity.application;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.security.GldJwtService;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.identity.infrastructure.UsuarioEntity;
import mx.gob.gld.identity.infrastructure.UsuarioRepository;
import mx.gob.gld.identity.web.AuthDtos.LoginResponse;
import mx.gob.gld.identity.web.AuthDtos.RegistroResponse;
import mx.gob.gld.identity.web.AuthDtos.TotpActivacionResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final UUID ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CiudadPerfilCliente ciudad;

    private AuthService servicio;

    @BeforeEach
    void prepara() {
        GldProperties properties = new GldProperties();
        properties.getJwt().setSecret("gld-dev-secret-cambia-en-produccion-32b");
        servicio = new AuthService(usuarios, passwordEncoder, new GldJwtService(properties), ciudad);
        TenantContext.set("05021");
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(passwordEncoder.matches("Demo123!", "hash")).thenReturn(true);
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);
        when(usuarios.buscar(any(), any())).thenReturn(Optional.empty());
        when(usuarios.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarVerificaYRechazaDuplicadoODatosMalos() {
        assertCodigo("DATO_INVALIDO", () -> servicio.registrar(" ", "ana@correo.test", "Demo123!", null));
        assertCodigo("DATO_INVALIDO", () -> servicio.registrar("Ana", "sin-arroba", "Demo123!", null));
        assertCodigo("DATO_INVALIDO", () -> servicio.registrar("Ana", "ana@correo", "Demo123!", null));
        assertCodigo("DATO_INVALIDO", () -> servicio.registrar("Ana", null, "Demo123!", null));
        assertCodigo("DATO_INVALIDO", () -> servicio.registrar("Ana", "ana@correo.test", "corta", null));
        assertCodigo("DATO_INVALIDO", () -> servicio.registrar("Ana", "ana@correo.test", null, null));
        when(usuarios.buscar("05021", "ana@correo.test")).thenReturn(Optional.of(usuario(true, false)));
        assertCodigo("CORREO_YA_REGISTRADO", () -> servicio.registrar("Ana", "ana@correo.test", "Demo123!", null));

        when(usuarios.buscar("05021", "ana@correo.test")).thenReturn(Optional.empty());
        RegistroResponse alta = servicio.registrar(" Ana Ruiz ", " Ana@Correo.test ", "Demo123!", " 5512340001 ");
        assertEquals("ana@correo.test", alta.correo());
        assertEquals(6, alta.codigoVerificacion().length());
        verify(ciudad, org.mockito.Mockito.atLeastOnce()).crear(any(), any(), org.mockito.ArgumentMatchers.eq("05021"));

        RegistroResponse sinTelefono = servicio.registrar("Ana", "ana@correo.test", "Demo123!", " ");
        assertNotNull(sinTelefono.id());

        UsuarioEntity guardado = usuario(false, false);
        guardado.codigoVerificacion = "482193";
        when(usuarios.buscar("05021", "ana@correo.test")).thenReturn(Optional.of(guardado));
        assertCodigo("CODIGO_INVALIDO", () -> servicio.verificarCorreo("ana@correo.test", "000000"));
        assertCodigo("CODIGO_INVALIDO", () -> servicio.verificarCorreo("ana@correo.test", null));
        guardado.codigoVerificacion = null;
        assertCodigo("CODIGO_INVALIDO", () -> servicio.verificarCorreo("ana@correo.test", "482193"));
        guardado.codigoVerificacion = "482193";
        when(usuarios.buscar("05021", "otro@correo.test")).thenReturn(Optional.empty());
        assertCodigo("CODIGO_INVALIDO", () -> servicio.verificarCorreo("otro@correo.test", "482193"));
        assertCodigo("CODIGO_INVALIDO", () -> servicio.verificarCorreo(null, "482193"));
        servicio.verificarCorreo(" ana@correo.test ", " 482193 ");
        assertTrue(guardado.correoVerificado);
        assertNull(guardado.codigoVerificacion);
    }

    @Test
    void loginRechazaPasswordCorreoNoVerificadoYPideTotp() {
        assertCodigo("CREDENCIALES_INVALIDAS", () -> servicio.login("ana@correo.test", "Demo123!"));
        UsuarioEntity usuario = usuario(false, false);
        when(usuarios.buscar("05021", "ana@correo.test")).thenReturn(Optional.of(usuario));
        assertCodigo("CREDENCIALES_INVALIDAS", () -> servicio.login("ana@correo.test", "mala"));
        assertCodigo("CREDENCIALES_INVALIDAS", () -> servicio.login("ana@correo.test", null));
        assertCodigo("CORREO_NO_VERIFICADO", () -> servicio.login(" ana@correo.test ", "Demo123!"));
        usuario.correoVerificado = true;
        LoginResponse sesion = servicio.login("ana@correo.test", "Demo123!");
        assertFalse(sesion.requiere2fa());
        assertEquals("Bearer", sesion.tokenType());
        assertEquals("Ana Ruiz", sesion.usuario().nombre());
        assertEquals("ana@correo.test", sesion.usuario().correo());

        usuario.totpActivo = true;
        LoginResponse pre = servicio.login("ana@correo.test", "Demo123!");
        assertTrue(pre.requiere2fa());
        assertNull(pre.usuario());
        assertEquals(300, pre.expiresIn());
    }

    @Test
    void totpSeActivaConfirmaYRechazaElCodigoMal() throws CodeGenerationException {
        entra(usuario(true, false));
        when(usuarios.findById(ID)).thenReturn(Optional.empty());
        assertCodigo("NO_ENCONTRADO", servicio::yo);
        assertCodigo("NO_ENCONTRADO", servicio::activarTotp);
        assertCodigo("NO_ENCONTRADO", () -> servicio.verificarTotp("123456"));
        assertCodigo("NO_ENCONTRADO", () -> servicio.confirmarTotp("123456"));
        assertCodigo("NO_ENCONTRADO", () -> servicio.totpDe(ID));

        UsuarioEntity usuario = usuario(true, true);
        when(usuarios.findById(ID)).thenReturn(Optional.of(usuario));
        assertCodigo("ESTADO_NO_PERMITE", servicio::activarTotp);
        usuario.totpActivo = false;
        usuario.totpSecreto = null;
        TotpActivacionResponse alta = servicio.activarTotp();
        assertEquals(alta.secreto(), usuario.totpSecreto);
        assertTrue(alta.otpauthUrl().startsWith("otpauth://totp/"));
        assertFalse(usuario.totpActivo);
        assertEquals("Ana Ruiz", servicio.yo().nombre());
        assertFalse(servicio.totpDe(ID).totpActivo());

        assertCodigo("TOTP_INVALIDO", () -> servicio.confirmarTotp(null));
        assertCodigo("TOTP_INVALIDO", () -> servicio.confirmarTotp("000000"));
        assertCodigo("TOTP_INVALIDO", () -> servicio.verificarTotp("000000"));
        usuario.totpSecreto = null;
        assertCodigo("TOTP_INVALIDO", () -> servicio.verificarTotp("123456"));
        usuario.totpSecreto = alta.secreto();
        String codigo = new DefaultCodeGenerator().generate(alta.secreto(), Instant.now().getEpochSecond() / 30);
        servicio.confirmarTotp(" " + codigo + " ");
        assertTrue(usuario.totpActivo);
        LoginResponse sesion = servicio.verificarTotp(codigo);
        assertFalse(sesion.requiere2fa());
        assertEquals(ID, sesion.usuario().id());

        usuario.tenantId = "99999";
        assertCodigo("NO_ENCONTRADO", () -> servicio.totpDe(ID));
    }

    private void entra(UsuarioEntity usuario) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(usuario.id, usuario.correo, usuario.nombre, usuario.rol, usuario.tenantId, "ok"), null));
    }

    private static void assertCodigo(String codigo, Runnable accion) {
        assertEquals(codigo, assertThrows(ApiException.class, accion::run).getCodigo());
    }

    private static UsuarioEntity usuario(boolean verificado, boolean totp) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.id = ID;
        usuario.tenantId = "05021";
        usuario.nombre = "Ana Ruiz";
        usuario.correo = "ana@correo.test";
        usuario.passwordHash = "hash";
        usuario.rol = "CIUDADANO";
        usuario.correoVerificado = verificado;
        usuario.totpActivo = totp;
        usuario.totpSecreto = totp ? "JBSWY3DPEHPK3PXP" : null;
        usuario.creadoEn = Instant.parse("2026-11-01T00:00:00Z");
        return usuario;
    }
}
