package mx.gob.gld.identity.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegistroRequest(String nombre, String correo, String password, String telefono) {
    }

    public record RegistroResponse(UUID id, String correo, String codigoVerificacion) {
    }

    public record VerificarCorreoRequest(String correo, String codigo) {
    }

    public record LoginRequest(String correo, String password) {
    }

    public record UsuarioResponse(UUID id, String nombre, String correo, String rol, String tenantId) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            boolean requiere2fa,
            @JsonInclude(JsonInclude.Include.NON_NULL) UsuarioResponse usuario
    ) {
    }

    public record CodigoRequest(String codigo) {
    }

    public record TotpActivacionResponse(String secreto, String otpauthUrl) {
    }

    public record TotpResponse(boolean totpActivo) {
    }
}
