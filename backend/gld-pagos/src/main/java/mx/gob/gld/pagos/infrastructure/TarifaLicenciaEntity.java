package mx.gob.gld.pagos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "pagos", name = "tarifas_licencia")
public class TarifaLicenciaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "giro", nullable = false, length = 40)
    public String giro;

    @Column(name = "tarifa_base", nullable = false)
    public BigDecimal tarifaBase;

    @Column(name = "factor_m2", nullable = false)
    public BigDecimal factorM2;
}
