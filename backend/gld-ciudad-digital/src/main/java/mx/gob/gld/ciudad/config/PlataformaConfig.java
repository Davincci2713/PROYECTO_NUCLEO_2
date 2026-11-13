package mx.gob.gld.ciudad.config;

import mx.gob.gld.common.bitacora.BitacoraRepository;
import mx.gob.gld.common.bitacora.BitacoraService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlataformaConfig {

    @Bean
    BitacoraService bitacoraService(BitacoraRepository repository) {
        return new BitacoraService(repository);
    }
}
