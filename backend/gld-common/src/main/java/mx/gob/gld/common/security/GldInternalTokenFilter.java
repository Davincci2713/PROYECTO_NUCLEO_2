package mx.gob.gld.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.gob.gld.common.error.ApiErrorWriter;
import mx.gob.gld.common.error.ApiException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class GldInternalTokenFilter extends OncePerRequestFilter {

    private final String tokenEsperado;
    private final RequestMatcher internos;

    public GldInternalTokenFilter(String tokenEsperado, RequestMatcher internos) {
        this.tokenEsperado = tokenEsperado;
        this.internos = internos;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!internos.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader("X-Internal-Token");
        if (token == null || !token.equals(tokenEsperado)) {
            ApiErrorWriter.write(response, ApiException.of(401, "No autenticado",
                    "La llamada interna requiere el encabezado X-Internal-Token.",
                    null, null, "TOKEN_INVALIDO"));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
