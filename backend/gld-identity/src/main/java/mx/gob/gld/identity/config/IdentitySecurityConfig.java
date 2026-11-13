package mx.gob.gld.identity.config;

import mx.gob.gld.common.security.GldHttpSecurity;
import mx.gob.gld.common.security.GldJwtService;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.Ruta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class IdentitySecurityConfig {

    @Bean
    SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                    GldJwtService jwt,
                                    GldProperties properties) throws Exception {
        RequestMatcher publicos = Ruta.alguna(
                new Ruta("POST", "/api/v1/auth/registro"),
                new Ruta("POST", "/api/v1/auth/verificar-correo"),
                new Ruta("POST", "/api/v1/auth/login"),
                new Ruta("POST", "/api/v1/auth/2fa/verificar"));
        RequestMatcher internos = new Ruta(null, "/api/v1/auth/interno/**");
        RequestMatcher preToken = new Ruta("POST", "/api/v1/auth/2fa/verificar");
        return GldHttpSecurity.build(http, jwt, properties.getInternalToken(), publicos, internos, preToken);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
