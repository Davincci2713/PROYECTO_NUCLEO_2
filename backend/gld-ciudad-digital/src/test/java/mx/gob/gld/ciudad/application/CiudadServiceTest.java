package mx.gob.gld.ciudad.application;

import mx.gob.gld.ciudad.infrastructure.EncuestaEntity;
import mx.gob.gld.ciudad.infrastructure.EncuestaRepository;
import mx.gob.gld.ciudad.infrastructure.EventoEntity;
import mx.gob.gld.ciudad.infrastructure.EventoRepository;
import mx.gob.gld.ciudad.infrastructure.ForoEntity;
import mx.gob.gld.ciudad.infrastructure.ForoRepository;
import mx.gob.gld.ciudad.infrastructure.InscripcionEntity;
import mx.gob.gld.ciudad.infrastructure.InscripcionRepository;
import mx.gob.gld.ciudad.infrastructure.MensajeChatEntity;
import mx.gob.gld.ciudad.infrastructure.MensajeChatRepository;
import mx.gob.gld.ciudad.infrastructure.MensajeForoEntity;
import mx.gob.gld.ciudad.infrastructure.MensajeForoRepository;
import mx.gob.gld.ciudad.infrastructure.OpcionEncuestaEntity;
import mx.gob.gld.ciudad.infrastructure.OpcionEncuestaRepository;
import mx.gob.gld.ciudad.infrastructure.PalabraBloqueadaEntity;
import mx.gob.gld.ciudad.infrastructure.PalabraBloqueadaRepository;
import mx.gob.gld.ciudad.infrastructure.PerfilEntity;
import mx.gob.gld.ciudad.infrastructure.PerfilRepository;
import mx.gob.gld.ciudad.infrastructure.PreferenciaAlertaEntity;
import mx.gob.gld.ciudad.infrastructure.PreferenciaAlertaRepository;
import mx.gob.gld.ciudad.infrastructure.PymeEntity;
import mx.gob.gld.ciudad.infrastructure.PymeRepository;
import mx.gob.gld.ciudad.infrastructure.SalaEntity;
import mx.gob.gld.ciudad.infrastructure.SalaRepository;
import mx.gob.gld.ciudad.infrastructure.VotoEntity;
import mx.gob.gld.ciudad.infrastructure.VotoRepository;
import mx.gob.gld.ciudad.web.CiudadDtos.CuerpoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EncuestaRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EncuestaResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.EnsayoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EventoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PerfilRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PreferenciasResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.SalaRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.TextoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.VotoRequest;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.ApiException;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CiudadServiceTest {

    private static final UUID USUARIO = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final Instant AHORA = Instant.parse("2026-11-08T15:00:00Z");

    @Mock PerfilRepository perfiles;
    @Mock ForoRepository foros;
    @Mock MensajeForoRepository mensajesForo;
    @Mock EncuestaRepository encuestas;
    @Mock OpcionEncuestaRepository opciones;
    @Mock VotoRepository votos;
    @Mock PymeRepository pymes;
    @Mock SalaRepository salas;
    @Mock MensajeChatRepository mensajesChat;
    @Mock PalabraBloqueadaRepository palabras;
    @Mock PreferenciaAlertaRepository preferencias;
    @Mock EventoRepository eventos;
    @Mock InscripcionRepository inscripciones;
    @Mock IdentidadCliente identidad;
    @Mock BitacoraService bitacora;
    @Mock mx.gob.gld.ciudad.infrastructure.AlertaEnviadaRepository alertas;

    private CiudadService servicio;

    @BeforeEach
    void prepara() {
        servicio = new CiudadService(perfiles, foros, mensajesForo, encuestas, opciones, votos, pymes, salas,
                mensajesChat, palabras, preferencias, alertas, eventos, inscripciones, identidad, bitacora,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        TenantContext.set("05021");
        como("CIUDADANO", "Ana Ruiz");
        when(identidad.totpActivo(any(), any())).thenReturn(true);
    }

    @AfterEach
    void limpia() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void elPerfilSeCreaActualizaYRechazaDatosVacios() {
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.altaPerfil(null, "Ana")));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.altaPerfil(USUARIO, " ")));
        when(perfiles.existsById(USUARIO)).thenReturn(true);
        servicio.altaPerfil(USUARIO, "Ana");
        when(perfiles.existsById(USUARIO)).thenReturn(false);
        servicio.altaPerfil(USUARIO, " Ana Ruiz ");
        verify(perfiles).save(any());

        assertEquals("NO_ENCONTRADO", codigo(servicio::miPerfil));
        PerfilEntity ajeno = perfil();
        ajeno.tenantId = "99999";
        when(perfiles.findById(USUARIO)).thenReturn(Optional.of(ajeno));
        assertEquals("NO_ENCONTRADO", codigo(servicio::miPerfil));
        PerfilEntity perfil = perfil();
        when(perfiles.findById(USUARIO)).thenReturn(Optional.of(perfil));
        assertTrue(servicio.miPerfil().totpActivo());
        assertEquals("", servicio.actualizarPerfil(null).biografia());
        assertEquals("Centro", servicio.actualizarPerfil(new PerfilRequest("  Hola  ", " Centro ")).colonia());
        assertEquals(500, servicio.actualizarPerfil(new PerfilRequest("b".repeat(600), "c".repeat(200))).biografia().length());
    }

    @Test
    void forosComentariosYOficial() {
        assertTrue(servicio.foros().isEmpty());
        ForoEntity foro = foro();
        when(foros.findByTenantIdOrderByCreadoEnDesc("05021")).thenReturn(List.of(foro));
        MensajeForoEntity mensaje = mensaje(foro.id);
        when(mensajesForo.findByForoIdIn(any())).thenReturn(List.of(mensaje));
        when(perfiles.findAllById(any())).thenReturn(List.of());
        assertEquals("Ciudadano", servicio.foros().get(0).autorNombre());
        assertEquals(1, servicio.foros().get(0).respuestas());
        PerfilEntity autor = perfil();
        autor.usuarioId = foro.autorId;
        when(perfiles.findAllById(any())).thenReturn(List.of(autor));
        assertEquals("Ana Ruiz", servicio.foros().get(0).autorNombre());

        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearForo(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearForo(new ForoRequest("t".repeat(181), "cuerpo"))));
        when(perfiles.existsById(USUARIO)).thenReturn(false);
        como("CIUDADANO", null);
        assertEquals(0, servicio.crearForo(new ForoRequest(" Alumbrado ", " Siguen apagadas ")).respuestas());
        como("CIUDADANO", "Ana Ruiz");
        when(perfiles.existsById(USUARIO)).thenReturn(true);
        assertEquals("Alumbrado", servicio.crearForo(new ForoRequest("Alumbrado", "cuerpo")).titulo());

        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.foro(foro.id)));
        when(foros.findByTenantIdAndId("05021", foro.id)).thenReturn(Optional.of(foro));
        when(mensajesForo.findByForoIdOrderByCreadoEnAsc(foro.id)).thenReturn(List.of(mensaje));
        when(perfiles.findById(foro.autorId)).thenReturn(Optional.empty());
        assertEquals("Ciudadano", servicio.foro(foro.id).autorNombre());
        when(perfiles.findById(foro.autorId)).thenReturn(Optional.of(autor));
        assertEquals(1, servicio.foro(foro.id).mensajes().size());

        assertEquals("DATO_INVALIDO", codigo(() -> servicio.comentar(foro.id, null, false)));
        assertEquals("NO_AUTORIZADO", codigo(() -> servicio.comentar(foro.id, new CuerpoRequest("Gracias"), true)));
        como("SERVIDOR_PUBLICO", "n".repeat(200));
        assertTrue(servicio.comentar(foro.id, new CuerpoRequest(" Gracias por el aviso "), true).esOficial());
        como("ADMINISTRADOR", "Laura");
        assertFalse(servicio.comentar(foro.id, new CuerpoRequest("Recibido"), false).esOficial());
    }

    @Test
    void laEncuestaAdmiteUnSoloVoto() {
        assertTrue(servicio.encuestas().isEmpty());
        assertEquals("NO_AUTORIZADO", codigo(() -> servicio.crearEncuesta(new EncuestaRequest("¿Obra?", List.of("Sí", "No"), 15))));
        como("SERVIDOR_PUBLICO", "Laura");
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEncuesta(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEncuesta(new EncuestaRequest("¿Obra?", List.of("Sí"), 15))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEncuesta(new EncuestaRequest("¿Obra?", null, 15))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEncuesta(new EncuestaRequest("¿Obra?", List.of("Sí", " "), 15))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEncuesta(new EncuestaRequest("¿Obra?", List.of("Sí", "No"), null))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEncuesta(new EncuestaRequest("¿Obra?", List.of("Sí", "No"), 0))));
        EncuestaResponse creada = servicio.crearEncuesta(new EncuestaRequest(" ¿Qué obra? ", List.of(" Sí ", "o".repeat(200)), 15));
        assertEquals(2, creada.opciones().size());
        assertFalse(creada.cerrada());
        assertEquals(180, creada.opciones().get(1).texto().length());

        EncuestaEntity encuesta = new EncuestaEntity();
        encuesta.id = UUID.randomUUID();
        encuesta.tenantId = "05021";
        encuesta.pregunta = "¿Qué obra?";
        encuesta.cierraEn = AHORA.minusSeconds(1);
        encuesta.creadorId = USUARIO;
        when(encuestas.findByTenantIdAndId("05021", encuesta.id)).thenReturn(Optional.empty());
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(UUID.randomUUID()))));
        when(encuestas.findByTenantIdAndId("05021", encuesta.id)).thenReturn(Optional.of(encuesta));
        assertEquals("ENCUESTA_CERRADA", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(UUID.randomUUID()))));
        encuesta.cierraEn = AHORA.plusSeconds(3600);
        when(votos.existsByEncuestaIdAndUsuarioId(encuesta.id, USUARIO)).thenReturn(true);
        assertEquals("YA_VOTO", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(UUID.randomUUID()))));
        when(votos.existsByEncuestaIdAndUsuarioId(encuesta.id, USUARIO)).thenReturn(false);
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.votar(encuesta.id, null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(null))));
        UUID opcionId = UUID.randomUUID();
        when(opciones.findById(opcionId)).thenReturn(Optional.empty());
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(opcionId))));
        OpcionEncuestaEntity ajena = new OpcionEncuestaEntity();
        ajena.id = opcionId;
        ajena.encuestaId = UUID.randomUUID();
        ajena.texto = "No";
        when(opciones.findById(opcionId)).thenReturn(Optional.of(ajena));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(opcionId))));
        ajena.encuestaId = encuesta.id;
        servicio.votar(encuesta.id, new VotoRequest(opcionId));
        verify(bitacora).registrar(any(), any(), org.mockito.ArgumentMatchers.eq("VOTO"), any(), any(), any());
        doThrow(new DataIntegrityViolationException("dup")).when(votos).saveAndFlush(any());
        assertEquals("YA_VOTO", codigo(() -> servicio.votar(encuesta.id, new VotoRequest(opcionId))));

        OpcionEncuestaEntity opcion = new OpcionEncuestaEntity();
        opcion.id = opcionId;
        opcion.encuestaId = encuesta.id;
        opcion.texto = "Sí";
        encuesta.cierraEn = AHORA.minusSeconds(1);
        when(encuestas.findByTenantIdOrderByCierraEnAsc("05021")).thenReturn(List.of(encuesta));
        when(opciones.findByEncuestaIdInOrderByIdAsc(any())).thenReturn(List.of(opcion));
        VotoEntity voto = new VotoEntity();
        voto.encuestaId = encuesta.id;
        voto.opcionId = opcionId;
        voto.usuarioId = USUARIO;
        when(votos.findByEncuestaIdIn(any())).thenReturn(List.of(voto));
        EncuestaResponse lista = servicio.encuestas().get(0);
        assertTrue(lista.yaVote());
        assertTrue(lista.cerrada());
        assertEquals(1, lista.opciones().get(0).votos());
    }

    @Test
    void pymesSalasYChatBloqueanLenguaje() {
        PymeEntity pyme = new PymeEntity();
        pyme.id = UUID.randomUUID();
        pyme.nombre = "Café Norte";
        pyme.giro = "Alimentos";
        pyme.descripcion = "Café";
        pyme.telefono = "5512340001";
        pyme.colonia = "Norte";
        when(pymes.buscar("05021", "")).thenReturn(List.of(pyme));
        assertEquals("Café Norte", servicio.pymes(null).get(0).nombre());
        when(pymes.buscar("05021", "café")).thenReturn(List.of());
        assertTrue(servicio.pymes(" café ").isEmpty());
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearPyme(null)));
        PymeResponse pymeCreada = servicio.crearPyme(new PymeRequest(" Café Norte ", " Alimentos ", " Café ", " 5512340001 ", " Norte "));
        assertEquals("Café Norte", pymeCreada.nombre());
        assertEquals("Alimentos", pymeCreada.giro());

        SalaEntity sala = new SalaEntity();
        sala.id = UUID.randomUUID();
        sala.nombre = "Centro";
        sala.tema = "Avisos";
        when(salas.findByTenantIdOrderByNombreAsc("05021")).thenReturn(List.of(sala));
        assertEquals("Centro", servicio.salas().get(0).nombre());
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearSala(new SalaRequest(" ", "Avisos"))));
        assertEquals("Vecinos", servicio.crearSala(new SalaRequest(" Vecinos ", " Avisos ")).nombre());
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.mensajes(sala.id)));
        when(salas.findByTenantIdAndId("05021", sala.id)).thenReturn(Optional.of(sala));
        MensajeChatEntity mensaje = new MensajeChatEntity();
        mensaje.id = UUID.randomUUID();
        mensaje.autorNombre = "Ana";
        mensaje.texto = "Hola";
        mensaje.creadoEn = AHORA;
        when(mensajesChat.findBySalaIdOrderByCreadoEnAsc(sala.id)).thenReturn(List.of(mensaje));
        assertEquals("Hola", servicio.mensajes(sala.id).get(0).texto());
        PalabraBloqueadaEntity bloqueada = new PalabraBloqueadaEntity();
        bloqueada.palabra = "idiota";
        when(palabras.findAll()).thenReturn(List.of(bloqueada));
        assertEquals("LENGUAJE_NO_PERMITIDO", codigo(() -> servicio.publicar(sala.id, new TextoRequest("Eres un idiota"))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.publicar(sala.id, null)));
        assertEquals("Buenos días", servicio.publicar(sala.id, new TextoRequest(" Buenos días ")).texto());
    }

    @Test
    void alertasYEventos() {
        assertTrue(servicio.preferencias().temas().isEmpty());
        PreferenciaAlertaEntity ajena = new PreferenciaAlertaEntity();
        ajena.usuarioId = USUARIO;
        ajena.tenantId = "99999";
        ajena.telefono = "1";
        ajena.temas = new String[] {"SISMO"};
        when(preferencias.findById(USUARIO)).thenReturn(Optional.of(ajena));
        assertEquals("", servicio.preferencias().telefono());
        PreferenciaAlertaEntity mia = new PreferenciaAlertaEntity();
        mia.usuarioId = USUARIO;
        mia.tenantId = "05021";
        mia.telefono = "5512340001";
        mia.temas = null;
        when(preferencias.findById(USUARIO)).thenReturn(Optional.of(mia));
        assertTrue(servicio.preferencias().temas().isEmpty());
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.guardarPreferencias(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.guardarPreferencias(new PreferenciasResponse(null, List.of()))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.guardarPreferencias(new PreferenciasResponse("5512340001", List.of("OTRO")))));
        when(preferencias.findById(USUARIO)).thenReturn(Optional.empty());
        PreferenciasResponse guardadas = servicio.guardarPreferencias(
                new PreferenciasResponse(" 551234000199999999999 ", List.of(" SISMO ", " ", "SISMO", "SALUD")));
        assertEquals(20, guardadas.telefono().length());
        assertEquals(List.of("SISMO", "SALUD"), guardadas.temas());
        when(preferencias.findById(USUARIO)).thenReturn(Optional.of(mia));
        servicio.guardarPreferencias(new PreferenciasResponse("5512340001", null));

        assertEquals("NO_AUTORIZADO", codigo(() -> servicio.ensayo(new EnsayoRequest("SISMO", "Simulacro"))));
        como("SERVIDOR_PUBLICO", "Laura");
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.ensayo(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.ensayo(new EnsayoRequest(" FUEGO ", "Simulacro"))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.ensayo(new EnsayoRequest("SISMO", " "))));
        PreferenciaAlertaEntity sinTema = new PreferenciaAlertaEntity();
        sinTema.temas = null;
        PreferenciaAlertaEntity otra = new PreferenciaAlertaEntity();
        otra.usuarioId = UUID.randomUUID();
        otra.telefono = "5500000000";
        otra.temas = new String[] {"SALUD"};
        mia.temas = new String[] {"SISMO"};
        when(preferencias.findByTenantId("05021")).thenReturn(List.of(sinTema, otra, mia));
        assertEquals(1, servicio.ensayo(new EnsayoRequest("SISMO", " Simulacro a las 11:00 ")).destinatarios());

        como("CIUDADANO", "Ana Ruiz");
        assertEquals("NO_AUTORIZADO", codigo(() -> servicio.crearEvento(new EventoRequest("T", "D", "L", AHORA))));
        como("ADMINISTRADOR", "Laura");
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEvento(null)));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEvento(new EventoRequest("T", "D", "L", null))));
        assertEquals("DATO_INVALIDO", codigo(() -> servicio.crearEvento(new EventoRequest(" ", "D", "L", AHORA))));
        assertEquals("Jornada", servicio.crearEvento(new EventoRequest(" Jornada ", " Módulos ", " Plaza ", AHORA)).titulo());

        EventoEntity evento = new EventoEntity();
        evento.id = UUID.randomUUID();
        evento.titulo = "Jornada";
        evento.descripcion = "Módulos";
        evento.lugar = "Plaza";
        evento.iniciaEn = AHORA;
        when(eventos.findByTenantIdOrderByIniciaEnAsc("05021")).thenReturn(List.of(evento));
        InscripcionEntity inscripcion = new InscripcionEntity();
        inscripcion.eventoId = evento.id;
        when(inscripciones.findByUsuarioId(USUARIO)).thenReturn(List.of(inscripcion));
        assertTrue(servicio.eventos().get(0).inscrito());
        assertEquals("NO_ENCONTRADO", codigo(() -> servicio.inscribir(evento.id)));
        when(eventos.findByTenantIdAndId("05021", evento.id)).thenReturn(Optional.of(evento));
        when(inscripciones.findByEventoIdAndUsuarioId(evento.id, USUARIO)).thenReturn(Optional.of(inscripcion));
        servicio.inscribir(evento.id);
        when(inscripciones.findByEventoIdAndUsuarioId(evento.id, USUARIO)).thenReturn(Optional.empty());
        servicio.inscribir(evento.id);
        doThrow(new DataIntegrityViolationException("dup")).when(inscripciones).saveAndFlush(any());
        servicio.inscribir(evento.id);
        servicio.cancelarInscripcion(evento.id);
        when(inscripciones.findByEventoIdAndUsuarioId(evento.id, USUARIO)).thenReturn(Optional.of(inscripcion));
        servicio.cancelarInscripcion(evento.id);
        verify(inscripciones).delete(inscripcion);
    }

    private void como(String rol, String nombre) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new GldPrincipal(USUARIO, "ana@gld.demo", nombre, rol, "05021", "ok"), null));
    }

    private static String codigo(Runnable accion) {
        return assertThrows(ApiException.class, accion::run).getCodigo();
    }

    private static PerfilEntity perfil() {
        PerfilEntity perfil = new PerfilEntity();
        perfil.usuarioId = USUARIO;
        perfil.tenantId = "05021";
        perfil.nombre = "Ana Ruiz";
        perfil.biografia = "";
        perfil.colonia = "";
        return perfil;
    }

    private static ForoEntity foro() {
        ForoEntity foro = new ForoEntity();
        foro.id = UUID.randomUUID();
        foro.autorId = UUID.randomUUID();
        foro.titulo = "Alumbrado";
        foro.cuerpo = "Apagado";
        foro.creadoEn = AHORA;
        return foro;
    }

    private static MensajeForoEntity mensaje(UUID foroId) {
        MensajeForoEntity mensaje = new MensajeForoEntity();
        mensaje.id = UUID.randomUUID();
        mensaje.foroId = foroId;
        mensaje.autorNombre = "Ana";
        mensaje.cuerpo = "Gracias";
        mensaje.esOficial = false;
        mensaje.creadoEn = AHORA;
        return mensaje;
    }
}
