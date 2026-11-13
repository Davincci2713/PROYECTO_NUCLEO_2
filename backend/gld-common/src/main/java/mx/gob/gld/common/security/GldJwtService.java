package mx.gob.gld.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public final class GldJwtService {

    private final SecretKey key;
    private final long sessionSeconds;
    private final long preTokenSeconds;

    public GldJwtService(GldProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.sessionSeconds = properties.getJwt().getSessionSeconds();
        this.preTokenSeconds = properties.getJwt().getPreTokenSeconds();
    }

    public String sesion(GldPrincipal principal) {
        return emitir(principal, "ok", sessionSeconds);
    }

    public String preToken(GldPrincipal principal) {
        return emitir(principal, "pending", preTokenSeconds);
    }

    public long getSessionSeconds() {
        return sessionSeconds;
    }

    public long getPreTokenSeconds() {
        return preTokenSeconds;
    }

    public GldPrincipal parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new GldPrincipal(
                UUID.fromString(claims.getSubject()),
                claims.get("correo", String.class),
                claims.get("nombre", String.class),
                claims.get("rol", String.class),
                claims.get("tenantId", String.class),
                claims.get("mfa", String.class));
    }

    private String emitir(GldPrincipal principal, String mfa, long seconds) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return Jwts.builder()
                .subject(principal.id().toString())
                .claim("correo", principal.correo())
                .claim("nombre", principal.nombre())
                .claim("rol", principal.rol())
                .claim("tenantId", principal.tenantId())
                .claim("mfa", mfa)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(key)
                .compact();
    }
}
