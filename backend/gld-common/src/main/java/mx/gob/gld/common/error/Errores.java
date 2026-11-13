package mx.gob.gld.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientResponseException;

public final class Errores {

    private Errores() {
    }

    public static ApiException claveCatastralInvalida() {
        return ApiException.of(400, "Datos inválidos",
                "Clave catastral incorrecta. Formato esperado: 05-021-0147-003",
                "claveCatastral", "05-021-0147-003", "CLAVE_CATASTRAL_INVALIDA");
    }

    public static ApiException predioNoEncontrado() {
        return ApiException.of(404, "Predio no encontrado",
                "No hay un predio con esa clave catastral. Ejemplo: 05-021-0147-003",
                "claveCatastral", "05-021-0147-003", "PREDIOS_NO_ENCONTRADOS");
    }

    public static ApiException periodoNoDisponible() {
        return ApiException.of(409, "Periodo no disponible",
                "Uno de los periodos ya está pagado o no existe en el padrón. Ejemplo: 2025-6",
                "periodos", "2025-6", "PERIODO_NO_DISPONIBLE");
    }

    public static ApiException contratoNoEncontrado() {
        return ApiException.of(404, "Contrato no encontrado",
                "No hay un contrato de agua con ese número. Ejemplo: CTA-1001",
                "numeroContrato", "CTA-1001", "CONTRATO_NO_ENCONTRADO");
    }

    public static ApiException saldoAlCorriente() {
        return ApiException.of(409, "Saldo al corriente",
                "El contrato no tiene saldo pendiente. Ejemplo: CTA-1001",
                "numeroContrato", "CTA-1001", "SALDO_AL_CORRIENTE");
    }

    public static ApiException giroDesconocido() {
        return ApiException.of(400, "Datos inválidos",
                "El giro no está en el catálogo de licencias. Ejemplo: ABARROTES",
                "giro", "ABARROTES", "GIRO_DESCONOCIDO");
    }

    public static ApiException superficieInvalida() {
        return ApiException.of(400, "Datos inválidos",
                "La superficie debe ser mayor que cero. Ejemplo: 40",
                "superficieM2", "40", "DATO_INVALIDO");
    }

    public static ApiException multaNoEncontrada() {
        return ApiException.of(404, "Multa no encontrada",
                "No hay una multa con ese folio. Ejemplo: MUL-2026-001",
                "folio", "MUL-2026-001", "MULTA_NO_ENCONTRADA");
    }

    public static ApiException placaObligatoria() {
        return ApiException.of(400, "Datos inválidos",
                "Indica la placa o el folio de la multa. Ejemplo: ABC-123",
                "placa", "ABC-123", "DATO_INVALIDO");
    }

    public static ApiException liquidacionNoEncontrada() {
        return ApiException.of(404, "Liquidación no encontrada",
                "No hay una liquidación con ese identificador.",
                null, null, "LIQUIDACION_NO_ENCONTRADA");
    }

    public static ApiException liquidacionExpirada() {
        return ApiException.of(409, "Liquidación expirada",
                "La liquidación superó las 72 horas y fue marcada EXPIRADA. Vuelve a calcular el adeudo.",
                null, null, "LIQUIDACION_EXPIRADA");
    }

    public static ApiException liquidacionNoActiva() {
        return ApiException.of(409, "Liquidación no activa",
                "La liquidación no está activa y no admite el cobro.",
                null, null, "LIQUIDACION_NO_ACTIVA");
    }

    public static ApiException medioInvalido() {
        return ApiException.of(400, "Datos inválidos",
                "El medio de pago debe ser TARJETA o PSE. Ejemplo: TARJETA",
                "medio", "TARJETA", "MEDIO_INVALIDO");
    }

    public static ApiException correoYaRegistrado() {
        return ApiException.of(409, "Correo ya registrado",
                "Ese correo ya tiene una cuenta en el municipio. Ejemplo: luis@correo.test",
                "correo", "luis@correo.test", "CORREO_YA_REGISTRADO");
    }

