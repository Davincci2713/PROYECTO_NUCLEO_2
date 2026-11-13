package mx.gob.gld.tramites.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.web.ClienteInterno;
import mx.gob.gld.common.web.SolicitudActual;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PagosTramiteCliente {

    private final RestClient http;
    private final ObjectMapper json;
    private final GldProperties properties;

    public PagosTramiteCliente(RestClient http, ObjectMapper json, GldProperties properties) {
        this.http = http;
        this.json = json;
        this.properties = properties;
    }

    public Cotizacion cotizar(String giro, BigDecimal superficieM2) {
        return ClienteInterno.post(http, json,
                properties.getServices().getPagos() + "/api/v1/pagos/cotizaciones/licencia",
                new CotizacionPedido(giro, superficieM2),
                Cotizacion.class,
                SolicitudActual.autorizacion(),
                SolicitudActual.tenant());
    }

    public UUID liquidarLicencia(String tenant, UUID ciudadanoId, String titular, String folio, UUID tramiteId,
                                 String nombreComercial, BigDecimal total) {
        LiquidacionCreada creada = ClienteInterno.postInterno(http, json,
                properties.getServices().getPagos() + "/api/v1/pagos/interno/liquidaciones/licencia",
                new LicenciaPedido(ciudadanoId, titular, folio, tramiteId, nombreComercial, total),
                LiquidacionCreada.class,
                properties.getInternalToken(),
                tenant);
        return creada.id();
    }

    public void exigirPredio(String clave) {
        ClienteInterno.get(http, json,
                properties.getServices().getCatastro() + "/api/v1/predios/" + clave,
                JsonNode.class,
                SolicitudActual.autorizacion(),
                SolicitudActual.tenant());
    }

    public record CotizacionPedido(String giro, BigDecimal superficieM2) {
    }

    public record Cotizacion(String giro, BigDecimal tarifaBase, BigDecimal factorM2, BigDecimal superficieM2, BigDecimal total) {
    }

    public record LicenciaPedido(
            UUID ciudadanoId,
            String titular,
            String folioTramite,
            UUID tramiteId,
            String nombreComercial,
            BigDecimal total
    ) {
    }

    public record LiquidacionCreada(UUID id) {
    }
}
