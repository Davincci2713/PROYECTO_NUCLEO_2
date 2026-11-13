package mx.gob.gld.tramites.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "tramites", name = "historial")
public class HistorialEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tramite_id", nullable = false)
    public UUID tramiteId;

    @Column(name = "estado", nullable = false, length = 32)
    public String estado;

    @Column(name = "actor", nullable = false, length = 160)
    public String actor;

    @Column(name = "nota", nullable = false, length = 500)
    public String nota;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
