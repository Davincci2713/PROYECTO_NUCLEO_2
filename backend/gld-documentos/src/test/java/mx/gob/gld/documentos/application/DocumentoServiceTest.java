package mx.gob.gld.documentos.application;

import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.security.GldProperties;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.documentos.domain.Sello;
import mx.gob.gld.documentos.infrastructure.DocumentoEntity;
import mx.gob.gld.documentos.infrastructure.DocumentoRepository;
import mx.gob.gld.documentos.web.DocumentoDtos.DocumentoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitidoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitirRequest;
import mx.gob.gld.documentos.web.DocumentoDtos.VerificacionResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentoServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-08T15:00:00Z");

    @Mock DocumentoRepository documentos;

    private DocumentoService servicio;
    private GldProperties properties;

    @BeforeEach
    void prepara() {
        properties = new GldProperties();
        properties.setSelloSecret("gld-sello-demo");
        properties.setVerificacionBase("http://localhost:3000/verificar/");
        servicio = new DocumentoService(documentos, properties, Clock.fixed(AHORA, ZoneOffset.UTC));
        TenantContext.set("05021");
        when(documentos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentos.findByTenantIdAndFolio(any(), any())).thenReturn(Optional.empty());
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
    }

    @Test
    void emitirSellaConHmacYReusaElFolio() {
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.emitir(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.emitir(pedido(" ", "Ana", "Predial", new BigDecimal("10"), "RECIBO_PAGO"))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.emitir(pedido("PAG-2026-000001", " ", "Predial", new BigDecimal("10"), "RECIBO_PAGO"))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.emitir(pedido("PAG-2026-000001", "Ana", " ", new BigDecimal("10"), "RECIBO_PAGO"))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.emitir(pedido("PAG-2026-000001", "Ana", "Predial", null, "RECIBO_PAGO"))));

        EmitidoResponse emitido = servicio.emitir(pedido(" PAG-2026-000001 ", "María López", "Impuesto predial", new BigDecimal("3116.4"), " "));
        assertEquals("PAG-2026-000001", emitido.folio());
        DocumentoEntity guardado = org.mockito.Mockito.mockingDetails(documentos).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .map(invocation -> (DocumentoEntity) invocation.getArgument(0))
                .findFirst().orElseThrow();
        assertEquals(Sello.hex("PAG-2026-000001", new BigDecimal("3116.40"), "Impuesto predial", "María López", "gld-sello-demo"), guardado.sello);
        assertEquals("http://localhost:3000/verificar/PAG-2026-000001", guardado.qrPayload);
        assertEquals("RECIBO_PAGO", guardado.tipo);
        assertEquals('%', guardado.pdf[0]);
        assertEquals(AHORA, guardado.emitidoEn);

        when(documentos.findByTenantIdAndFolio("05021", "PAG-2026-000001")).thenReturn(Optional.of(guardado));
        EmitidoResponse repetido = servicio.emitir(pedido("PAG-2026-000001", "María López", "Impuesto predial", new BigDecimal("3116.40"), "LICENCIA"));
        assertEquals(guardado.id, repetido.id());

        properties.setVerificacionBase("http://localhost:3000/verificar");
        when(documentos.findByTenantIdAndFolio("05021", "PAG-2026-000002")).thenReturn(Optional.empty());
        servicio.emitir(pedido("PAG-2026-000002", "Ana", "Agua\n😀", new BigDecimal("10"), "RECIBO"));
    }

    @Test
    void consultarPdfYVerificar() {
        DocumentoEntity doc = new DocumentoEntity();
        doc.id = UUID.randomUUID();
        doc.folio = "PAG-2026-000001";
        doc.tipo = "RECIBO_PAGO";
        doc.transaccionId = UUID.randomUUID();
        doc.titular = "Ana";
        doc.concepto = "Predial";
        doc.monto = new BigDecimal("10");
        doc.sello = "abc";
        doc.qrPayload = "http://localhost/verificar/PAG-2026-000001";
        doc.emitidoEn = AHORA;
        doc.pdf = new byte[] {1, 2, 3};
        when(documentos.findByTenantIdAndId("05021", doc.id)).thenReturn(Optional.of(doc));
        DocumentoResponse meta = servicio.consultar(doc.id);
        assertEquals("abc", meta.sello());
        assertEquals(new BigDecimal("10.00"), meta.monto());
        assertArrayEquals(doc.pdf, servicio.pdf(doc.id));
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.consultar(UUID.randomUUID())));

        when(documentos.findByTenantIdAndFolio("05021", "PAG-2026-000001")).thenReturn(Optional.of(doc));
        VerificacionResponse valida = servicio.verificar("PAG-2026-000001");
        assertTrue(valida.valido());
        assertEquals("Ana", valida.titular());
        VerificacionResponse ausente = servicio.verificar("NO-EXISTE");
        assertFalse(ausente.valido());
        assertEquals("NO-EXISTE", ausente.folio());
    }

    private static String codigo(Runnable accion) {
        return assertThrows(ApiException.class, accion::run).getCodigo();
    }

    private static EmitirRequest pedido(String folio, String titular, String concepto, BigDecimal monto, String tipo) {
        return new EmitirRequest(folio, tipo, UUID.randomUUID(), null, titular, concepto, monto);
    }
}
