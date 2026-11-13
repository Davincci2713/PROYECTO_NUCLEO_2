package mx.gob.gld.pagos.domain;

import mx.gob.gld.common.money.Dinero;

import java.math.BigDecimal;

public final class LicenciaCalculo {

    private LicenciaCalculo() {
    }

    public static BigDecimal total(BigDecimal tarifaBase, BigDecimal factorM2, BigDecimal superficieM2) {
        return Dinero.dos(tarifaBase.add(superficieM2.multiply(factorM2)));
    }
}
