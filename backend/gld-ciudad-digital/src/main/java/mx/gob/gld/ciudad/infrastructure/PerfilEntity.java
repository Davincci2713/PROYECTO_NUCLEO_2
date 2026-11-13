package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "perfiles")
public class PerfilEntity {

    @Id
    @Column(name = "usuario_id")
    public UUID usuarioId;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "nombre", nullable = false, length = 160)
    public String nombre;

    @Column(name = "biografia", nullable = false, length = 500)
    public String biografia;

    @Column(name = "colonia", nullable = false, length = 120)
    public String colonia;
}
