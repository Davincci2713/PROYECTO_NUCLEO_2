package mx.gob.gld.pagos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "pagos", name = "liquidaciones")
public class LiquidacionEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "tipo", nullable = false, length = 24)
    public String tipo;

    @Column(name = "referencia", nullable = false, length = 120)
    public String referencia;

    @Column(name = "concepto", nullable = false, length = 240)
    public String concepto;

    @Column(name = "subtotal", nullable = false)
    public BigDecimal subtotal;

    @Column(name = "recargos", nullable = false)
    public BigDecimal recargos;

    @Column(name = "descuento", nullable = false)
    public BigDecimal descuento;

    @Column(name = "total", nullable = false)
    public BigDecimal total;

    @Column(name = "estado", nullable = false, length = 16)
    public String estado;

    @Column(name = "ciudadano_id")
    public UUID ciudadanoId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "desglose", nullable = false)
    public String desglose;

    @Column(name = "creada_en", nullable = false)
    public Instant creadaEn;

    @Column(name = "vence_en", nullable = false)
    public Instant venceEn;

    @Column(name = "folio_pago", length = 32)
    public String folioPago;
}
