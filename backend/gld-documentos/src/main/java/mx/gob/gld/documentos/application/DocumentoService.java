package mx.gob.gld.documentos.application;

import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.money.Dinero;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.documentos.domain.ReciboPdf;
import mx.gob.gld.documentos.domain.Sello;
import mx.gob.gld.documentos.infrastructure.DocumentoEntity;
import mx.gob.gld.documentos.infrastructure.DocumentoRepository;
import mx.gob.gld.documentos.web.DocumentoDtos.DocumentoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitidoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitirRequest;
import mx.gob.gld.documentos.web.DocumentoDtos.VerificacionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentos;
    private final GldProperties properties;
    private final Clock clock;

    public DocumentoService(DocumentoRepository documentos, GldProperties properties, Clock clock) {
        this.documentos = documentos;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public EmitidoResponse emitir(EmitirRequest request) {
        if (request == null || request.folio() == null || request.folio().isBlank()) {
            throw Errores.datoInvalido("folio", "PAG-2026-000001",
                    "El folio del pago es obligatorio. Ejemplo: PAG-2026-000001");
        }
        if (request.titular() == null || request.titular().isBlank()
                || request.concepto() == null || request.concepto().isBlank()
                || request.monto() == null) {
            throw Errores.datoInvalido("concepto", "Impuesto predial",
                    "El recibo necesita titular, concepto y monto. Ejemplo: Impuesto predial");
        }
        String tenant = TenantContext.exigir();
        DocumentoEntity previo = documentos.findByTenantIdAndFolio(tenant, request.folio().trim()).orElse(null);
        if (previo != null) {
            return new EmitidoResponse(previo.id, previo.folio);
        }
        String folio = request.folio().trim();
        String base = properties.getVerificacionBase();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String qr = base + "/" + folio;
        String sello = Sello.hex(folio, request.monto(), request.concepto(), request.titular(), properties.getSelloSecret());
        DocumentoEntity entidad = new DocumentoEntity();
        entidad.id = UUID.randomUUID();
        entidad.tenantId = tenant;
        entidad.folio = folio;
        entidad.tipo = request.tipo() == null || request.tipo().isBlank() ? "RECIBO_PAGO" : request.tipo();
        entidad.transaccionId = request.transaccionId();
        entidad.tramiteId = request.tramiteId();
        entidad.titular = request.titular();
        entidad.concepto = request.concepto();
        entidad.monto = Dinero.dos(request.monto());
        entidad.sello = sello;
        entidad.qrPayload = qr;
        entidad.emitidoEn = clock.instant();
        entidad.pdf = ReciboPdf.generar(folio, request.transaccionId(), request.titular(),
                request.concepto(), entidad.monto, sello, qr);
        documentos.save(entidad);
        return new EmitidoResponse(entidad.id, entidad.folio);
    }

    @Transactional(readOnly = true)
    public DocumentoResponse consultar(UUID id) {
        return metadatos(buscar(id));
    }

    @Transactional(readOnly = true)
    public byte[] pdf(UUID id) {
        return buscar(id).pdf;
    }

    @Transactional(readOnly = true)
    public VerificacionResponse verificar(String folio) {
        return documentos.findByTenantIdAndFolio(TenantContext.exigir(), folio)
                .map(doc -> new VerificacionResponse(true, doc.folio, doc.concepto, Dinero.dos(doc.monto),
                        doc.titular, doc.emitidoEn, doc.sello))
                .orElseGet(() -> new VerificacionResponse(false, folio, null, null, null, null, null));
    }

    private DocumentoEntity buscar(UUID id) {
        return documentos.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(() -> Errores.noEncontrado("No hay un documento con ese identificador."));
    }

    private static DocumentoResponse metadatos(DocumentoEntity doc) {
        return new DocumentoResponse(
                doc.id, doc.folio, doc.tipo, doc.transaccionId, doc.titular, doc.concepto,
                Dinero.dos(doc.monto), doc.sello, doc.qrPayload, doc.emitidoEn);
    }
}
