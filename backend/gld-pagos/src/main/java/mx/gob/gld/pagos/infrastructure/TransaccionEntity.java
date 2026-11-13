package mx.gob.gld.pagos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "pagos", name = "transacciones")
public class TransaccionEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "liquidacion_id", nullable = false)
    public UUID liquidacionId;

    @Column(name = "medio", nullable = false, length = 16)
    public String medio;

    @Column(name = "estado", nullable = false, length = 16)
    public String estado;

    @Column(name = "monto", nullable = false)
    public BigDecimal monto;

    @Column(name = "concepto", nullable = false, length = 240)
    public String concepto;

    @Column(name = "motivo_rechazo", length = 240)
    public String motivoRechazo;

    @Column(name = "creada_en", nullable = false)
    public Instant creadaEn;
}
