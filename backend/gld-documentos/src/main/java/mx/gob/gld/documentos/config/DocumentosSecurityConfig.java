package mx.gob.gld.documentos.config;

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
public class DocumentosSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, GldJwtService jwt, GldProperties properties) throws Exception {
        return GldHttpSecurity.build(
                http,
                jwt,
                properties.getInternalToken(),
                new Ruta("GET", "/api/v1/documentos/verificar/**"),
                new Ruta(null, "/api/v1/documentos/interno/**"),
                Ruta.ninguna());
    }
}
