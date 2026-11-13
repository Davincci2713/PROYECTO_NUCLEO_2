package mx.gob.gld.pagos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "pagos", name = "multas")
public class MultaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "folio", nullable = false, length = 32)
    public String folio;

    @Column(name = "placa", nullable = false, length = 16)
    public String placa;

    @Column(name = "descripcion", nullable = false, length = 240)
    public String descripcion;

    @Column(name = "importe", nullable = false)
    public BigDecimal importe;

    @Column(name = "fecha_infraccion", nullable = false)
    public LocalDate fechaInfraccion;

    @Column(name = "pagada", nullable = false)
    public boolean pagada;
}
