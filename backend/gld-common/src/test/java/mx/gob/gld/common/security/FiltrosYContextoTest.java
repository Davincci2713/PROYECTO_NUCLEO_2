package mx.gob.gld.common.security;

import jakarta.servlet.FilterChain;
import mx.gob.gld.common.error.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FiltrosYContextoTest {

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void elTenantExigeElEncabezadoYLoLimpiaAlSalir() throws Exception {
        GldTenantFilter filtro = new GldTenantFilter();
        FilterChain cadena = mock(FilterChain.class);
        MockHttpServletRequest salud = new MockHttpServletRequest("GET", "/actuator/health/liveness");
        filtro.doFilter(salud, new MockHttpServletResponse(), cadena);
        verify(cadena).doFilter(eq(salud), any());

        MockHttpServletRequest sinTenant = new MockHttpServletRequest("GET", "/api/v1/predios/05-021-0147-003");
        MockHttpServletResponse rechazada = new MockHttpServletResponse();
        filtro.doFilter(sinTenant, rechazada, mock(FilterChain.class));
        assertEquals(400, rechazada.getStatus());
        assertTrue(rechazada.getContentAsString().contains("TENANT_OBLIGATORIO"));

        MockHttpServletRequest conTenant = new MockHttpServletRequest("GET", "/api/v1/pagos/multas");
        conTenant.addHeader("X-Tenant-ID", " 05021 ");
        FilterChain sigue = mock(FilterChain.class);
        filtro.doFilter(conTenant, new MockHttpServletResponse(), sigue);
        verify(sigue).doFilter(eq(conTenant), any());
        assertNull(TenantContext.get());

        assertTrue(GldTenantFilter.exento("/v3/api-docs/swagger-config"));
        assertTrue(GldTenantFilter.exento("/swagger-ui/index.html"));
        assertTrue(GldTenantFilter.exento("/swagger-ui.html"));
        assertTrue(GldTenantFilter.exento("/error"));
        assertFalse(GldTenantFilter.exento("/api/v1/auth/me"));

        assertThrows(ApiException.class, TenantContext::exigir);
        TenantContext.set("   ");
        assertThrows(ApiException.class, TenantContext::exigir);
        TenantContext.set("05021");
        assertEquals("05021", TenantContext.exigir());
    }

    @Test
    void elTokenInternoSoloExigeLaRutaInterna() throws Exception {
        GldInternalTokenFilter filtro = new GldInternalTokenFilter(
                "gld-dev-internal", new Ruta(null, "/api/v1/pagos/interno/**"));
        FilterChain cadena = mock(FilterChain.class);
        MockHttpServletRequest publica = new MockHttpServletRequest("GET", "/api/v1/pagos/multas");
        filtro.doFilter(publica, new MockHttpServletResponse(), cadena);
        verify(cadena).doFilter(eq(publica), any());

        MockHttpServletResponse sinToken = new MockHttpServletResponse();
        filtro.doFilter(interna("POST", "/api/v1/pagos/interno/liquidaciones/licencia"), sinToken, mock(FilterChain.class));
        assertEquals(401, sinToken.getStatus());

        MockHttpServletRequest tokenMalRequest = interna("POST", "/api/v1/pagos/interno/liquidaciones/licencia");
        tokenMalRequest.addHeader("X-Internal-Token", "otro");
        MockHttpServletResponse tokenMal = new MockHttpServletResponse();
        filtro.doFilter(tokenMalRequest, tokenMal, mock(FilterChain.class));
        assertEquals(401, tokenMal.getStatus());

        MockHttpServletRequest tokenOk = interna("POST", "/api/v1/pagos/interno/liquidaciones/licencia");
        tokenOk.addHeader("X-Internal-Token", "gld-dev-internal");
        FilterChain sigue = mock(FilterChain.class);
        filtro.doFilter(tokenOk, new MockHttpServletResponse(), sigue);
        verify(sigue).doFilter(eq(tokenOk), any());
    }

    @Test
    void elJwtDejaPasarPublicosRechazaPreTokenYAceptaLaSesion() throws Exception {
        GldProperties properties = propiedades();
        GldJwtService jwt = new GldJwtService(properties);
        GldPrincipal ciudadano = principal("CIUDADANO");
        RequestMatcherPublicos matchers = new RequestMatcherPublicos();
        GldJwtFilter filtro = new GldJwtFilter(jwt, matchers.publicos, matchers.internos, matchers.preToken);

        MockHttpServletRequest salud = new MockHttpServletRequest("GET", "/actuator/health");
        FilterChain libre = mock(FilterChain.class);
        filtro.doFilter(salud, new MockHttpServletResponse(), libre);
        verify(libre).doFilter(eq(salud), any());

        MockHttpServletRequest login = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        FilterChain loginCadena = mock(FilterChain.class);
        filtro.doFilter(login, new MockHttpServletResponse(), loginCadena);
        verify(loginCadena).doFilter(eq(login), any());

        MockHttpServletRequest interna = new MockHttpServletRequest("GET", "/api/v1/auth/interno/usuarios/1/totp");
        FilterChain internaCadena = mock(FilterChain.class);
        filtro.doFilter(interna, new MockHttpServletResponse(), internaCadena);
        verify(internaCadena).doFilter(eq(interna), any());

        assertEquals(401, rechaza(filtro, peticion("GET", "/api/v1/auth/me")));

        MockHttpServletRequest basico = peticion("GET", "/api/v1/auth/me");
        basico.addHeader("Authorization", "Basic abc");
        assertEquals(401, rechaza(filtro, basico));

        MockHttpServletRequest bearerVacio = peticion("GET", "/api/v1/auth/me");
        bearerVacio.addHeader("Authorization", "Bearer ");
        assertEquals(401, rechaza(filtro, bearerVacio));

        MockHttpServletRequest tokenRoto = peticion("GET", "/api/v1/auth/me");
        tokenRoto.addHeader("Authorization", "Bearer no-es-jwt");
        assertEquals(401, rechaza(filtro, tokenRoto));
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest preEnMe = peticion("GET", "/api/v1/auth/me");
        preEnMe.addHeader("Authorization", "Bearer " + jwt.preToken(ciudadano));
        assertEquals(401, rechaza(filtro, preEnMe));

        MockHttpServletRequest otroTenant = peticion("GET", "/api/v1/auth/me");
        otroTenant.addHeader("Authorization", "Bearer " + jwt.sesion(ciudadano));
        otroTenant.addHeader("X-Tenant-ID", "99999");
        assertEquals(403, rechaza(filtro, otroTenant));

        MockHttpServletRequest me = peticion("GET", "/api/v1/auth/me");
        me.addHeader("Authorization", "Bearer " + jwt.sesion(ciudadano));
        me.addHeader("X-Tenant-ID", "05021");
        FilterChain ok = mock(FilterChain.class);
        filtro.doFilter(me, new MockHttpServletResponse(), ok);
        verify(ok).doFilter(eq(me), any());
        assertEquals(ciudadano.id(), GldPrincipal.actual().id());

        MockHttpServletRequest dosFa = peticion("POST", "/api/v1/auth/2fa/verificar");
        dosFa.addHeader("Authorization", "Bearer " + jwt.sesion(ciudadano));
        assertEquals(401, rechaza(filtro, dosFa));

        MockHttpServletRequest pre = peticion("POST", "/api/v1/auth/2fa/verificar");
        pre.addHeader("Authorization", "Bearer " + jwt.preToken(ciudadano));
        FilterChain preCadena = mock(FilterChain.class);
        filtro.doFilter(pre, new MockHttpServletResponse(), preCadena);
        verify(preCadena).doFilter(eq(pre), any());
    }

    @Test
    void lasRutasComparanMetodoComodinYContexto() {
        Ruta post = new Ruta("POST", "/api/v1/auth/login");
        MockHttpServletRequest get = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        assertFalse(post.matches(get));
        MockHttpServletRequest login = new MockHttpServletRequest("post", "/gld/api/v1/auth/login");
        login.setContextPath("/gld");
        assertTrue(post.matches(login));

        Ruta estrella = new Ruta(null, "/api/v1/predios/*");
        assertTrue(estrella.matches(new MockHttpServletRequest("GET", "/api/v1/predios/05-021-0147-003")));
        assertFalse(estrella.matches(new MockHttpServletRequest("GET", "/api/v1/predios/a/b")));
        Ruta doble = new Ruta(null, "/api/v1/incidencias/**");
        assertTrue(doble.matches(new MockHttpServletRequest("GET", "/api/v1/incidencias/folio/INC-2026-000001")));
        assertFalse(Ruta.ninguna().matches(login));
        assertTrue(Ruta.alguna(post, estrella).matches(login));
        assertFalse(Ruta.alguna(post).matches(get));

        MockHttpServletRequest sinContexto = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        sinContexto.setContextPath(null);
        assertTrue(post.matches(sinContexto));
    }

    @Test
    void elAccesoYElPrincipalDistinguenRolYMfa() {
        assertThrows(ApiException.class, Acceso::exigirServidor);
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("no", null));
        assertThrows(ApiException.class, Acceso::exigirServidor);

        GldPrincipal ciudadano = principal("CIUDADANO");
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(ciudadano, null));
        ApiException negado = assertThrows(ApiException.class, Acceso::exigirServidor);
        assertEquals("NO_AUTORIZADO", negado.getCodigo());

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal("SERVIDOR_PUBLICO"), null));
        Acceso.exigirServidor();
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal("ADMINISTRADOR"), null));
        Acceso.exigirServidor();
        assertEquals("pending", GldPrincipal.actual().conMfa("pending").mfa());
        assertEquals("ADMINISTRADOR", GldPrincipal.actual().rol());
    }

    private static MockHttpServletRequest peticion(String metodo, String ruta) {
        return new MockHttpServletRequest(metodo, ruta);
    }

    private static MockHttpServletRequest interna(String metodo, String ruta) {
        return peticion(metodo, ruta);
    }

    private static int rechaza(GldJwtFilter filtro, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, mock(FilterChain.class));
        return response.getStatus();
    }

    private static GldPrincipal principal(String rol) {
        return new GldPrincipal(
                UUID.fromString("11111111-1111-4111-8111-111111111111"),
                "ciudadano@gld.demo",
                "María López",
                rol,
                "05021",
                "ok");
    }

    private static GldProperties propiedades() {
        GldProperties properties = new GldProperties();
        properties.getJwt().setSecret("gld-dev-secret-cambia-en-produccion-32b");
        properties.getJwt().setSessionSeconds(3600);
        properties.getJwt().setPreTokenSeconds(300);
        return properties;
    }

    private static final class RequestMatcherPublicos {
        private final org.springframework.security.web.util.matcher.RequestMatcher publicos = Ruta.alguna(
                new Ruta("POST", "/api/v1/auth/login"),
                new Ruta("POST", "/api/v1/auth/2fa/verificar"));
        private final org.springframework.security.web.util.matcher.RequestMatcher internos =
                new Ruta(null, "/api/v1/auth/interno/**");
        private final org.springframework.security.web.util.matcher.RequestMatcher preToken =
                new Ruta("POST", "/api/v1/auth/2fa/verificar");
    }
}
