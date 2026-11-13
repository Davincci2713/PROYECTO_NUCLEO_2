package mx.gob.gld.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"mx.gob.gld.tramites", "mx.gob.gld.common.bitacora"})
@EnableJpaRepositories(basePackages = {"mx.gob.gld.tramites", "mx.gob.gld.common.bitacora"})
public class TramitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TramitesApplication.class, args);
    }
}
