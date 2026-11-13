package mx.gob.gld.common.security;

import mx.gob.gld.common.error.Errores;

public final class Acceso {

    private Acceso() {
    }

    public static void exigirServidor() {
        String rol = GldPrincipal.actual().rol();
        if (!"SERVIDOR_PUBLICO".equals(rol) && !"ADMINISTRADOR".equals(rol)) {
            throw Errores.noAutorizado();
        }
    }
}
