package mx.gob.gld.catastro.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.catastro.application.CatastroService;
import mx.gob.gld.catastro.web.CatastroDtos.ContratoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agua/contratos")
@Tag(name = "Agua", description = "HU-02 Consulta de contratos de agua")
public class AguaController {

    private final CatastroService catastro;

    public AguaController(CatastroService catastro) {
        this.catastro = catastro;
    }

    @GetMapping
    @Operation(summary = "HU-02 Listar contratos de agua del municipio")
    public List<ContratoResponse> listar() {
        return catastro.contratos();
    }

    @GetMapping("/{numero}")
    @Operation(summary = "HU-02 Consultar el estado de cuenta de un contrato")
    public ContratoResponse uno(@PathVariable String numero) {
        return catastro.contrato(numero);
    }
}
