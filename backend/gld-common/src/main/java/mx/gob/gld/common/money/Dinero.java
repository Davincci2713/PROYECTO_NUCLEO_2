package mx.gob.gld.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Dinero {

    private Dinero() {
    }

    public static BigDecimal dos(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal dos(String valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal cero() {
        return dos("0");
    }
}
