package mx.gob.gld.pagos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.pagos.application.PagosService;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaInternaRequest;
import mx.gob.gld.pagos.web.PagosDtos.LiquidacionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos/interno")
@Tag(name = "Pagos interno", description = "HU-03 Liquidación de licencia tras el dictamen")
public class PagosInternoController {

    private final PagosService pagos;

    public PagosInternoController(PagosService pagos) {
        this.pagos = pagos;
    }

    @PostMapping("/liquidaciones/licencia")
    @Operation(summary = "HU-03 Crear la liquidación de una licencia aprobada")
    public LiquidacionResponse licencia(@RequestBody LicenciaInternaRequest request) {
        return pagos.crearLicencia(request);
    }
}
