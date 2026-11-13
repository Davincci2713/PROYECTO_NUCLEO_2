package mx.gob.gld.common.bitacora;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bitacora")
public class BitacoraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "ocurrio_en", nullable = false)
    public Instant ocurrioEn;

    @Column(name = "actor_id")
    public UUID actorId;

    @Column(name = "accion", nullable = false, length = 80)
    public String accion;

    @Column(name = "recurso", nullable = false, length = 80)
    public String recurso;

    @Column(name = "referencia", length = 80)
    public String referencia;

    @Column(name = "detalle", nullable = false, length = 500)
    public String detalle;
}
