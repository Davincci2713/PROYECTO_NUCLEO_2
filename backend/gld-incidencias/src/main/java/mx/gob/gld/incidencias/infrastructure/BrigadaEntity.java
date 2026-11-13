package mx.gob.gld.incidencias.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(schema = "incidencias", name = "brigadas")
public class BrigadaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "tipo", nullable = false, length = 24)
    public String tipo;

    @Column(name = "nombre", nullable = false, length = 120)
    public String nombre;

    @Column(name = "zona", nullable = false, length = 80)
    public String zona;
}
