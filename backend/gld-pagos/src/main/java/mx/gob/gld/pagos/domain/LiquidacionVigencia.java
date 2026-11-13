package mx.gob.gld.pagos.domain;

import mx.gob.gld.common.error.Errores;

import java.time.Duration;
import java.time.Instant;

public final class LiquidacionVigencia {

    public static final Duration PLAZO = Duration.ofHours(72);

    private LiquidacionVigencia() {
    }

    public static Instant venceEn(Instant creadaEn) {
        return creadaEn.plus(PLAZO);
    }

    public static boolean expirada(String estado, Instant venceEn, Instant ahora) {
        return "ACTIVA".equals(estado) && !ahora.isBefore(venceEn);
    }

    public static String estadoVisible(String estado, Instant venceEn, Instant ahora) {
        if (expirada(estado, venceEn, ahora)) {
            return "EXPIRADA";
        }
        return estado;
    }

    public static void exigirCobrable(String estado, Instant venceEn, Instant ahora) {
        if ("EXPIRADA".equals(estado) || expirada(estado, venceEn, ahora)) {
            throw Errores.liquidacionExpirada();
        }
        if (!"ACTIVA".equals(estado)) {
            throw Errores.liquidacionNoActiva();
        }
    }
}
