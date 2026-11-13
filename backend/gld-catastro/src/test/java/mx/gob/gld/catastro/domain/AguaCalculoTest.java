package mx.gob.gld.catastro.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AguaCalculoTest {

    @Test
    void veintiochoMetrosPorTarifaMasCuotaFija() {
        BigDecimal importe = AguaCalculo.importe(100, 128, new BigDecimal("18.50"), new BigDecimal("45"));
        assertEquals(28, AguaCalculo.consumo(100, 128));
        assertEquals(new BigDecimal("563.00"), importe);
    }
}
