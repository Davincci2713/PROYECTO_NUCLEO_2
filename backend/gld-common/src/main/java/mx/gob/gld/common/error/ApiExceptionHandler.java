package mx.gob.gld.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> negocio(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.cuerpo());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> acceso(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Errores.noAutorizado().cuerpo());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> cuerpo(Exception ex) {
        return ResponseEntity.badRequest().body(Errores.datoInvalido(
                "body",
                "{}",
                "El cuerpo o un parámetro de la ruta no tiene el formato esperado.").cuerpo());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> desconocido(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.internalServerError().body(new ApiError(
                "Error interno",
                500,
                "No se pudo completar la operación.",
                null,
                null,
                "ERROR_INTERNO"));
    }
}
