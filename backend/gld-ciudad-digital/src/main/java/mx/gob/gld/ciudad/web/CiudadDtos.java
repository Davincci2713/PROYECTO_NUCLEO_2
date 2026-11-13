package mx.gob.gld.ciudad.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CiudadDtos {

    private CiudadDtos() {
    }

    public record PerfilResponse(UUID usuarioId, String nombre, String biografia, String colonia, boolean totpActivo) {
    }

    public record PerfilRequest(String biografia, String colonia) {
    }

    public record AltaPerfilRequest(UUID usuarioId, String nombre) {
    }

    public record ForoResumen(UUID id, String titulo, String cuerpo, String autorNombre, Instant creadoEn, long respuestas) {
    }

    public record ForoRequest(String titulo, String cuerpo) {
    }

    public record MensajeForoResponse(UUID id, String autorNombre, String cuerpo, boolean esOficial, Instant creadoEn) {
    }

    public record ForoDetalle(UUID id, String titulo, String cuerpo, String autorNombre, Instant creadoEn, long respuestas,
                              List<MensajeForoResponse> mensajes) {
    }

    public record CuerpoRequest(String cuerpo) {
    }

    public record OpcionResponse(UUID id, String texto, long votos) {
    }

    public record EncuestaResponse(UUID id, String pregunta, Instant cierraEn, boolean cerrada, boolean yaVote,
                                   List<OpcionResponse> opciones) {
    }

    public record EncuestaRequest(String pregunta, List<String> opciones, Integer dias) {
    }

    public record VotoRequest(UUID opcionId) {
    }

    public record PymeResponse(UUID id, String nombre, String giro, String descripcion, String telefono, String colonia) {
    }

    public record PymeRequest(String nombre, String giro, String descripcion, String telefono, String colonia) {
    }

    public record SalaResponse(UUID id, String nombre, String tema) {
    }

    public record SalaRequest(String nombre, String tema) {
    }

    public record MensajeChatResponse(UUID id, String autorNombre, String texto, Instant creadoEn) {
    }

    public record TextoRequest(String texto) {
    }

    public record PreferenciasResponse(String telefono, List<String> temas) {
    }

    public record EnsayoRequest(String tema, String mensaje) {
    }

    public record EnsayoResponse(String tema, int destinatarios) {
    }

    public record EventoResponse(UUID id, String titulo, String descripcion, String lugar, Instant iniciaEn, boolean inscrito) {
    }

    public record EventoRequest(String titulo, String descripcion, String lugar, Instant iniciaEn) {
    }
}
