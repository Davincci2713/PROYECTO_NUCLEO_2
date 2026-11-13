package mx.gob.gld.catastro.domain;

import mx.gob.gld.common.money.Dinero;

import java.math.BigDecimal;

public final class AguaCalculo {

    private AguaCalculo() {
    }

    public static int consumo(int lecturaAnterior, int lecturaActual) {
        return lecturaActual - lecturaAnterior;
    }

    public static BigDecimal importe(int lecturaAnterior, int lecturaActual, BigDecimal tarifaM3, BigDecimal cuotaFija) {
        return Dinero.dos(tarifaM3.multiply(BigDecimal.valueOf(consumo(lecturaAnterior, lecturaActual))).add(cuotaFija));
    }
}
