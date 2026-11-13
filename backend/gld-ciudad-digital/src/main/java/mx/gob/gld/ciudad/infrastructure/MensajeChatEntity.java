package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "mensajes_chat")
public class MensajeChatEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "sala_id", nullable = false)
    public UUID salaId;

    @Column(name = "autor_id", nullable = false)
    public UUID autorId;

    @Column(name = "autor_nombre", nullable = false, length = 160)
    public String autorNombre;

    @Column(name = "texto", nullable = false, length = 500)
    public String texto;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
