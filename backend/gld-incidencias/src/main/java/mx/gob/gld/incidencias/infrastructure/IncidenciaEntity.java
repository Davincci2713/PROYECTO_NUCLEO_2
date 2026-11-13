package mx.gob.gld.incidencias.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "incidencias", name = "incidencias")
public class IncidenciaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "folio", nullable = false, length = 32)
    public String folio;

    @Column(name = "tipo", nullable = false, length = 24)
    public String tipo;

    @Column(name = "estado", nullable = false, length = 24)
    public String estado;

    @Column(name = "ciudadano_id", nullable = false)
    public UUID ciudadanoId;

    @Column(name = "lat", nullable = false)
    public BigDecimal lat;

    @Column(name = "lng", nullable = false)
    public BigDecimal lng;

    @Column(name = "direccion_referencia", nullable = false, length = 240)
    public String direccionReferencia;

    @Column(name = "descripcion", nullable = false, length = 800)
    public String descripcion;

    @Column(name = "foto")
    public byte[] foto;

    @Column(name = "foto_content_type", length = 80)
    public String fotoContentType;

    @Column(name = "brigada_id", nullable = false)
    public UUID brigadaId;

    @Column(name = "creada_en", nullable = false)
    public Instant creadaEn;
}
