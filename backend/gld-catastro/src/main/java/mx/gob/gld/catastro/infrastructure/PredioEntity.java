package mx.gob.gld.catastro.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "catastro", name = "predios")
public class PredioEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "clave_catastral", nullable = false, length = 20)
    public String claveCatastral;

    @Column(name = "propietario", nullable = false, length = 160)
    public String propietario;

    @Column(name = "direccion", nullable = false, length = 240)
    public String direccion;

    @Column(name = "superficie_m2", nullable = false)
    public BigDecimal superficieM2;
}
