package mx.gob.gld.common;

import mx.gob.gld.common.error.ApiExceptionHandler;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldJwtService;
import mx.gob.gld.common.security.GldProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@AutoConfiguration
@EnableConfigurationProperties(GldProperties.class)
@Import(ApiExceptionHandler.class)
public class GldCommonAutoConfiguration {

    @Bean
    GldJwtService gldJwtService(GldProperties properties) {
        return new GldJwtService(properties);
    }

    @Bean
    Secuencias secuencias(JdbcTemplate jdbcTemplate) {
        return new Secuencias(jdbcTemplate);
    }

    @Bean
    RestClient gldRestClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    Clock gldClock() {
        return Clock.systemUTC();
    }
}
