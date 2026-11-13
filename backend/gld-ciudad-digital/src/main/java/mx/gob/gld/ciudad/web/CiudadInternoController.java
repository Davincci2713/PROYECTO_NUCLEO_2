package mx.gob.gld.ciudad.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.ciudad.application.CiudadService;
import mx.gob.gld.ciudad.web.CiudadDtos.AltaPerfilRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ciudad/interno")
@Tag(name = "Ciudad interna", description = "HU-18 Alta de perfil al registrarse")
public class CiudadInternoController {

    private final CiudadService ciudad;

    public CiudadInternoController(CiudadService ciudad) {
        this.ciudad = ciudad;
    }

    @PostMapping("/perfiles")
    @Operation(summary = "HU-18 Crear el perfil de ciudad de un ciudadano nuevo")
    public ResponseEntity<Void> alta(@RequestBody AltaPerfilRequest request) {
        ciudad.altaPerfil(request == null ? null : request.usuarioId(), request == null ? null : request.nombre());
        return ResponseEntity.noContent().build();
    }
}
