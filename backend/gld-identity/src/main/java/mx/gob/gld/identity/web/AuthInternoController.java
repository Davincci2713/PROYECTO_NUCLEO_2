package mx.gob.gld.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.identity.application.AuthService;
import mx.gob.gld.identity.web.AuthDtos.TotpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/interno")
@Tag(name = "Identidad interna", description = "HU-18 Consulta interna de TOTP")
public class AuthInternoController {

    private final AuthService auth;

    public AuthInternoController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/usuarios/{id}/totp")
    @Operation(summary = "HU-18 Consultar si el usuario tiene TOTP activo")
    public TotpResponse totp(@PathVariable UUID id) {
        return auth.totpDe(id);
    }
}
