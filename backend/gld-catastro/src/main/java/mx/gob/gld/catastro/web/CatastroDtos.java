package mx.gob.gld.catastro.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CatastroDtos {

    private CatastroDtos() {
    }

    public record PeriodoResponse(
            String periodo,
            BigDecimal importeBase,
            BigDecimal recargoMora,
            BigDecimal importePeriodo,
            LocalDate fechaVencimiento,
            boolean vencido,
            boolean pagado
    ) {
    }

    public record PredioResponse(
            String claveCatastral,
            String propietario,
            String direccion,
            BigDecimal superficieM2,
            List<PeriodoResponse> periodos
    ) {
    }

    public record ContratoResponse(
            String numeroContrato,
            String titular,
            String direccion,
            String periodo,
            int lecturaAnterior,
            int lecturaActual,
            int consumoM3,
            BigDecimal tarifaM3,
            BigDecimal cuotaFija,
            BigDecimal importe,
            boolean pagado
    ) {
    }

    public record MarcarPredialRequest(String claveCatastral, List<String> periodos) {
    }

    public record MarcarAguaRequest(String numeroContrato) {
    }

    public record TitularResponse(String titular) {
    }
}
