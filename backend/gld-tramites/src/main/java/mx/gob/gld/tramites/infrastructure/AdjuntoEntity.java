package mx.gob.gld.tramites.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "tramites", name = "adjuntos")
public class AdjuntoEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tramite_id", nullable = false)
    public UUID tramiteId;

    @Column(name = "nombre", nullable = false, length = 180)
    public String nombre;

    @Column(name = "content_type", nullable = false, length = 80)
    public String contentType;

    @Column(name = "contenido", nullable = false)
    public byte[] contenido;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
