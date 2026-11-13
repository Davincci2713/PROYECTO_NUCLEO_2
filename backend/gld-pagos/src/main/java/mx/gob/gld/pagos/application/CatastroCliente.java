package mx.gob.gld.pagos.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.web.ClienteInterno;
import mx.gob.gld.common.web.SolicitudActual;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class CatastroCliente {

    private final RestClient http;
    private final ObjectMapper json;
    private final GldProperties properties;

    public CatastroCliente(RestClient http, ObjectMapper json, GldProperties properties) {
        this.http = http;
        this.json = json;
        this.properties = properties;
    }

    public PredioRemoto predio(String clave) {
        return ClienteInterno.get(http, json, properties.getServices().getCatastro() + "/api/v1/predios/" + clave,
                PredioRemoto.class, SolicitudActual.autorizacion(), SolicitudActual.tenant());
    }

    public ContratoRemoto contrato(String numero) {
        return ClienteInterno.get(http, json,
                properties.getServices().getCatastro() + "/api/v1/agua/contratos/" + numero,
                ContratoRemoto.class, SolicitudActual.autorizacion(), SolicitudActual.tenant());
    }

    public void marcarPredial(String tenant, String clave, List<String> periodos) {
        ClienteInterno.postInternoVacio(http, json,
                properties.getServices().getCatastro() + "/api/v1/predios/interno/pagado",
                new MarcarPredial(clave, periodos), properties.getInternalToken(), tenant);
    }

    public void marcarAgua(String tenant, String numero) {
        ClienteInterno.postInternoVacio(http, json,
                properties.getServices().getCatastro() + "/api/v1/agua/interno/pagado",
                new MarcarAgua(numero), properties.getInternalToken(), tenant);
    }

    public record PeriodoRemoto(
            String periodo,
            BigDecimal importeBase,
            BigDecimal recargoMora,
            BigDecimal importePeriodo,
            LocalDate fechaVencimiento,
            boolean vencido,
            boolean pagado
    ) {
    }

    public record PredioRemoto(
            String claveCatastral,
            String propietario,
            String direccion,
            BigDecimal superficieM2,
            List<PeriodoRemoto> periodos
    ) {
    }

    public record ContratoRemoto(
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

    public record MarcarPredial(String claveCatastral, List<String> periodos) {
    }

    public record MarcarAgua(String numeroContrato) {
    }
}
