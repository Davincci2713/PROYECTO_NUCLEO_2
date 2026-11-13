package mx.gob.gld.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class ApiErrorWriter {

    private static final ObjectMapper JSON = new ObjectMapper();

    private ApiErrorWriter() {
    }

    public static void write(HttpServletResponse response, ApiException ex) throws IOException {
        response.setStatus(ex.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSON.writeValue(response.getWriter(), ex.cuerpo());
    }
}