    public static ApiException correoNoVerificado() {
        return ApiException.of(403, "Correo no verificado",
                "Confirma el código de verificación antes de iniciar sesión.",
                null, null, "CORREO_NO_VERIFICADO");
    }

    public static ApiException codigoInvalido() {
        return ApiException.of(400, "Código inválido",
                "El código de verificación no coincide. Ejemplo: 482193",
                "codigo", "482193", "CODIGO_INVALIDO");
    }

    public static ApiException credencialesInvalidas() {
        return ApiException.of(401, "Credenciales inválidas",
                "Correo o contraseña incorrectos.",
                null, null, "CREDENCIALES_INVALIDAS");
    }

    public static ApiException totpInvalido() {
        return ApiException.of(400, "Código inválido",
                "El código del autenticador no coincide. Ejemplo: 123456",
                "codigo", "123456", "TOTP_INVALIDO");
    }

    public static ApiException tramiteNoEncontrado() {
        return ApiException.of(404, "Trámite no encontrado",
                "No hay un trámite con ese folio. Ejemplo: TRM-2026-000001",
                "folio", "TRM-2026-000001", "TRAMITE_NO_ENCONTRADO");
    }

    public static ApiException estadoNoPermite() {
        return ApiException.of(409, "Estado no permite la operación",
                "El trámite no está en un estado que admita esta acción.",
                null, null, "ESTADO_NO_PERMITE");
    }

    public static ApiException observacionObligatoria() {
        return ApiException.of(400, "Datos inválidos",
                "La observación es obligatoria al devolver el trámite. Ejemplo: Falta el plano de fachada.",
                "observaciones", "Falta el plano de fachada.", "OBSERVACION_OBLIGATORIA");
    }

    public static ApiException adjuntoObligatorio() {
        return ApiException.of(400, "Datos inválidos",
                "Adjunta al menos un documento. Ejemplo: plano.pdf",
                "documentos", "plano.pdf", "ADJUNTO_OBLIGATORIO");
    }

    public static ApiException yaVoto() {
        return ApiException.of(409, "Voto duplicado",
                "Esta cuenta ya registró su voto en la encuesta.",
                null, null, "YA_VOTO");
    }

    public static ApiException lenguajeNoPermitido() {
        return ApiException.of(400, "Datos inválidos",
                "El mensaje contiene lenguaje no permitido y no se publicó.",
                "texto", "Buenos días, vecinos", "LENGUAJE_NO_PERMITIDO");
    }

    public static ApiException encuestaCerrada() {
        return ApiException.of(409, "Encuesta cerrada",
                "La encuesta ya cerró y no admite votos.",
                null, null, "ENCUESTA_CERRADA");
    }

    public static ApiException noAutorizado() {
        return ApiException.of(403, "No autorizado",
                "El rol no puede realizar esta operación.",
                null, null, "NO_AUTORIZADO");
    }

    public static ApiException datoInvalido(String campo, String ejemplo, String detalle) {
        return ApiException.of(400, "Datos inválidos", detalle, campo, ejemplo, "DATO_INVALIDO");
    }

    public static ApiException noEncontrado(String detalle) {
        return ApiException.of(404, "No encontrado", detalle, null, null, "NO_ENCONTRADO");
    }

    public static ApiException servicioNoDisponible() {
        return ApiException.of(502, "Servicio no disponible",
                "Un servicio interno no respondió. Intenta de nuevo.",
                null, null, "SERVICIO_NO_DISPONIBLE");
    }

    public static ApiException traducir(RestClientResponseException ex, ObjectMapper json) {
        try {
            String body = ex.getResponseBodyAsString();
            if (body == null || body.isBlank()) {
                return servicioNoDisponible();
            }
            return ApiException.de(json.readValue(body, ApiError.class));
        } catch (Exception e) {
            return servicioNoDisponible();
        }
    }
}
