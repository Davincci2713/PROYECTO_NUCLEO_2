package mx.gob.gld.catastro.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "catastro", name = "periodos_predial")
public class PeriodoPredialEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "predio_id", nullable = false)
    public UUID predioId;

    @Column(name = "periodo", nullable = false, length = 16)
    public String periodo;

    @Column(name = "importe_base", nullable = false)
    public BigDecimal importeBase;

    @Column(name = "recargo_mora", nullable = false)
    public BigDecimal recargoMora;

    @Column(name = "fecha_vencimiento", nullable = false)
    public LocalDate fechaVencimiento;

    @Column(name = "pagado", nullable = false)
    public boolean pagado;
}
