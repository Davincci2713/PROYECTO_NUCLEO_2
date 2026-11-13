package mx.gob.gld.tramites.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.tramites.application.TramitesService;
import mx.gob.gld.tramites.web.TramitesDtos.DictamenRequest;
import mx.gob.gld.tramites.web.TramitesDtos.LicenciaConstruccionRequest;
import mx.gob.gld.tramites.web.TramitesDtos.LicenciaFuncionamientoRequest;
import mx.gob.gld.tramites.web.TramitesDtos.SubsaniarRequest;
import mx.gob.gld.tramites.web.TramitesDtos.TramiteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tramites")
@Tag(name = "Trámites", description = "HU-03, HU-05, HU-06 y subsanación")
public class TramitesController {

    private final TramitesService tramites;

    public TramitesController(TramitesService tramites) {
        this.tramites = tramites;
    }

    @PostMapping("/licencias-funcionamiento")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-03 Solicitar licencia de funcionamiento")
    public TramiteResponse funcionamiento(@RequestBody LicenciaFuncionamientoRequest request) {
        return tramites.licenciaFuncionamiento(request);
    }

    @PostMapping("/licencias-construccion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-05 Radicar licencia de construcción")
    public TramiteResponse construccion(@RequestBody LicenciaConstruccionRequest request) {
        return tramites.licenciaConstruccion(request);
    }

    @GetMapping("/folio/{folio}")
    @Operation(summary = "HU-06 Consultar un trámite por folio")
    public TramiteResponse folio(@PathVariable String folio) {
        return tramites.porFolio(folio);
    }

    @GetMapping("/mios")
    @Operation(summary = "HU-06 Listar los trámites del ciudadano")
    public List<TramiteResponse> mios() {
        return tramites.mios();
    }

    @GetMapping("/bandeja")
    @Operation(summary = "HU-03 y HU-05 Bandeja del servidor público")
    public List<TramiteResponse> bandeja() {
        return tramites.bandeja();
    }

    @PostMapping("/folio/{folio}/dictamen")
    @Operation(summary = "HU-03 y HU-05 Dictaminar un trámite")
    public TramiteResponse dictamen(@PathVariable String folio, @RequestBody DictamenRequest request) {
        return tramites.dictaminar(folio, request);
    }

    @PostMapping("/folio/{folio}/subsanar")
    @Operation(summary = "HU-06 Subsanar un trámite observado")
    public TramiteResponse subsanar(@PathVariable String folio, @RequestBody SubsaniarRequest request) {
        return tramites.subsanar(folio, request);
    }
}
