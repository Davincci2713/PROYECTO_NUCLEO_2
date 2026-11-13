package mx.gob.gld.common.web;

import jakarta.servlet.http.HttpServletRequest;
import mx.gob.gld.common.security.TenantContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class SolicitudActual {

    private SolicitudActual() {
    }

    public static String autorizacion() {
        String value = request().getHeader("Authorization");
        return value == null ? "" : value;
    }

    public static String tenant() {
        String tenant = TenantContext.get();
        if (tenant != null && !tenant.isBlank()) {
            return tenant;
        }
        String header = request().getHeader("X-Tenant-ID");
        return header == null ? "" : header;
    }

    private static HttpServletRequest request() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("No hay una solicitud HTTP activa");
        }
        return attrs.getRequest();
    }
}
