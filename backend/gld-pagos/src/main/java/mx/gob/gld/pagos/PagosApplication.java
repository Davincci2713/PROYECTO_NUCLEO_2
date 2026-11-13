package mx.gob.gld.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"mx.gob.gld.pagos", "mx.gob.gld.common.bitacora"})
@EnableJpaRepositories(basePackages = {"mx.gob.gld.pagos", "mx.gob.gld.common.bitacora"})
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }
}
