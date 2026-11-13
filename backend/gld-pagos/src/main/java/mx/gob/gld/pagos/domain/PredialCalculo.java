package mx.gob.gld.pagos.domain;

import mx.gob.gld.common.money.Dinero;

import java.math.BigDecimal;
import java.util.List;

public final class PredialCalculo {

    private PredialCalculo() {
    }

    public record Linea(String periodo, BigDecimal importeBase, BigDecimal recargoMora) {
    }

    public record Totales(BigDecimal subtotal, BigDecimal recargos, BigDecimal descuento, BigDecimal total) {
    }

    public static Totales calcular(List<Linea> lineas) {
        BigDecimal subtotal = Dinero.cero();
        BigDecimal recargos = Dinero.cero();
        for (Linea linea : lineas) {
            subtotal = subtotal.add(linea.importeBase()).add(linea.recargoMora());
            recargos = recargos.add(linea.recargoMora());
        }
        subtotal = Dinero.dos(subtotal);
        recargos = Dinero.dos(recargos);
        BigDecimal descuento = Dinero.dos(subtotal.multiply(new BigDecimal("0.02")));
        BigDecimal total = Dinero.dos(subtotal.subtract(descuento));
        return new Totales(subtotal, recargos, descuento, total);
    }
}
