package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "inscripciones")
public class InscripcionEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "evento_id", nullable = false)
    public UUID eventoId;

    @Column(name = "usuario_id", nullable = false)
    public UUID usuarioId;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
