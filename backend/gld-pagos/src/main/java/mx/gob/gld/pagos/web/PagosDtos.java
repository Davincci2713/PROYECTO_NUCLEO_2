package mx.gob.gld.pagos.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class PagosDtos {

    private PagosDtos() {
    }

    public record GiroResponse(String giro, BigDecimal tarifaBase, BigDecimal factorM2) {
    }

    public record LicenciaCotizacionRequest(String giro, BigDecimal superficieM2) {
    }

    public record CotizacionResponse(
            String giro,
            BigDecimal tarifaBase,
            BigDecimal factorM2,
            BigDecimal superficieM2,
            BigDecimal total
    ) {
    }

    public record MultaItem(
            String folio,
            String placa,
            String descripcion,
            BigDecimal importe,
            LocalDate fechaInfraccion,
            boolean dentroDeProntoPago,
            BigDecimal descuentoProntoPago,
            BigDecimal totalAPagar,
            boolean pagada
    ) {
    }

    public record MultasResponse(List<MultaItem> resultados) {
    }

    public record PredialRequest(String claveCatastral, List<String> periodos) {
    }

    public record AguaRequest(String numeroContrato) {
    }

    public record MultaLiquidacionRequest(String folio) {
    }

    public record Linea(String periodo, BigDecimal importeBase, BigDecimal recargoMora, BigDecimal importe) {
    }

    public record LiquidacionResponse(
            UUID id,
            String tipo,
            String referencia,
            String concepto,
            String estado,
            BigDecimal subtotal,
            BigDecimal recargos,
            BigDecimal descuento,
            int descuentoPorcentaje,
            BigDecimal total,
            Instant venceEn,
            List<Linea> desglose
    ) {
    }

    public record CobroRequest(String medio, String simular) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CobroResponse(
            String estadoPago,
            UUID liquidacionId,
            String estadoLiquidacion,
            String folio,
            UUID transaccionId,
            UUID documentoId,
            String motivo,
            String concepto,
            BigDecimal total,
            String medio
    ) {
    }

    public record LicenciaInternaRequest(
            UUID ciudadanoId,
            String titular,
            String folioTramite,
            UUID tramiteId,
            String nombreComercial,
            BigDecimal total
    ) {
    }
}
