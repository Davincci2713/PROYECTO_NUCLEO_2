package mx.gob.gld.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String titulo,
        int status,
        String detalle,
        String campo,
        String ejemplo,
        String codigo
) {
}
