package mx.gob.gld.documentos.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class DocumentoDtos {

    private DocumentoDtos() {
    }

    public record EmitirRequest(
            String folio,
            String tipo,
            UUID transaccionId,
            UUID tramiteId,
            String titular,
            String concepto,
            BigDecimal monto
    ) {
    }

    public record EmitidoResponse(UUID id, String folio) {
    }

    public record DocumentoResponse(
            UUID id,
            String folio,
            String tipo,
            UUID transaccionId,
            String titular,
            String concepto,
            BigDecimal monto,
            String sello,
            String qrPayload,
            Instant emitidoEn
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerificacionResponse(
            boolean valido,
            String folio,
            String concepto,
            BigDecimal monto,
            String titular,
            Instant emitidoEn,
            String sello
    ) {
    }
}
