package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "alertas_enviadas")
public class AlertaEnviadaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "usuario_id", nullable = false)
    public UUID usuarioId;

    @Column(name = "telefono", nullable = false, length = 20)
    public String telefono;

    @Column(name = "tema", nullable = false, length = 40)
    public String tema;

    @Column(name = "mensaje", nullable = false, length = 320)
    public String mensaje;

    @Column(name = "enviada_en", nullable = false)
    public Instant enviadaEn;
}
