package mx.gob.gld.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RutasGatewayTest {

    @Test
    void cadaRutaDelPortalApuntaAlPuertoDelServicio() {
        try (AnnotationConfigApplicationContext contexto = new AnnotationConfigApplicationContext()) {
            contexto.registerBean(PathRoutePredicateFactory.class);
            contexto.refresh();
            RouteLocator locator = new RutasGateway().rutas(new RouteLocatorBuilder(contexto));
            Map<String, Route> rutas = locator.getRoutes().collectList().block().stream()
                    .collect(Collectors.toMap(Route::getId, Function.identity()));
            assertEquals(8, rutas.size());
            assertUri(rutas, "identity", 8081, "/api/v1/auth/login");
            assertUri(rutas, "identity", 8081, "/api/v1/auth/2fa/verificar");
            assertUri(rutas, "predios", 8082, "/api/v1/predios/05-021-0147-003");
            assertUri(rutas, "agua", 8082, "/api/v1/agua/contratos");
            assertUri(rutas, "agua", 8082, "/api/v1/agua/contratos/CTA-1001");
            assertUri(rutas, "pagos", 8083, "/api/v1/pagos/multas");
            assertUri(rutas, "tramites", 8084, "/api/v1/tramites/bandeja");
            assertUri(rutas, "documentos", 8085, "/api/v1/documentos/verificar/PAG-2026-000001");
            assertUri(rutas, "ciudad", 8086, "/api/v1/ciudad/encuestas");
            assertUri(rutas, "incidencias", 8087, "/api/v1/incidencias/mias");
        }
    }

    @Test
    void lasUriSalenDeLaConfiguracionCuandoNoSonLocalhost() {
        try (AnnotationConfigApplicationContext contexto = new AnnotationConfigApplicationContext()) {
            contexto.registerBean(PathRoutePredicateFactory.class);
            contexto.refresh();
            RouteLocator locator = new RutasGateway(
                    "http://gld-identity:8081",
                    "http://gld-catastro:8082",
                    "http://gld-pagos:8083",
                    "http://gld-tramites:8084",
                    "http://gld-documentos:8085",
                    "http://gld-ciudad-digital:8086",
                    "http://gld-incidencias:8087").rutas(new RouteLocatorBuilder(contexto));
            Map<String, Route> rutas = locator.getRoutes().collectList().block().stream()
                    .collect(Collectors.toMap(Route::getId, Function.identity()));
            assertEquals(URI.create("http://gld-identity:8081"), rutas.get("identity").getUri());
            assertEquals(URI.create("http://gld-catastro:8082"), rutas.get("predios").getUri());
            assertEquals(URI.create("http://gld-catastro:8082"), rutas.get("agua").getUri());
            assertEquals(URI.create("http://gld-pagos:8083"), rutas.get("pagos").getUri());
            assertEquals(URI.create("http://gld-tramites:8084"), rutas.get("tramites").getUri());
            assertEquals(URI.create("http://gld-documentos:8085"), rutas.get("documentos").getUri());
            assertEquals(URI.create("http://gld-ciudad-digital:8086"), rutas.get("ciudad").getUri());
            assertEquals(URI.create("http://gld-incidencias:8087"), rutas.get("incidencias").getUri());
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/auth/login").build());
            assertTrue(Boolean.TRUE.equals(Mono.from(rutas.get("identity").getPredicate().apply(exchange)).block()));
        }
    }

    private static void assertUri(Map<String, Route> rutas, String id, int puerto, String path) {
        Route ruta = rutas.get(id);
        assertEquals(URI.create("http://localhost:" + puerto), ruta.getUri());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
        assertTrue(Boolean.TRUE.equals(Mono.from(ruta.getPredicate().apply(exchange)).block()));
    }
}
