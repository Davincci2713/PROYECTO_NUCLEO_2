package mx.gob.gld.pagos.domain;

import mx.gob.gld.common.error.Errores;

public final class ResultadoCobro {

    public static final String MOTIVO_RECHAZO =
            "La pasarela rechazó el cargo. La liquidación sigue activa; puedes reintentar con otro medio.";

    private ResultadoCobro() {
    }

    public static boolean autorizado(String simular) {
        validar(simular);
        return "AUTORIZADO".equals(simular);
    }

    public static String estadoLiquidacion(String simular) {
        if ("AUTORIZADO".equals(simular)) {
            return "PAGADA";
        }
        if ("RECHAZADO".equals(simular)) {
            return "ACTIVA";
        }
        throw simulacionInvalida();
    }

    public static String estadoTransaccion(String simular) {
        if ("AUTORIZADO".equals(simular)) {
            return "AUTORIZADA";
        }
        if ("RECHAZADO".equals(simular)) {
            return "RECHAZADA";
        }
        throw simulacionInvalida();
    }

    private static void validar(String simular) {
        if (!"AUTORIZADO".equals(simular) && !"RECHAZADO".equals(simular)) {
            throw simulacionInvalida();
        }
    }

    private static RuntimeException simulacionInvalida() {
        return Errores.datoInvalido("simular", "AUTORIZADO",
                "El resultado simulado debe ser AUTORIZADO o RECHAZADO. Ejemplo: AUTORIZADO");
    }
}
