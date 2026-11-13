package mx.gob.gld.incidencias.domain;

import mx.gob.gld.common.error.Errores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Coordenadas {

    private Coordenadas() {
    }

    public static BigDecimal latitud(BigDecimal lat) {
        if (lat == null || lat.compareTo(new BigDecimal("-90")) < 0 || lat.compareTo(new BigDecimal("90")) > 0) {
            throw Errores.datoInvalido("lat", "19.432600",
                    "La latitud debe estar entre -90 y 90. Ejemplo: 19.432600");
        }
        return lat.setScale(6, RoundingMode.HALF_UP);
    }

    public static BigDecimal longitud(BigDecimal lng) {
        if (lng == null || lng.compareTo(new BigDecimal("-180")) < 0 || lng.compareTo(new BigDecimal("180")) > 0) {
            throw Errores.datoInvalido("lng", "-99.133200",
                    "La longitud debe estar entre -180 y 180. Ejemplo: -99.133200");
        }
        return lng.setScale(6, RoundingMode.HALF_UP);
    }

    public static String referencia(BigDecimal lat, BigDecimal lng) {
        return "Referencia aproximada " + seis(lat) + ", " + seis(lng) + ", zona centro";
    }

    private static String seis(BigDecimal valor) {
        return String.format(Locale.ROOT, "%.6f", valor);
    }
}
