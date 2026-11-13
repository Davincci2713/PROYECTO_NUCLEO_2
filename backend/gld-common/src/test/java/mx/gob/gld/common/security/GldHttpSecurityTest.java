package mx.gob.gld.common.security;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(GldHttpSecurityTest.Cfg.class)
class GldHttpSecurityTest {

    @Autowired
    SecurityFilterChain cadena;

    @Test
    void laCadenaRechazaSinTokenYDejaPasarLaSalud() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new Sonda())
                .addFilters(cadena.getFilters().toArray(Filter[]::new))
                .build();
        mockMvc.perform(get("/api/v1/privado"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Configuration
    @EnableWebSecurity
    static class Cfg {
        @Bean
        SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http)
                throws Exception {
            GldProperties properties = new GldProperties();
            properties.getJwt().setSecret("gld-dev-secret-cambia-en-produccion-32b");
            return GldHttpSecurity.build(
                    http,
                    new GldJwtService(properties),
                    "gld-dev-internal",
                    Ruta.ninguna(),
                    new Ruta(null, "/api/v1/interno/**"),
                    Ruta.ninguna());
        }
    }

    @RestController
    static class Sonda {
        @GetMapping({"/api/v1/privado", "/actuator/health"})
        String ok() {
            return "ok";
        }
    }
}
