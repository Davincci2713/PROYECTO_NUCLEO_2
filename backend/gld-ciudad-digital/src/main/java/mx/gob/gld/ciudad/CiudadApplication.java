package mx.gob.gld.ciudad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"mx.gob.gld.ciudad", "mx.gob.gld.common.bitacora"})
@EnableJpaRepositories(basePackages = {"mx.gob.gld.ciudad", "mx.gob.gld.common.bitacora"})
public class CiudadApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiudadApplication.class, args);
    }
}
