package mx.gob.gld.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "gld.services.identity=http://gld-identity:8081",
        "gld.services.catastro=http://gld-catastro:8082",
        "gld.services.pagos=http://gld-pagos:8083",
        "gld.services.tramites=http://gld-tramites:8084",
        "gld.services.documentos=http://gld-documentos:8085",
        "gld.services.ciudad=http://gld-ciudad-digital:8086",
        "gld.services.incidencias=http://gld-incidencias:8087"
})
class RutasGatewayContextoTest {

    @Autowired
    private RouteLocator locator;

    @Test
    void elContextoSobreescribeLasUriDeLocalhost() {
        Map<String, Route> rutas = locator.getRoutes().collectList().block().stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));
        assertEquals(URI.create("http://gld-identity:8081"), rutas.get("identity").getUri());
        assertEquals(URI.create("http://gld-catastro:8082"), rutas.get("predios").getUri());
        assertEquals(URI.create("http://gld-ciudad-digital:8086"), rutas.get("ciudad").getUri());
        assertEquals(URI.create("http://gld-incidencias:8087"), rutas.get("incidencias").getUri());
    }
}
