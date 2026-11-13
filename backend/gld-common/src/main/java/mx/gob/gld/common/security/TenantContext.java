package mx.gob.gld.common.security;

import mx.gob.gld.common.error.ApiException;

public final class TenantContext {

    private static final ThreadLocal<String> ACTUAL = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(String tenantId) {
        ACTUAL.set(tenantId);
    }

    public static String get() {
        return ACTUAL.get();
    }

    public static String exigir() {
        String tenant = ACTUAL.get();
        if (tenant == null || tenant.isBlank()) {
            throw ApiException.of(400, "Datos inválidos",
                    "El encabezado X-Tenant-ID es obligatorio. Ejemplo: 05021",
                    "X-Tenant-ID", "05021", "TENANT_OBLIGATORIO");
        }
        return tenant;
    }

    public static void clear() {
        ACTUAL.remove();
    }
}
