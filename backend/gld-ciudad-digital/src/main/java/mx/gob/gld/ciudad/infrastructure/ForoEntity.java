package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "foros")
public class ForoEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "autor_id", nullable = false)
    public UUID autorId;

    @Column(name = "titulo", nullable = false, length = 180)
    public String titulo;

    @Column(name = "cuerpo", nullable = false, length = 2000)
    public String cuerpo;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
