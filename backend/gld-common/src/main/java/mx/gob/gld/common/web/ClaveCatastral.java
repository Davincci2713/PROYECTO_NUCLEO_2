package mx.gob.gld.common.web;

import java.util.regex.Pattern;

public final class ClaveCatastral {

    public static final Pattern FORMATO = Pattern.compile("\\d{2}-\\d{3}-\\d{4}-\\d{3}");

    private ClaveCatastral() {
    }

    public static boolean valida(String clave) {
        return clave != null && FORMATO.matcher(clave).matches();
    }
}
