package mx.gob.gld.pagos.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.pagos.application.PagosService;
import mx.gob.gld.pagos.web.PagosDtos.AguaRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroResponse;
import mx.gob.gld.pagos.web.PagosDtos.CotizacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.GiroResponse;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaCotizacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.LiquidacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.MultaLiquidacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.MultasResponse;
import mx.gob.gld.pagos.web.PagosDtos.PredialRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "HU-01 a HU-04 y HU-08 Liquidaciones y cobro")
public class PagosController {

    private final PagosService pagos;

    public PagosController(PagosService pagos) {
        this.pagos = pagos;
    }

    @GetMapping("/cotizaciones/giros")
    @Operation(summary = "HU-03 Catálogo de giros de licencia")
    public List<GiroResponse> giros() {
        return pagos.giros();
    }

    @PostMapping("/cotizaciones/licencia")
    @Operation(summary = "HU-03 Cotizar una licencia de funcionamiento")
    public CotizacionResponse cotizar(@RequestBody LicenciaCotizacionRequest request) {
        return pagos.cotizar(request);
    }

    @GetMapping("/multas")
    @Operation(summary = "HU-04 Buscar multas por placa o folio")
    public MultasResponse multas(@RequestParam(required = false) String placa,
                                 @RequestParam(required = false) String folio) {
        return pagos.buscarMultas(placa, folio);
    }

    @PostMapping("/liquidaciones/predial")
    @Operation(summary = "HU-01 Liquidar periodos de predial con 2 % en línea")
    public LiquidacionResponse predial(@RequestBody PredialRequest request) {
        return pagos.liquidarPredial(request);
    }

    @PostMapping("/liquidaciones/agua")
    @Operation(summary = "HU-02 Liquidar el saldo de agua")
    public LiquidacionResponse agua(@RequestBody AguaRequest request) {
        return pagos.liquidarAgua(request);
    }

    @PostMapping("/liquidaciones/multa")
    @Operation(summary = "HU-04 Liquidar una multa con pronto pago si aplica")
    public LiquidacionResponse multa(@RequestBody MultaLiquidacionRequest request) {
        return pagos.liquidarMulta(request);
    }

    @GetMapping("/liquidaciones/{id}")
    @Operation(summary = "RF-07 Consultar una liquidación y expirarla a las 72 horas")
    public LiquidacionResponse consultar(@PathVariable UUID id) {
        return pagos.consultar(id);
    }

    @PostMapping("/liquidaciones/{id}/cobrar")
    @Operation(summary = "HU-08 Cobrar con medio simulado TARJETA o PSE")
    public CobroResponse cobrar(@PathVariable UUID id, @RequestBody CobroRequest request) {
        return pagos.cobrar(id, request);
    }
}
