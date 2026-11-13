package mx.gob.gld.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErroresYManejadorTest {

    private final ApiExceptionHandler manejador = new ApiExceptionHandler();
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void cadaErrorDeNegocioConservaCodigoYCampo() {
        assertCodigo("CLAVE_CATASTRAL_INVALIDA", Errores.claveCatastralInvalida());
        assertCodigo("PREDIOS_NO_ENCONTRADOS", Errores.predioNoEncontrado());
        assertCodigo("PERIODO_NO_DISPONIBLE", Errores.periodoNoDisponible());
        assertCodigo("CONTRATO_NO_ENCONTRADO", Errores.contratoNoEncontrado());
        assertCodigo("SALDO_AL_CORRIENTE", Errores.saldoAlCorriente());
        assertCodigo("GIRO_DESCONOCIDO", Errores.giroDesconocido());
        assertCodigo("DATO_INVALIDO", Errores.superficieInvalida());
        assertCodigo("MULTA_NO_ENCONTRADA", Errores.multaNoEncontrada());
        assertCodigo("DATO_INVALIDO", Errores.placaObligatoria());
        assertCodigo("LIQUIDACION_NO_ENCONTRADA", Errores.liquidacionNoEncontrada());
        assertCodigo("LIQUIDACION_EXPIRADA", Errores.liquidacionExpirada());
        assertCodigo("LIQUIDACION_NO_ACTIVA", Errores.liquidacionNoActiva());
        assertCodigo("MEDIO_INVALIDO", Errores.medioInvalido());
        assertCodigo("CORREO_YA_REGISTRADO", Errores.correoYaRegistrado());
        assertCodigo("CORREO_NO_VERIFICADO", Errores.correoNoVerificado());
        assertCodigo("CODIGO_INVALIDO", Errores.codigoInvalido());
        assertCodigo("CREDENCIALES_INVALIDAS", Errores.credencialesInvalidas());
        assertCodigo("TOTP_INVALIDO", Errores.totpInvalido());
        assertCodigo("TRAMITE_NO_ENCONTRADO", Errores.tramiteNoEncontrado());
        assertCodigo("ESTADO_NO_PERMITE", Errores.estadoNoPermite());
        assertCodigo("OBSERVACION_OBLIGATORIA", Errores.observacionObligatoria());
        assertCodigo("ADJUNTO_OBLIGATORIO", Errores.adjuntoObligatorio());
        assertCodigo("YA_VOTO", Errores.yaVoto());
        assertCodigo("LENGUAJE_NO_PERMITIDO", Errores.lenguajeNoPermitido());
        assertCodigo("ENCUESTA_CERRADA", Errores.encuestaCerrada());
        assertCodigo("NO_AUTORIZADO", Errores.noAutorizado());
        assertEquals("correo", Errores.datoInvalido("correo", "ana@gld.demo", "formato").getCampo());
        assertEquals("No encontrado", Errores.noEncontrado("no está").getTitulo());
        assertEquals(502, Errores.servicioNoDisponible().getStatus());
    }

    @Test
    void traducirRespetaElCuerpoYCaeAlServicioSiVieneVacioORoto() throws Exception {
        ApiError original = new ApiError("Voto duplicado", 409, "ya votó", null, null, "YA_VOTO");
        ApiException traducido = Errores.traducir(respuesta(409, json.writeValueAsString(original)), json);
        assertEquals("YA_VOTO", traducido.getCodigo());
        assertEquals(409, traducido.getStatus());
        assertEquals("ya votó", traducido.getDetalle());
        assertNull(traducido.getCampo());
        assertEquals("SERVICIO_NO_DISPONIBLE", Errores.traducir(respuesta(502, " "), json).getCodigo());
        assertEquals("SERVICIO_NO_DISPONIBLE", Errores.traducir(respuesta(500, "no-json"), json).getCodigo());
        assertEquals("SERVICIO_NO_DISPONIBLE", Errores.traducir(respuesta(500, null), json).getCodigo());
    }

    @Test
    void elManejadorTraduceNegocioAccesoCuerpoYErrorInterno() {
        ApiException negocio = Errores.yaVoto();
        ResponseEntity<ApiError> respuesta = manejador.negocio(negocio);
        assertEquals(409, respuesta.getStatusCode().value());
        assertEquals("YA_VOTO", respuesta.getBody().codigo());

        assertEquals(403, manejador.acceso(new AccessDeniedException("no")).getStatusCode().value());
        assertEquals("DATO_INVALIDO", manejador.cuerpo(new IllegalStateException("x")).getBody().codigo());
        assertEquals(500, manejador.desconocido(new IllegalStateException("boom")).getStatusCode().value());
        assertEquals("ERROR_INTERNO", manejador.desconocido(new IllegalStateException("boom")).getBody().codigo());
    }

    @Test
    void elEscritorDejaElJsonEnLaRespuesta() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ApiErrorWriter.write(response, Errores.medioInvalido());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("MEDIO_INVALIDO"));
        assertEquals("TARJETA", json.readTree(response.getContentAsString()).get("ejemplo").asText());
    }

    private static void assertCodigo(String codigo, ApiException ex) {
        assertEquals(codigo, ex.getCodigo());
        assertEquals(codigo, ex.cuerpo().codigo());
        assertEquals(ex.getDetalle(), ex.getMessage());
        assertEquals(ex.getTitulo(), ex.cuerpo().titulo());
        assertEquals(ex.getEjemplo(), ex.cuerpo().ejemplo());
    }

    private static RestClientResponseException respuesta(int status, String body) {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return new RestClientResponseException("error", status, "estado", null, bytes, StandardCharsets.UTF_8);
    }
}
