package mx.gob.gld.catastro.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "catastro", name = "contratos_agua")
public class ContratoAguaEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "numero_contrato", nullable = false, length = 32)
    public String numeroContrato;

    @Column(name = "titular", nullable = false, length = 160)
    public String titular;

    @Column(name = "direccion", nullable = false, length = 240)
    public String direccion;

    @Column(name = "periodo", nullable = false, length = 16)
    public String periodo;

    @Column(name = "lectura_anterior", nullable = false)
    public int lecturaAnterior;

    @Column(name = "lectura_actual", nullable = false)
    public int lecturaActual;

    @Column(name = "tarifa_m3", nullable = false)
    public BigDecimal tarifaM3;

    @Column(name = "cuota_fija", nullable = false)
    public BigDecimal cuotaFija;

    @Column(name = "pagado", nullable = false)
    public boolean pagado;
}
