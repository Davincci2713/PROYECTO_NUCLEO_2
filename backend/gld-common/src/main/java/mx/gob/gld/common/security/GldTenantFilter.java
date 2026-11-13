package mx.gob.gld.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.gob.gld.common.error.ApiErrorWriter;
import mx.gob.gld.common.error.ApiException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class GldTenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (exento(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        String tenant = request.getHeader("X-Tenant-ID");
        if (tenant == null || tenant.isBlank()) {
            ApiErrorWriter.write(response, ApiException.of(400, "Datos inválidos",
                    "El encabezado X-Tenant-ID es obligatorio. Ejemplo: 05021",
                    "X-Tenant-ID", "05021", "TENANT_OBLIGATORIO"));
            return;
        }
        TenantContext.set(tenant.trim());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    static boolean exento(String path) {
        return path.startsWith("/actuator/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.equals("/error");
    }
}
