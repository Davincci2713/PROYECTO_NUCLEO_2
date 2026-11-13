package mx.gob.gld.tramites.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TramitesDtos {

    private TramitesDtos() {
    }

    public record DocumentoAdjunto(String nombre, String contentType, String contenidoBase64) {
    }

    public record LicenciaFuncionamientoRequest(String nombreComercial, String giro, BigDecimal superficieM2) {
    }

    public record LicenciaConstruccionRequest(
            String tipoObra,
            String claveCatastral,
            String descripcion,
            List<DocumentoAdjunto> documentos
    ) {
    }

    public record DictamenRequest(String decision, String observaciones) {
    }

    public record SubsaniarRequest(List<DocumentoAdjunto> documentos) {
    }

    public record PagadoRequest(String folio, UUID liquidacionId) {
    }

    public record EventoLinea(String estado, String nota, String actor, Instant creadoEn) {
    }

    public record TramiteResponse(
            String folio,
            String tipo,
            String estado,
            String titulo,
            UUID ciudadanoId,
            UUID liquidacionId,
            Instant creadoEn,
            Map<String, Object> payload,
            List<EventoLinea> lineaTiempo
    ) {
    }
}
