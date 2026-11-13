package mx.gob.gld.tramites.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.tramites.application.TramitesService;
import mx.gob.gld.tramites.web.TramitesDtos.PagadoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tramites/interno")
@Tag(name = "Trámites interno", description = "HU-03 Paso a PAGADO tras el cobro")
public class TramitesInternoController {

    private final TramitesService tramites;

    public TramitesInternoController(TramitesService tramites) {
        this.tramites = tramites;
    }

    @PostMapping("/pagado")
    @Operation(summary = "HU-03 Marcar la licencia como pagada")
    public ResponseEntity<Void> pagado(@RequestBody PagadoRequest request) {
        tramites.marcarPagado(request);
        return ResponseEntity.noContent().build();
    }
}
