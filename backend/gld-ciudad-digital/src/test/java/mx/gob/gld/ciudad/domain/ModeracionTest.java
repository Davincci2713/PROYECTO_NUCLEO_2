package mx.gob.gld.ciudad.domain;

import mx.gob.gld.common.error.ApiException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModeracionTest {

    @Test
    void palabraBloqueadaResponde400() {
        ApiException ex = assertThrows(ApiException.class,
                () -> Moderacion.validar("Eres un Idiota", List.of("idiota", "estúpido")));
        assertEquals(400, ex.getStatus());
        assertEquals("LENGUAJE_NO_PERMITIDO", ex.getCodigo());
        assertEquals("texto", ex.getCampo());
    }

    @Test
    void comparaSinAcentosYDejaPasarUnMensajeLimpio() {
        assertThrows(ApiException.class,
                () -> Moderacion.validar("Qué comentario tan estúpido", List.of("estupido")));
        assertDoesNotThrow(() -> Moderacion.validar("Buenos días, vecinos", List.of("idiota", "estúpido")));
        assertDoesNotThrow(() -> Moderacion.validar(null, null));
        assertDoesNotThrow(() -> Moderacion.validar("hola", Arrays.asList("  ", null)));
        assertEquals("", Moderacion.normalizar(null));
    }
}
