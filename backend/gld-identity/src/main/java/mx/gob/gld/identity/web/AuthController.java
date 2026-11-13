package mx.gob.gld.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.identity.application.AuthService;
import mx.gob.gld.identity.web.AuthDtos.CodigoRequest;
import mx.gob.gld.identity.web.AuthDtos.LoginRequest;
import mx.gob.gld.identity.web.AuthDtos.LoginResponse;
import mx.gob.gld.identity.web.AuthDtos.RegistroRequest;
import mx.gob.gld.identity.web.AuthDtos.RegistroResponse;
import mx.gob.gld.identity.web.AuthDtos.TotpActivacionResponse;
import mx.gob.gld.identity.web.AuthDtos.UsuarioResponse;
import mx.gob.gld.identity.web.AuthDtos.VerificarCorreoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Identidad", description = "HU-18 Registro, verificación y 2FA")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/registro")
    @Operation(summary = "HU-18 Registrar ciudadano")
    public ResponseEntity<RegistroResponse> registro(@RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(auth.registrar(request.nombre(), request.correo(), request.password(), request.telefono()));
    }

    @PostMapping("/verificar-correo")
    @Operation(summary = "HU-18 Verificar correo con el código de 6 dígitos")
    public ResponseEntity<Void> verificarCorreo(@RequestBody VerificarCorreoRequest request) {
        auth.verificarCorreo(request.correo(), request.codigo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    @Operation(summary = "HU-18 Iniciar sesión")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return auth.login(request.correo(), request.password());
    }

    @PostMapping("/2fa/verificar")
    @Operation(summary = "HU-18 Verificar el segundo factor con el pre-token")
    public LoginResponse verificarTotp(@RequestBody CodigoRequest request) {
        return auth.verificarTotp(request.codigo());
    }

    @PostMapping("/2fa/activar")
    @Operation(summary = "HU-18 Activar TOTP y devolver el secreto")
    public TotpActivacionResponse activar() {
        return auth.activarTotp();
    }

    @PostMapping("/2fa/confirmar")
    @Operation(summary = "HU-18 Confirmar el TOTP y exigirlo en el siguiente login")
    public ResponseEntity<Void> confirmar(@RequestBody CodigoRequest request) {
        auth.confirmarTotp(request.codigo());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "HU-18 Consultar el usuario de la sesión")
    public UsuarioResponse me() {
        return auth.yo();
    }
}
