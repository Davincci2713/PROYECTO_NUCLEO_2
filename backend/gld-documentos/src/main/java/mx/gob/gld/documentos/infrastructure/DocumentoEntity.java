package mx.gob.gld.documentos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "documentos", name = "documentos_oficiales")
public class DocumentoEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "folio", nullable = false, length = 32)
    public String folio;

    @Column(name = "tipo", nullable = false, length = 40)
    public String tipo;

    @Column(name = "transaccion_id")
    public UUID transaccionId;

    @Column(name = "tramite_id")
    public UUID tramiteId;

    @Column(name = "titular", nullable = false, length = 160)
    public String titular;

    @Column(name = "concepto", nullable = false, length = 240)
    public String concepto;

    @Column(name = "monto", nullable = false)
    public BigDecimal monto;

    @Column(name = "sello", nullable = false, length = 128)
    public String sello;

    @Column(name = "qr_payload", nullable = false, length = 300)
    public String qrPayload;

    @Column(name = "emitido_en", nullable = false)
    public Instant emitidoEn;

    @Column(name = "pdf", nullable = false)
    public byte[] pdf;
}
