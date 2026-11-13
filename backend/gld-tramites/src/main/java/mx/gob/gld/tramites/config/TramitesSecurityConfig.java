package mx.gob.gld.tramites.config;

import mx.gob.gld.common.security.GldHttpSecurity;
import mx.gob.gld.common.security.GldJwtService;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.Ruta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TramitesSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, GldJwtService jwt, GldProperties properties) throws Exception {
        return GldHttpSecurity.build(
                http,
                jwt,
                properties.getInternalToken(),
                Ruta.ninguna(),
                new Ruta(null, "/api/v1/tramites/interno/**"),
                Ruta.ninguna());
    }
}
