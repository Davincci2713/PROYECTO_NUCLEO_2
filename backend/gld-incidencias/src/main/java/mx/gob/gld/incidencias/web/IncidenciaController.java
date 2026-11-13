package mx.gob.gld.incidencias.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.incidencias.application.IncidenciaService;
import mx.gob.gld.incidencias.infrastructure.IncidenciaEntity;
import mx.gob.gld.incidencias.web.IncidenciaDtos.AltaRequest;
import mx.gob.gld.incidencias.web.IncidenciaDtos.IncidenciaResponse;
import mx.gob.gld.incidencias.web.IncidenciaDtos.VistaPreviaRequest;
import mx.gob.gld.incidencias.web.IncidenciaDtos.VistaPreviaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidencias")
@Tag(name = "Incidencias", description = "HU-24 Reporte ciudadano con brigada")
public class IncidenciaController {

    private final IncidenciaService incidencias;

    public IncidenciaController(IncidenciaService incidencias) {
        this.incidencias = incidencias;
    }

    @PostMapping("/vista-previa")
    @Operation(summary = "HU-24 Precargar la dirección a partir del mapa")
    public VistaPreviaResponse vistaPrevia(@RequestBody VistaPreviaRequest request) {
        if (request == null) {
            return incidencias.vistaPrevia(null, null);
        }
        return incidencias.vistaPrevia(request.lat(), request.lng());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-24 Reportar una incidencia")
    public IncidenciaResponse reportar(@RequestBody AltaRequest request) {
        return incidencias.reportar(request);
    }

    @GetMapping("/folio/{folio}")
    @Operation(summary = "HU-24 Consultar una incidencia por folio")
    public IncidenciaResponse folio(@PathVariable String folio) {
        return incidencias.porFolio(folio);
    }

    @GetMapping("/mias")
    @Operation(summary = "HU-24 Listar las incidencias del ciudadano")
    public List<IncidenciaResponse> mias() {
        return incidencias.mias();
    }

    @GetMapping("/{id}/foto")
    @Operation(summary = "HU-24 Descargar la foto de la incidencia")
    public ResponseEntity<byte[]> foto(@PathVariable UUID id) {
        IncidenciaEntity incidencia = incidencias.foto(id);
        MediaType tipo = MediaType.APPLICATION_OCTET_STREAM;
        if (incidencia.fotoContentType != null) {
            try {
                tipo = MediaType.parseMediaType(incidencia.fotoContentType);
            } catch (Exception ignored) {
                tipo = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return ResponseEntity.ok().contentType(tipo).body(incidencia.foto);
    }
}
