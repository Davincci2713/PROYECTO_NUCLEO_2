package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "pymes")
public class PymeEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "dueno_id", nullable = false)
    public UUID duenoId;

    @Column(name = "nombre", nullable = false, length = 160)
    public String nombre;

    @Column(name = "giro", nullable = false, length = 80)
    public String giro;

    @Column(name = "descripcion", nullable = false, length = 500)
    public String descripcion;

    @Column(name = "telefono", nullable = false, length = 20)
    public String telefono;

    @Column(name = "colonia", nullable = false, length = 120)
    public String colonia;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
