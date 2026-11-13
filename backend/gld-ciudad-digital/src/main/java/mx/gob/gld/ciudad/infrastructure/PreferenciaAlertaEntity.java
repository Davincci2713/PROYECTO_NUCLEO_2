package mx.gob.gld.ciudad.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(schema = "ciudad", name = "preferencias_alerta")
public class PreferenciaAlertaEntity {

    @Id
    @Column(name = "usuario_id")
    public UUID usuarioId;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "telefono", nullable = false, length = 20)
    public String telefono;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "temas", nullable = false, columnDefinition = "text[]")
    public String[] temas;
}
