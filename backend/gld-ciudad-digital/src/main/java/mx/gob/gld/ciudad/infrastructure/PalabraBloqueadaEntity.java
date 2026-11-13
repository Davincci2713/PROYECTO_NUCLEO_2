package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "ciudad", name = "palabras_bloqueadas")
public class PalabraBloqueadaEntity {

    @Id
    @Column(name = "palabra", length = 40)
    public String palabra;
}
