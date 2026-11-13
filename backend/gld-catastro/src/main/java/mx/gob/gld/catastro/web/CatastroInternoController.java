package mx.gob.gld.catastro.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.catastro.application.CatastroService;
import mx.gob.gld.catastro.web.CatastroDtos.MarcarAguaRequest;
import mx.gob.gld.catastro.web.CatastroDtos.MarcarPredialRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catastro interno", description = "HU-01 y HU-02 Marcado de pagos")
public class CatastroInternoController {

    private final CatastroService catastro;

    public CatastroInternoController(CatastroService catastro) {
        this.catastro = catastro;
    }

    @PostMapping("/predios/interno/pagado")
    @Operation(summary = "HU-01 Marcar periodos prediales como pagados")
    public ResponseEntity<Void> predial(@RequestBody MarcarPredialRequest request) {
        catastro.marcarPredialPagado(request.claveCatastral(), request.periodos());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/agua/interno/pagado")
    @Operation(summary = "HU-02 Marcar el contrato de agua como pagado")
    public ResponseEntity<Void> agua(@RequestBody MarcarAguaRequest request) {
        catastro.marcarAguaPagado(request.numeroContrato());
        return ResponseEntity.noContent().build();
    }
}
