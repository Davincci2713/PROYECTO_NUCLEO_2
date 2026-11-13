package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "votos")
public class VotoEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "encuesta_id", nullable = false)
    public UUID encuestaId;

    @Column(name = "opcion_id", nullable = false)
    public UUID opcionId;

    @Column(name = "usuario_id", nullable = false)
    public UUID usuarioId;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
