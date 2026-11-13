package mx.gob.gld.pagos.domain;

import mx.gob.gld.common.money.Dinero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class MultaCalculo {

    private static final BigDecimal MITAD = new BigDecimal("0.50");

    private MultaCalculo() {
    }

    public static boolean dentroDeProntoPago(LocalDate fechaInfraccion, LocalDate hoy) {
        return ChronoUnit.DAYS.between(fechaInfraccion, hoy) <= 10;
    }

    public static BigDecimal descuento(BigDecimal importe, boolean dentro) {
        if (!dentro) {
            return Dinero.cero();
        }
        return Dinero.dos(importe.multiply(MITAD));
    }

    public static BigDecimal total(BigDecimal importe, boolean dentro) {
        return Dinero.dos(importe.subtract(descuento(importe, dentro)));
    }
}
