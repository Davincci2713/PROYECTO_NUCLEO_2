package mx.gob.gld.pagos.config;

import mx.gob.gld.common.bitacora.BitacoraRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class PlataformaConfigTest {

    @Test
    void publicaLaBitacoraDelServicio() {
        assertNotNull(new PlataformaConfig().bitacoraService(mock(BitacoraRepository.class)));
    }
}
