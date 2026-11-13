package mx.gob.gld.common.security;

import mx.gob.gld.common.error.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public record GldPrincipal(
        UUID id,
        String correo,
        String nombre,
        String rol,
        String tenantId,
        String mfa
) {
    public static GldPrincipal actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof GldPrincipal principal) {
            return principal;
        }
        throw ApiException.of(401, "No autenticado", "Token ausente o vencido.", null, null, "TOKEN_INVALIDO");
    }

    public GldPrincipal conMfa(String nuevoMfa) {
        return new GldPrincipal(id, correo, nombre, rol, tenantId, nuevoMfa);
    }
}
