package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "opciones_encuesta")
public class OpcionEncuestaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "encuesta_id", nullable = false)
    public UUID encuestaId;

    @Column(name = "texto", nullable = false, length = 180)
    public String texto;
}
