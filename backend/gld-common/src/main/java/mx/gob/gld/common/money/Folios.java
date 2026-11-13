package mx.gob.gld.common.money;

import java.util.Locale;

public final class Folios {

    private Folios() {
    }

    public static String formato(String prefijo, int anio, long secuencia) {
        return prefijo + "-" + anio + "-" + String.format(Locale.ROOT, "%06d", secuencia);
    }
}
