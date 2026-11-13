package mx.gob.gld.common.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GldJwtServiceTest {

    @Test
    void sesionConservaClaimsYPreTokenDuraMenos() {
        GldProperties properties = propiedades();
        GldJwtService jwt = new GldJwtService(properties);
        GldPrincipal principal = new GldPrincipal(
                UUID.fromString("11111111-1111-4111-8111-111111111111"),
                "ciudadano@gld.demo",
                "María López Hernández",
                "CIUDADANO",
                "05021",
                "ok");

        GldPrincipal leido = jwt.parse(jwt.sesion(principal));
        assertEquals(principal.id(), leido.id());
        assertEquals("ciudadano@gld.demo", leido.correo());
        assertEquals("María López Hernández", leido.nombre());
        assertEquals("CIUDADANO", leido.rol());
        assertEquals("05021", leido.tenantId());
        assertEquals("ok", leido.mfa());
        assertEquals("pending", jwt.parse(jwt.preToken(principal)).mfa());
        assertEquals(3600, jwt.getSessionSeconds());
        assertEquals(300, jwt.getPreTokenSeconds());
    }

    @Test
    void tokenAlteradoSeRechaza() {
        GldJwtService jwt = new GldJwtService(propiedades());
        assertThrows(Exception.class, () -> jwt.parse("no-es-un-jwt"));
    }

    private static GldProperties propiedades() {
        GldProperties properties = new GldProperties();
        properties.getJwt().setSecret("gld-dev-secret-cambia-en-produccion-32b");
        properties.getJwt().setSessionSeconds(3600);
        properties.getJwt().setPreTokenSeconds(300);
        return properties;
    }
}
