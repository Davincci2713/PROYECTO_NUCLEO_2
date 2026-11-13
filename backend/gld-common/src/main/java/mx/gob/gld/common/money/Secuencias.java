package mx.gob.gld.common.money;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Year;
import java.time.ZoneOffset;

public class Secuencias {

    private final JdbcTemplate jdbc;

    public Secuencias(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long next(String secuencia) {
        if (!secuencia.matches("[a-z]+\\.folio_seq")) {
            throw new IllegalArgumentException(secuencia);
        }
        Long valor = jdbc.queryForObject("select nextval(cast(? as regclass))", Long.class, secuencia);
        if (valor == null) {
            throw new IllegalStateException(secuencia);
        }
        return valor;
    }

    public String folio(String prefijo, String secuencia) {
        int anio = Year.now(ZoneOffset.UTC).getValue();
        return Folios.formato(prefijo, anio, next(secuencia));
    }
}
