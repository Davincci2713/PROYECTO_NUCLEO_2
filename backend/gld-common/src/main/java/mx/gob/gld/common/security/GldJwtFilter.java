package mx.gob.gld.common.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.gob.gld.common.error.ApiErrorWriter;
import mx.gob.gld.common.error.ApiException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class GldJwtFilter extends OncePerRequestFilter {

    private final GldJwtService jwt;
    private final RequestMatcher publicos;
    private final RequestMatcher internos;
    private final RequestMatcher preToken;

    public GldJwtFilter(GldJwtService jwt, RequestMatcher publicos, RequestMatcher internos, RequestMatcher preToken) {
        this.jwt = jwt;
        this.publicos = publicos;
        this.internos = internos;
        this.preToken = preToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (GldTenantFilter.exento(request.getRequestURI()) || internos.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        boolean esPreToken = preToken.matches(request);
        if (publicos.matches(request) && !esPreToken) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
            ApiErrorWriter.write(response, ApiException.of(401, "No autenticado",
                    "Token ausente o vencido.", null, null, "TOKEN_INVALIDO"));
            return;
        }
        try {
            GldPrincipal principal = jwt.parse(header.substring(7).trim());
            if (esPreToken) {
                if (!"pending".equals(principal.mfa())) {
                    ApiErrorWriter.write(response, ApiException.of(401, "No autenticado",
                            "Esta ruta solo acepta el pre-token de 2FA.", null, null, "TOKEN_INVALIDO"));
                    return;
                }
            } else if (!"ok".equals(principal.mfa())) {
                ApiErrorWriter.write(response, ApiException.of(401, "No autenticado",
                        "El pre-token de 2FA solo sirve para verificar el segundo factor.",
                        null, null, "TOKEN_INVALIDO"));
                return;
            }
            String tenant = request.getHeader("X-Tenant-ID");
            if (tenant != null && principal.tenantId() != null && !tenant.trim().equals(principal.tenantId())) {
                ApiErrorWriter.write(response, ApiException.of(403, "No autorizado",
                        "El tenant del token no coincide con X-Tenant-ID.",
                        null, null, "NO_AUTORIZADO"));
                return;
            }
            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + principal.rol())));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            ApiErrorWriter.write(response, ApiException.of(401, "No autenticado",
                    "Token ausente o vencido.", null, null, "TOKEN_INVALIDO"));
        }
    }
}
