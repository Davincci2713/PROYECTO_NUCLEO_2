package mx.gob.gld.ciudad.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.gob.gld.ciudad.application.CiudadService;
import mx.gob.gld.ciudad.web.CiudadDtos.CuerpoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EncuestaRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EncuestaResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.EnsayoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EnsayoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.EventoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EventoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoDetalle;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoResumen;
import mx.gob.gld.ciudad.web.CiudadDtos.MensajeChatResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.MensajeForoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PerfilRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PerfilResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PreferenciasResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.SalaRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.SalaResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.TextoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.VotoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ciudad")
@Tag(name = "Ciudad digital", description = "HU-18 a HU-23 y HU-25")
public class CiudadController {

    private final CiudadService ciudad;

    public CiudadController(CiudadService ciudad) {
        this.ciudad = ciudad;
    }

    @GetMapping("/perfiles/me")
    @Operation(summary = "HU-18 Consultar el perfil del ciudadano")
    public PerfilResponse perfil() {
        return ciudad.miPerfil();
    }

    @PutMapping("/perfiles/me")
    @Operation(summary = "HU-18 Actualizar biografía y colonia")
    public PerfilResponse actualizarPerfil(@RequestBody PerfilRequest request) {
        return ciudad.actualizarPerfil(request);
    }

    @GetMapping("/foros")
    @Operation(summary = "HU-19 Listar foros")
    public List<ForoResumen> foros() {
        return ciudad.foros();
    }

    @PostMapping("/foros")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-19 Publicar un foro")
    public ForoResumen crearForo(@RequestBody ForoRequest request) {
        return ciudad.crearForo(request);
    }

    @GetMapping("/foros/{id}")
    @Operation(summary = "HU-19 Consultar un foro y sus mensajes")
    public ForoDetalle foro(@PathVariable UUID id) {
        return ciudad.foro(id);
    }

    @PostMapping("/foros/{id}/mensajes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-19 Responder en un foro")
    public MensajeForoResponse comentar(@PathVariable UUID id, @RequestBody CuerpoRequest request) {
        return ciudad.comentar(id, request, false);
    }

    @PostMapping("/foros/{id}/respuesta-oficial")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-19 Publicar la respuesta del servidor público")
    public MensajeForoResponse oficial(@PathVariable UUID id, @RequestBody CuerpoRequest request) {
        return ciudad.comentar(id, request, true);
    }

    @GetMapping("/encuestas")
    @Operation(summary = "HU-20 Listar encuestas")
    public List<EncuestaResponse> encuestas() {
        return ciudad.encuestas();
    }

    @PostMapping("/encuestas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-20 Crear una encuesta")
    public EncuestaResponse crearEncuesta(@RequestBody EncuestaRequest request) {
        return ciudad.crearEncuesta(request);
    }

    @PostMapping("/encuestas/{id}/votar")
    @Operation(summary = "HU-20 Registrar un voto único")
    public ResponseEntity<Void> votar(@PathVariable UUID id, @RequestBody VotoRequest request) {
        ciudad.votar(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pymes")
    @Operation(summary = "HU-21 Buscar el directorio PyME")
    public List<PymeResponse> pymes(@RequestParam(required = false) String q) {
        return ciudad.pymes(q);
    }

    @PostMapping("/pymes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-21 Dar de alta una PyME")
    public PymeResponse crearPyme(@RequestBody PymeRequest request) {
        return ciudad.crearPyme(request);
    }

    @GetMapping("/salas")
    @Operation(summary = "HU-22 Listar salas de chat")
    public List<SalaResponse> salas() {
        return ciudad.salas();
    }

    @PostMapping("/salas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-22 Abrir una sala")
    public SalaResponse crearSala(@RequestBody SalaRequest request) {
        return ciudad.crearSala(request);
    }

    @GetMapping("/salas/{id}/mensajes")
    @Operation(summary = "HU-22 Leer los mensajes de una sala")
    public List<MensajeChatResponse> mensajes(@PathVariable UUID id) {
        return ciudad.mensajes(id);
    }

    @PostMapping("/salas/{id}/mensajes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-22 Publicar un mensaje moderado")
    public MensajeChatResponse publicar(@PathVariable UUID id, @RequestBody TextoRequest request) {
        return ciudad.publicar(id, request);
    }

    @GetMapping("/alertas/preferencias")
    @Operation(summary = "HU-23 Consultar preferencias de alerta")
    public PreferenciasResponse preferencias() {
        return ciudad.preferencias();
    }

    @PutMapping("/alertas/preferencias")
    @Operation(summary = "HU-23 Guardar preferencias de alerta")
    public PreferenciasResponse guardarPreferencias(@RequestBody PreferenciasResponse request) {
        return ciudad.guardarPreferencias(request);
    }

    @PostMapping("/alertas/ensayo")
    @Operation(summary = "HU-23 Enviar un SMS simulado por tema")
    public EnsayoResponse ensayo(@RequestBody EnsayoRequest request) {
        return ciudad.ensayo(request);
    }

    @GetMapping("/eventos")
    @Operation(summary = "HU-25 Listar la agenda")
    public List<EventoResponse> eventos() {
        return ciudad.eventos();
    }

    @PostMapping("/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "HU-25 Publicar un evento")
    public EventoResponse crearEvento(@RequestBody EventoRequest request) {
        return ciudad.crearEvento(request);
    }

    @PostMapping("/eventos/{id}/inscripcion")
    @Operation(summary = "HU-25 Inscribirse a un evento")
    public ResponseEntity<Void> inscribir(@PathVariable UUID id) {
        ciudad.inscribir(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/eventos/{id}/inscripcion")
    @Operation(summary = "HU-25 Cancelar la inscripción")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        ciudad.cancelarInscripcion(id);
        return ResponseEntity.noContent().build();
    }
}
