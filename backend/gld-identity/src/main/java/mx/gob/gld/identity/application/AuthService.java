package mx.gob.gld.identity.application;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.security.GldJwtService;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.identity.infrastructure.UsuarioEntity;
import mx.gob.gld.identity.infrastructure.UsuarioRepository;
import mx.gob.gld.identity.web.AuthDtos.LoginResponse;
import mx.gob.gld.identity.web.AuthDtos.RegistroResponse;
import mx.gob.gld.identity.web.AuthDtos.TotpActivacionResponse;
import mx.gob.gld.identity.web.AuthDtos.TotpResponse;
import mx.gob.gld.identity.web.AuthDtos.UsuarioResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final GldJwtService jwt;
    private final CiudadPerfilCliente ciudad;
    private final SecureRandom random = new SecureRandom();
    private final DefaultCodeVerifier verificadorTotp = crearVerificador();

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, GldJwtService jwt, CiudadPerfilCliente ciudad) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.ciudad = ciudad;
    }

    @Transactional
    public RegistroResponse registrar(String nombre, String correo, String password, String telefono) {
        String tenant = TenantContext.exigir();
        if (nombre == null || nombre.isBlank()) {
            throw Errores.datoInvalido("nombre", "Luis Pérez", "El nombre es obligatorio. Ejemplo: Luis Pérez");
        }
        if (correo == null || !correo.contains("@") || !correo.contains(".")) {
            throw Errores.datoInvalido("correo", "luis@correo.test", "El correo no tiene un formato válido. Ejemplo: luis@correo.test");
        }
        if (password == null || password.length() < 8) {
            throw Errores.datoInvalido("password", "Demo123!", "La contraseña debe tener al menos 8 caracteres. Ejemplo: Demo123!");
        }
        String correoNormalizado = correo.trim().toLowerCase();
        if (usuarios.buscar(tenant, correoNormalizado).isPresent()) {
            throw Errores.correoYaRegistrado();
        }
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.id = UUID.randomUUID();
        usuario.tenantId = tenant;
        usuario.nombre = nombre.trim();
        usuario.correo = correoNormalizado;
        usuario.passwordHash = passwordEncoder.encode(password);
        usuario.telefono = telefono == null || telefono.isBlank() ? null : telefono.trim();
        usuario.rol = "CIUDADANO";
        usuario.correoVerificado = false;
        usuario.codigoVerificacion = codigoSeis();
        usuario.totpActivo = false;
        usuario.creadoEn = Instant.now();
        usuarios.save(usuario);
        ciudad.crear(usuario.id, usuario.nombre, tenant);
        return new RegistroResponse(usuario.id, usuario.correo, usuario.codigoVerificacion);
    }

    @Transactional
    public void verificarCorreo(String correo, String codigo) {
        UsuarioEntity usuario = usuarios.buscar(TenantContext.exigir(), correo == null ? "" : correo.trim())
                .orElseThrow(Errores::codigoInvalido);
        if (codigo == null || usuario.codigoVerificacion == null || !usuario.codigoVerificacion.equals(codigo.trim())) {
            throw Errores.codigoInvalido();
        }
        usuario.correoVerificado = true;
        usuario.codigoVerificacion = null;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String correo, String password) {
        UsuarioEntity usuario = usuarios.buscar(TenantContext.exigir(), correo == null ? "" : correo.trim()).orElse(null);
        if (usuario == null || password == null || !passwordEncoder.matches(password, usuario.passwordHash)) {
            throw Errores.credencialesInvalidas();
        }
        if (!usuario.correoVerificado) {
            throw Errores.correoNoVerificado();
        }
        GldPrincipal principal = principal(usuario);
        if (usuario.totpActivo) {
            return new LoginResponse(jwt.preToken(principal), "Bearer", jwt.getPreTokenSeconds(), true, null);
        }
        return sesion(usuario);
    }

    @Transactional(readOnly = true)
    public LoginResponse verificarTotp(String codigo) {
        GldPrincipal principal = GldPrincipal.actual();
        UsuarioEntity usuario = exigirUsuario(principal.id());
        if (usuario.totpSecreto == null || codigo == null || !verificadorTotp.isValidCode(usuario.totpSecreto, codigo.trim())) {
            throw Errores.totpInvalido();
        }
        return sesion(usuario);
    }

    @Transactional
    public TotpActivacionResponse activarTotp() {
        UsuarioEntity usuario = exigirUsuario(GldPrincipal.actual().id());
        if (usuario.totpActivo) {
            throw Errores.estadoNoPermite();
        }
        String secreto = new DefaultSecretGenerator().generate();
        usuario.totpSecreto = secreto;
        usuario.totpActivo = false;
        QrData data = new QrData.Builder()
                .label(usuario.correo)
                .secret(secreto)
                .issuer("GLD")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        return new TotpActivacionResponse(secreto, data.getUri());
    }

    @Transactional
    public void confirmarTotp(String codigo) {
        UsuarioEntity usuario = exigirUsuario(GldPrincipal.actual().id());
        if (usuario.totpSecreto == null || codigo == null || !verificadorTotp.isValidCode(usuario.totpSecreto, codigo.trim())) {
            throw Errores.totpInvalido();
        }
        usuario.totpActivo = true;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse yo() {
        return usuario(exigirUsuario(GldPrincipal.actual().id()));
    }

    @Transactional(readOnly = true)
    public TotpResponse totpDe(UUID id) {
        UsuarioEntity usuario = exigirUsuario(id);
        if (!usuario.tenantId.equals(TenantContext.exigir())) {
            throw Errores.noEncontrado("No hay un usuario con ese identificador.");
        }
        return new TotpResponse(usuario.totpActivo);
    }

    private LoginResponse sesion(UsuarioEntity usuario) {
        return new LoginResponse(jwt.sesion(principal(usuario)), "Bearer", jwt.getSessionSeconds(), false, usuario(usuario));
    }

    private UsuarioEntity exigirUsuario(UUID id) {
        return usuarios.findById(id).orElseThrow(() -> Errores.noEncontrado("No hay un usuario con ese identificador."));
    }

    private static UsuarioResponse usuario(UsuarioEntity usuario) {
        return new UsuarioResponse(usuario.id, usuario.nombre, usuario.correo, usuario.rol, usuario.tenantId);
    }

    private static GldPrincipal principal(UsuarioEntity usuario) {
        return new GldPrincipal(usuario.id, usuario.correo, usuario.nombre, usuario.rol, usuario.tenantId, "ok");
    }

    private String codigoSeis() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private static DefaultCodeVerifier crearVerificador() {
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        verifier.setAllowedTimePeriodDiscrepancy(1);
        return verifier;
    }
}
