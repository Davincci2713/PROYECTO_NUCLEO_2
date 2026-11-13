package mx.gob.gld.documentos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.documentos.application.DocumentoService;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitidoResponse;
import mx.gob.gld.documentos.web.DocumentoDtos.EmitirRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documentos/interno")
@Tag(name = "Documentos interno", description = "HU-07 Emisión del recibo al autorizar el pago")
public class DocumentoInternoController {

    private final DocumentoService documentos;

    public DocumentoInternoController(DocumentoService documentos) {
        this.documentos = documentos;
    }

    @PostMapping("/emitir")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-07 Emitir el recibo con el folio de pago")
    public EmitidoResponse emitir(@RequestBody EmitirRequest request) {
        return documentos.emitir(request);
    }
}
