package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "eventos")
public class EventoEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "titulo", nullable = false, length = 180)
    public String titulo;

    @Column(name = "descripcion", nullable = false, length = 800)
    public String descripcion;

    @Column(name = "lugar", nullable = false, length = 180)
    public String lugar;

    @Column(name = "inicia_en", nullable = false)
    public Instant iniciaEn;

    @Column(name = "creador_id", nullable = false)
    public UUID creadorId;
}
