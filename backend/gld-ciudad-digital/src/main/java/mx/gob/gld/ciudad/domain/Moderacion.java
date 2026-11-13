package mx.gob.gld.ciudad.domain;

import mx.gob.gld.common.error.Errores;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;

public final class Moderacion {

    private Moderacion() {
    }

    public static String normalizar(String valor) {
        String base = valor == null ? "" : valor;
        String sinAcentos = Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    public static void validar(String texto, Collection<String> palabras) {
        String normalizado = normalizar(texto);
        if (palabras == null) {
            return;
        }
        for (String palabra : palabras) {
            String bloqueada = normalizar(palabra);
            if (!bloqueada.isBlank() && normalizado.contains(bloqueada)) {
                throw Errores.lenguajeNoPermitido();
            }
        }
    }
}
