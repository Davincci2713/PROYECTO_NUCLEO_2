package mx.gob.gld.catastro.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.catastro.application.CatastroService;
import mx.gob.gld.catastro.web.CatastroDtos.PredioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/predios")
@Tag(name = "Predial", description = "HU-01 Consulta del padrón catastral")
public class PredioController {

    private final CatastroService catastro;

    public PredioController(CatastroService catastro) {
        this.catastro = catastro;
    }

    @GetMapping("/{clave}")
    @Operation(summary = "HU-01 Consultar un predio y sus periodos")
    public PredioResponse predio(@PathVariable String clave) {
        return catastro.predio(clave);
    }
}
