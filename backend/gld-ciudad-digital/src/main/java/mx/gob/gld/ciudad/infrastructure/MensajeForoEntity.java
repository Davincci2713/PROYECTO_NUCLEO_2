package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "mensajes_foro")
public class MensajeForoEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "foro_id", nullable = false)
    public UUID foroId;

    @Column(name = "autor_id", nullable = false)
    public UUID autorId;

    @Column(name = "autor_nombre", nullable = false, length = 160)
    public String autorNombre;

    @Column(name = "cuerpo", nullable = false, length = 2000)
    public String cuerpo;

    @Column(name = "es_oficial", nullable = false)
    public boolean esOficial;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
