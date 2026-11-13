package mx.gob.gld.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RutasGateway {

    private final String identity;
    private final String catastro;
    private final String pagos;
    private final String tramites;
    private final String documentos;
    private final String ciudad;
    private final String incidencias;

    /** Conserva http://localhost:8081 … 8087 para el arranque con Maven en la máquina. */
    public RutasGateway() {
        this("http://localhost:8081",
                "http://localhost:8082",
                "http://localhost:8083",
                "http://localhost:8084",
                "http://localhost:8085",
                "http://localhost:8086",
                "http://localhost:8087");
    }

    @Autowired
    public RutasGateway(
            @Value("${gld.services.identity:http://localhost:8081}") String identity,
            @Value("${gld.services.catastro:http://localhost:8082}") String catastro,
            @Value("${gld.services.pagos:http://localhost:8083}") String pagos,
            @Value("${gld.services.tramites:http://localhost:8084}") String tramites,
            @Value("${gld.services.documentos:http://localhost:8085}") String documentos,
            @Value("${gld.services.ciudad:http://localhost:8086}") String ciudad,
            @Value("${gld.services.incidencias:http://localhost:8087}") String incidencias) {
        this.identity = identity;
        this.catastro = catastro;
        this.pagos = pagos;
        this.tramites = tramites;
        this.documentos = documentos;
        this.ciudad = ciudad;
        this.incidencias = incidencias;
    }

    @Bean
    RouteLocator rutas(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity", r -> r.path(
                        "/api/v1/auth/registro",
                        "/api/v1/auth/verificar-correo",
                        "/api/v1/auth/login",
                        "/api/v1/auth/2fa/**",
                        "/api/v1/auth/me").uri(identity))
                .route("predios", r -> r.path("/api/v1/predios/*").uri(catastro))
                .route("agua", r -> r.path("/api/v1/agua/contratos", "/api/v1/agua/contratos/**").uri(catastro))
                .route("pagos", r -> r.path(
                        "/api/v1/pagos/cotizaciones/**",
                        "/api/v1/pagos/multas",
                        "/api/v1/pagos/liquidaciones/**").uri(pagos))
                .route("tramites", r -> r.path(
                        "/api/v1/tramites/licencias-funcionamiento",
                        "/api/v1/tramites/licencias-construccion",
                        "/api/v1/tramites/folio/**",
                        "/api/v1/tramites/mios",
                        "/api/v1/tramites/bandeja").uri(tramites))
                .route("documentos", r -> r.path(
                        "/api/v1/documentos/verificar/**",
                        "/api/v1/documentos/*",
                        "/api/v1/documentos/*/pdf").uri(documentos))
                .route("ciudad", r -> r.path(
                        "/api/v1/ciudad/perfiles/**",
                        "/api/v1/ciudad/foros/**",
                        "/api/v1/ciudad/encuestas/**",
                        "/api/v1/ciudad/pymes",
                        "/api/v1/ciudad/pymes/**",
                        "/api/v1/ciudad/salas/**",
                        "/api/v1/ciudad/alertas/**",
                        "/api/v1/ciudad/eventos/**").uri(ciudad))
                .route("incidencias", r -> r.path("/api/v1/incidencias/**").uri(incidencias))
                .build();
    }
}
