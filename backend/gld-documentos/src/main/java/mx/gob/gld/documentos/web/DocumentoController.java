package mx.gob.gld.documentos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.documentos.application.DocumentoService;
import mx.gob.gld.documentos.web.DocumentoDtos.DocumentoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.VerificacionResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documentos")
@Tag(name = "Documentos", description = "HU-07 Recibo, sello y QR")
public class DocumentoController {

    private final DocumentoService documentos;

    public DocumentoController(DocumentoService documentos) {
        this.documentos = documentos;
    }

    @GetMapping("/verificar/{folio}")
    @Operation(summary = "HU-07 Verificar un folio de forma pública")
    public VerificacionResponse verificar(@PathVariable String folio) {
        return documentos.verificar(folio);
    }

    @GetMapping("/{id}")
    @Operation(summary = "HU-07 Consultar los metadatos del recibo")
    public DocumentoResponse consultar(@PathVariable UUID id) {
        return documentos.consultar(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "HU-07 Descargar el PDF con sello y QR")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(documentos.pdf(id));
    }
}
