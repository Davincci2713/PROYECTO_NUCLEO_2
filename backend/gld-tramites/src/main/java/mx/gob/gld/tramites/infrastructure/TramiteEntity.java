package mx.gob.gld.tramites.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "tramites", name = "tramites")
public class TramiteEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "folio", nullable = false, length = 32)
    public String folio;

    @Column(name = "tipo", nullable = false, length = 40)
    public String tipo;

    @Column(name = "estado", nullable = false, length = 32)
    public String estado;

    @Column(name = "ciudadano_id", nullable = false)
    public UUID ciudadanoId;

    @Column(name = "titulo", nullable = false, length = 200)
    public String titulo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    public String payload;

    @Column(name = "liquidacion_id")
    public UUID liquidacionId;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    public Instant actualizadoEn;
}
