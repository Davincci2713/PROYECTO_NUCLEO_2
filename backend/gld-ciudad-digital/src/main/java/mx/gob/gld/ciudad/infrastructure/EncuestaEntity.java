package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "encuestas")
public class EncuestaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "pregunta", nullable = false, length = 300)
    public String pregunta;

    @Column(name = "cierra_en", nullable = false)
    public Instant cierraEn;

    @Column(name = "creador_id", nullable = false)
    public UUID creadorId;
}
