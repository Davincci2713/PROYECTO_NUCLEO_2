package mx.gob.gld.ciudad.application;

import mx.gob.gld.ciudad.domain.Moderacion;
import mx.gob.gld.ciudad.infrastructure.AlertaEnviadaEntity;
import mx.gob.gld.ciudad.infrastructure.AlertaEnviadaRepository;
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
import mx.gob.gld.ciudad.web.CiudadDtos.EnsayoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.EventoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.EventoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoDetalle;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.ForoResumen;
import mx.gob.gld.ciudad.web.CiudadDtos.MensajeChatResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.MensajeForoResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.OpcionResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PerfilRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PerfilResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PreferenciasResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.PymeResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.SalaRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.SalaResponse;
import mx.gob.gld.ciudad.web.CiudadDtos.TextoRequest;
import mx.gob.gld.ciudad.web.CiudadDtos.VotoRequest;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.security.Acceso;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CiudadService {

    private static final Set<String> TEMAS = Set.of("SISMO", "INUNDACION", "SERVICIOS", "SALUD");

    private final PerfilRepository perfiles;
    private final ForoRepository foros;
    private final MensajeForoRepository mensajesForo;
    private final EncuestaRepository encuestas;
    private final OpcionEncuestaRepository opciones;
    private final VotoRepository votos;
    private final PymeRepository pymes;
    private final SalaRepository salas;
    private final MensajeChatRepository mensajesChat;
    private final PalabraBloqueadaRepository palabras;
    private final PreferenciaAlertaRepository preferencias;
    private final AlertaEnviadaRepository alertas;
    private final EventoRepository eventos;
    private final InscripcionRepository inscripciones;
    private final IdentidadCliente identidad;
    private final BitacoraService bitacora;
    private final Clock clock;

    public CiudadService(PerfilRepository perfiles, ForoRepository foros, MensajeForoRepository mensajesForo,
                         EncuestaRepository encuestas, OpcionEncuestaRepository opciones, VotoRepository votos,
                         PymeRepository pymes, SalaRepository salas, MensajeChatRepository mensajesChat,
                         PalabraBloqueadaRepository palabras, PreferenciaAlertaRepository preferencias,
                         AlertaEnviadaRepository alertas, EventoRepository eventos, InscripcionRepository inscripciones,
                         IdentidadCliente identidad, BitacoraService bitacora, Clock clock) {
        this.perfiles = perfiles;
        this.foros = foros;
        this.mensajesForo = mensajesForo;
        this.encuestas = encuestas;
        this.opciones = opciones;
        this.votos = votos;
        this.pymes = pymes;
        this.salas = salas;
        this.mensajesChat = mensajesChat;
        this.palabras = palabras;
        this.preferencias = preferencias;
        this.alertas = alertas;
        this.eventos = eventos;
        this.inscripciones = inscripciones;
        this.identidad = identidad;
        this.bitacora = bitacora;
        this.clock = clock;
    }

    @Transactional
    public void altaPerfil(UUID usuarioId, String nombre) {
        if (usuarioId == null) {
            throw Errores.datoInvalido("usuarioId", "11111111-1111-4111-8111-111111111111",
                    "El usuario es obligatorio. Ejemplo: 11111111-1111-4111-8111-111111111111");
        }
        if (nombre == null || nombre.isBlank()) {
            throw Errores.datoInvalido("nombre", "Luis Pérez", "El nombre es obligatorio. Ejemplo: Luis Pérez");
        }
        if (perfiles.existsById(usuarioId)) {
            return;
        }
        PerfilEntity perfil = new PerfilEntity();
        perfil.usuarioId = usuarioId;
        perfil.tenantId = TenantContext.exigir();
        perfil.nombre = recortar(nombre.trim(), 160);
        perfil.biografia = "";
        perfil.colonia = "";
        perfiles.save(perfil);
    }

    @Transactional(readOnly = true)
    public PerfilResponse miPerfil() {
        GldPrincipal principal = GldPrincipal.actual();
        PerfilEntity perfil = perfiles.findById(principal.id())
                .filter(item -> item.tenantId.equals(TenantContext.exigir()))
                .orElseThrow(() -> Errores.noEncontrado("No hay un perfil para esta cuenta."));
        return new PerfilResponse(perfil.usuarioId, perfil.nombre, perfil.biografia, perfil.colonia,
                identidad.totpActivo(principal.id(), perfil.tenantId));
    }

    @Transactional
    public PerfilResponse actualizarPerfil(PerfilRequest request) {
        GldPrincipal principal = GldPrincipal.actual();
        PerfilEntity perfil = perfiles.findById(principal.id())
                .filter(item -> item.tenantId.equals(TenantContext.exigir()))
                .orElseThrow(() -> Errores.noEncontrado("No hay un perfil para esta cuenta."));
        perfil.biografia = recortar(request == null || request.biografia() == null ? "" : request.biografia().trim(), 500);
        perfil.colonia = recortar(request == null || request.colonia() == null ? "" : request.colonia().trim(), 120);
        return new PerfilResponse(perfil.usuarioId, perfil.nombre, perfil.biografia, perfil.colonia,
                identidad.totpActivo(principal.id(), perfil.tenantId));
    }

    @Transactional(readOnly = true)
    public List<ForoResumen> foros() {
        List<ForoEntity> lista = foros.findByTenantIdOrderByCreadoEnDesc(TenantContext.exigir());
        Map<UUID, Long> cuentas = cuentas(lista);
        Map<UUID, String> nombres = nombres(lista.stream().map(foro -> foro.autorId).toList());
        List<ForoResumen> respuesta = new ArrayList<>();
        for (ForoEntity foro : lista) {
            respuesta.add(new ForoResumen(foro.id, foro.titulo, foro.cuerpo,
                    nombres.getOrDefault(foro.autorId, "Ciudadano"), foro.creadoEn, cuentas.getOrDefault(foro.id, 0L)));
        }
        return respuesta;
    }

    @Transactional
    public ForoResumen crearForo(ForoRequest request) {
        String titulo = exigir(request == null ? null : request.titulo(), "titulo", "Alumbrado de la calle", 180);
        String cuerpo = exigir(request == null ? null : request.cuerpo(), "cuerpo", "Las luminarias siguen apagadas.", 2000);
        GldPrincipal principal = GldPrincipal.actual();
        asegurarPerfil(principal);
        ForoEntity foro = new ForoEntity();
        foro.id = UUID.randomUUID();
        foro.tenantId = TenantContext.exigir();
        foro.autorId = principal.id();
        foro.titulo = titulo;
        foro.cuerpo = cuerpo;
        foro.creadoEn = clock.instant();
        foros.save(foro);
        return new ForoResumen(foro.id, foro.titulo, foro.cuerpo, principal.nombre(), foro.creadoEn, 0);
    }

    @Transactional(readOnly = true)
    public ForoDetalle foro(UUID id) {
        ForoEntity foro = buscarForo(id);
        List<MensajeForoResponse> mensajes = new ArrayList<>();
        for (MensajeForoEntity mensaje : mensajesForo.findByForoIdOrderByCreadoEnAsc(foro.id)) {
            mensajes.add(aMensaje(mensaje));
        }
        String autor = perfiles.findById(foro.autorId).map(perfil -> perfil.nombre).orElse("Ciudadano");
        return new ForoDetalle(foro.id, foro.titulo, foro.cuerpo, autor, foro.creadoEn, mensajes.size(), mensajes);
    }

    @Transactional
    public MensajeForoResponse comentar(UUID id, CuerpoRequest request, boolean oficial) {
        if (oficial) {
            Acceso.exigirServidor();
        }
        ForoEntity foro = buscarForo(id);
        String cuerpo = exigir(request == null ? null : request.cuerpo(), "cuerpo", "Gracias por el aviso.", 2000);
        GldPrincipal principal = GldPrincipal.actual();
        MensajeForoEntity mensaje = new MensajeForoEntity();
        mensaje.id = UUID.randomUUID();
        mensaje.foroId = foro.id;
        mensaje.autorId = principal.id();
        mensaje.autorNombre = recortar(principal.nombre(), 160);
        mensaje.cuerpo = cuerpo;
        mensaje.esOficial = oficial;
        mensaje.creadoEn = clock.instant();
        mensajesForo.save(mensaje);
        return aMensaje(mensaje);
    }

    @Transactional(readOnly = true)
    public List<EncuestaResponse> encuestas() {
        String tenant = TenantContext.exigir();
        UUID usuario = GldPrincipal.actual().id();
        List<EncuestaEntity> lista = encuestas.findByTenantIdOrderByCierraEnAsc(tenant);
        return armarEncuestas(lista, usuario);
    }

    @Transactional
    public EncuestaResponse crearEncuesta(EncuestaRequest request) {
        Acceso.exigirServidor();
        String pregunta = exigir(request == null ? null : request.pregunta(), "pregunta",
                "¿Qué obra debe priorizar el municipio?", 300);
        if (request.opciones() == null || request.opciones().size() < 2) {
            throw Errores.datoInvalido("opciones", "Sí", "La encuesta necesita al menos dos opciones. Ejemplo: Sí");
        }
        if (request.dias() == null || request.dias() < 1) {
            throw Errores.datoInvalido("dias", "15", "Indica cuántos días permanece abierta. Ejemplo: 15");
        }
        List<String> textos = new ArrayList<>();
        for (String opcion : request.opciones()) {
            if (opcion == null || opcion.isBlank()) {
                throw Errores.datoInvalido("opciones", "Sí", "Cada opción necesita un texto. Ejemplo: Sí");
            }
            textos.add(recortar(opcion.trim(), 180));
        }
        GldPrincipal principal = GldPrincipal.actual();
        EncuestaEntity encuesta = new EncuestaEntity();
        encuesta.id = UUID.randomUUID();
        encuesta.tenantId = TenantContext.exigir();
        encuesta.pregunta = pregunta;
        encuesta.cierraEn = clock.instant().plus(request.dias(), ChronoUnit.DAYS);
        encuesta.creadorId = principal.id();
        encuestas.save(encuesta);
        List<OpcionResponse> opcionesRespuesta = new ArrayList<>();
        for (String texto : textos) {
            OpcionEncuestaEntity opcion = new OpcionEncuestaEntity();
            opcion.id = UUID.randomUUID();
            opcion.encuestaId = encuesta.id;
            opcion.texto = texto;
            opciones.save(opcion);
            opcionesRespuesta.add(new OpcionResponse(opcion.id, opcion.texto, 0));
        }
        return new EncuestaResponse(encuesta.id, encuesta.pregunta, encuesta.cierraEn, false, false, opcionesRespuesta);
    }

    @Transactional
    public void votar(UUID id, VotoRequest request) {
        EncuestaEntity encuesta = encuestas.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(() -> Errores.noEncontrado("No hay una encuesta con ese identificador."));
        if (!encuesta.cierraEn.isAfter(clock.instant())) {
            throw Errores.encuestaCerrada();
        }
        GldPrincipal principal = GldPrincipal.actual();
        if (votos.existsByEncuestaIdAndUsuarioId(encuesta.id, principal.id())) {
            throw Errores.yaVoto();
        }
        if (request == null || request.opcionId() == null) {
            throw Errores.datoInvalido("opcionId", "f3333333-3333-4333-8333-333333333331",
                    "Elige una opción de la encuesta. Ejemplo: f3333333-3333-4333-8333-333333333331");
        }
        OpcionEncuestaEntity opcion = opciones.findById(request.opcionId())
                .filter(item -> item.encuestaId.equals(encuesta.id))
                .orElseThrow(() -> Errores.datoInvalido("opcionId", "f3333333-3333-4333-8333-333333333331",
                        "La opción no pertenece a la encuesta. Ejemplo: f3333333-3333-4333-8333-333333333331"));
        VotoEntity voto = new VotoEntity();
        voto.id = UUID.randomUUID();
        voto.encuestaId = encuesta.id;
        voto.opcionId = opcion.id;
        voto.usuarioId = principal.id();
        voto.creadoEn = clock.instant();
        try {
            votos.saveAndFlush(voto);
        } catch (DataIntegrityViolationException ex) {
            throw Errores.yaVoto();
        }
        bitacora.registrar(encuesta.tenantId, principal.id(), "VOTO", "ENCUESTA", encuesta.id.toString(),
                "Voto en: " + encuesta.pregunta);
    }

    @Transactional(readOnly = true)
    public List<PymeResponse> pymes(String q) {
        String filtro = q == null ? "" : q.trim();
        List<PymeResponse> lista = new ArrayList<>();
        for (PymeEntity pyme : pymes.buscar(TenantContext.exigir(), filtro)) {
            lista.add(aPyme(pyme));
        }
        return lista;
    }

    @Transactional
    public PymeResponse crearPyme(PymeRequest request) {
        PymeEntity pyme = new PymeEntity();
        pyme.id = UUID.randomUUID();
        pyme.tenantId = TenantContext.exigir();
        pyme.duenoId = GldPrincipal.actual().id();
        pyme.nombre = exigir(request == null ? null : request.nombre(), "nombre", "Café Norte", 160);
        pyme.giro = exigir(request == null ? null : request.giro(), "giro", "Alimentos", 80);
        pyme.descripcion = exigir(request == null ? null : request.descripcion(), "descripcion", "Café de la colonia", 500);
        pyme.telefono = exigir(request == null ? null : request.telefono(), "telefono", "5599990000", 20);
        pyme.colonia = exigir(request == null ? null : request.colonia(), "colonia", "Norte", 120);
        pyme.creadoEn = clock.instant();
        pymes.save(pyme);
        return aPyme(pyme);
    }

    @Transactional(readOnly = true)
    public List<SalaResponse> salas() {
        List<SalaResponse> lista = new ArrayList<>();
        for (SalaEntity sala : salas.findByTenantIdOrderByNombreAsc(TenantContext.exigir())) {
            lista.add(new SalaResponse(sala.id, sala.nombre, sala.tema));
        }
        return lista;
    }

    @Transactional
    public SalaResponse crearSala(SalaRequest request) {
        SalaEntity sala = new SalaEntity();
        sala.id = UUID.randomUUID();
        sala.tenantId = TenantContext.exigir();
        sala.nombre = exigir(request == null ? null : request.nombre(), "nombre", "Vecinos del centro", 120);
        sala.tema = exigir(request == null ? null : request.tema(), "tema", "Avisos de la colonia", 160);
        salas.save(sala);
        return new SalaResponse(sala.id, sala.nombre, sala.tema);
    }

    @Transactional(readOnly = true)
    public List<MensajeChatResponse> mensajes(UUID id) {
        SalaEntity sala = buscarSala(id);
        List<MensajeChatResponse> lista = new ArrayList<>();
        for (MensajeChatEntity mensaje : mensajesChat.findBySalaIdOrderByCreadoEnAsc(sala.id)) {
            lista.add(new MensajeChatResponse(mensaje.id, mensaje.autorNombre, mensaje.texto, mensaje.creadoEn));
        }
        return lista;
    }

    @Transactional
    public MensajeChatResponse publicar(UUID id, TextoRequest request) {
        SalaEntity sala = buscarSala(id);
        String texto = exigir(request == null ? null : request.texto(), "texto", "Buenos días, vecinos", 500);
        List<String> bloqueadas = palabras.findAll().stream().map(palabra -> palabra.palabra).toList();
        Moderacion.validar(texto, bloqueadas);
        GldPrincipal principal = GldPrincipal.actual();
        MensajeChatEntity mensaje = new MensajeChatEntity();
        mensaje.id = UUID.randomUUID();
        mensaje.salaId = sala.id;
        mensaje.autorId = principal.id();
        mensaje.autorNombre = recortar(principal.nombre(), 160);
        mensaje.texto = texto;
        mensaje.creadoEn = clock.instant();
        mensajesChat.save(mensaje);
        return new MensajeChatResponse(mensaje.id, mensaje.autorNombre, mensaje.texto, mensaje.creadoEn);
    }

    @Transactional(readOnly = true)
    public PreferenciasResponse preferencias() {
        return preferencias.findById(GldPrincipal.actual().id())
                .filter(item -> item.tenantId.equals(TenantContext.exigir()))
                .map(this::aPreferencias)
                .orElseGet(() -> new PreferenciasResponse("", List.of()));
    }

    @Transactional
    public PreferenciasResponse guardarPreferencias(PreferenciasResponse request) {
        if (request == null || request.telefono() == null) {
            throw Errores.datoInvalido("telefono", "5512340001",
                    "El teléfono de alertas es obligatorio. Ejemplo: 5512340001");
        }
        String telefono = recortar(request.telefono().trim(), 20);
        List<String> temas = normalizarTemas(request.temas());
        GldPrincipal principal = GldPrincipal.actual();
        PreferenciaAlertaEntity preferencia = preferencias.findById(principal.id()).orElseGet(PreferenciaAlertaEntity::new);
        preferencia.usuarioId = principal.id();
        preferencia.tenantId = TenantContext.exigir();
        preferencia.telefono = telefono;
        preferencia.temas = temas.toArray(String[]::new);
        preferencias.save(preferencia);
        return aPreferencias(preferencia);
    }

    @Transactional
    public EnsayoResponse ensayo(EnsayoRequest request) {
        Acceso.exigirServidor();
        if (request == null || request.tema() == null || !TEMAS.contains(request.tema().trim())) {
            throw Errores.datoInvalido("tema", "SISMO",
                    "El tema debe ser SISMO, INUNDACION, SERVICIOS o SALUD. Ejemplo: SISMO");
        }
        String mensaje = exigir(request.mensaje(), "mensaje", "Simulacro a las 11:00", 320);
        String tema = request.tema().trim();
        String tenant = TenantContext.exigir();
        int destinatarios = 0;
        Instant ahora = clock.instant();
        for (PreferenciaAlertaEntity preferencia : preferencias.findByTenantId(tenant)) {
            if (preferencia.temas == null || Arrays.stream(preferencia.temas).noneMatch(tema::equals)) {
                continue;
            }
            AlertaEnviadaEntity alerta = new AlertaEnviadaEntity();
            alerta.id = UUID.randomUUID();
            alerta.tenantId = tenant;
            alerta.usuarioId = preferencia.usuarioId;
            alerta.telefono = preferencia.telefono;
            alerta.tema = tema;
            alerta.mensaje = mensaje;
            alerta.enviadaEn = ahora;
            alertas.save(alerta);
            destinatarios++;
        }
        return new EnsayoResponse(tema, destinatarios);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> eventos() {
        Set<UUID> mios = new java.util.HashSet<>();
        for (InscripcionEntity inscripcion : inscripciones.findByUsuarioId(GldPrincipal.actual().id())) {
            mios.add(inscripcion.eventoId);
        }
        List<EventoResponse> lista = new ArrayList<>();
        for (EventoEntity evento : eventos.findByTenantIdOrderByIniciaEnAsc(TenantContext.exigir())) {
            lista.add(aEvento(evento, mios.contains(evento.id)));
        }
        return lista;
    }

    @Transactional
    public EventoResponse crearEvento(EventoRequest request) {
        Acceso.exigirServidor();
        if (request == null || request.iniciaEn() == null) {
            throw Errores.datoInvalido("iniciaEn", "2026-12-01T16:00:00Z",
                    "Indica cuándo inicia el evento. Ejemplo: 2026-12-01T16:00:00Z");
        }
        EventoEntity evento = new EventoEntity();
        evento.id = UUID.randomUUID();
        evento.tenantId = TenantContext.exigir();
        evento.titulo = exigir(request.titulo(), "titulo", "Jornada de salud", 180);
        evento.descripcion = exigir(request.descripcion(), "descripcion", "Módulos de vacunación", 800);
        evento.lugar = exigir(request.lugar(), "lugar", "Plaza principal", 180);
        evento.iniciaEn = request.iniciaEn();
        evento.creadorId = GldPrincipal.actual().id();
        eventos.save(evento);
        return aEvento(evento, false);
    }

    @Transactional
    public void inscribir(UUID id) {
        EventoEntity evento = buscarEvento(id);
        UUID usuario = GldPrincipal.actual().id();
        if (inscripciones.findByEventoIdAndUsuarioId(evento.id, usuario).isPresent()) {
            return;
        }
        InscripcionEntity inscripcion = new InscripcionEntity();
        inscripcion.id = UUID.randomUUID();
        inscripcion.eventoId = evento.id;
        inscripcion.usuarioId = usuario;
        inscripcion.creadoEn = clock.instant();
        try {
            inscripciones.saveAndFlush(inscripcion);
        } catch (DataIntegrityViolationException ex) {
            // repetir la inscripción también responde 204
        }
    }

    @Transactional
    public void cancelarInscripcion(UUID id) {
        EventoEntity evento = buscarEvento(id);
        inscripciones.findByEventoIdAndUsuarioId(evento.id, GldPrincipal.actual().id())
                .ifPresent(inscripciones::delete);
    }

    private void asegurarPerfil(GldPrincipal principal) {
        if (perfiles.existsById(principal.id())) {
            return;
        }
        PerfilEntity perfil = new PerfilEntity();
        perfil.usuarioId = principal.id();
        perfil.tenantId = TenantContext.exigir();
        perfil.nombre = recortar(principal.nombre(), 160);
        perfil.biografia = "";
        perfil.colonia = "";
        perfiles.save(perfil);
    }

    private List<EncuestaResponse> armarEncuestas(List<EncuestaEntity> lista, UUID usuario) {
        if (lista.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = lista.stream().map(encuesta -> encuesta.id).toList();
        Map<UUID, List<OpcionEncuestaEntity>> porEncuesta = new HashMap<>();
        for (OpcionEncuestaEntity opcion : opciones.findByEncuestaIdInOrderByIdAsc(ids)) {
            porEncuesta.computeIfAbsent(opcion.encuestaId, key -> new ArrayList<>()).add(opcion);
        }
        Map<UUID, Long> cuentas = new HashMap<>();
        Set<UUID> votadas = new java.util.HashSet<>();
        for (VotoEntity voto : votos.findByEncuestaIdIn(ids)) {
            cuentas.merge(voto.opcionId, 1L, Long::sum);
            if (voto.usuarioId.equals(usuario)) {
                votadas.add(voto.encuestaId);
            }
        }
        Instant ahora = clock.instant();
        List<EncuestaResponse> respuesta = new ArrayList<>();
        for (EncuestaEntity encuesta : lista) {
            List<OpcionResponse> opcionesRespuesta = new ArrayList<>();
            for (OpcionEncuestaEntity opcion : porEncuesta.getOrDefault(encuesta.id, List.of())) {
                opcionesRespuesta.add(new OpcionResponse(opcion.id, opcion.texto, cuentas.getOrDefault(opcion.id, 0L)));
            }
            respuesta.add(new EncuestaResponse(
                    encuesta.id, encuesta.pregunta, encuesta.cierraEn,
                    !encuesta.cierraEn.isAfter(ahora), votadas.contains(encuesta.id), opcionesRespuesta));
        }
        return respuesta;
    }

    private Map<UUID, Long> cuentas(List<ForoEntity> lista) {
        Map<UUID, Long> cuentas = new HashMap<>();
        if (lista.isEmpty()) {
            return cuentas;
        }
        List<UUID> ids = lista.stream().map(foro -> foro.id).toList();
        for (MensajeForoEntity mensaje : mensajesForo.findByForoIdIn(ids)) {
            cuentas.merge(mensaje.foroId, 1L, Long::sum);
        }
        return cuentas;
    }

    private Map<UUID, String> nombres(List<UUID> autores) {
        Map<UUID, String> nombres = new HashMap<>();
        if (autores.isEmpty()) {
            return nombres;
        }
        for (PerfilEntity perfil : perfiles.findAllById(autores)) {
            nombres.put(perfil.usuarioId, perfil.nombre);
        }
        return nombres;
    }

    private ForoEntity buscarForo(UUID id) {
        return foros.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(() -> Errores.noEncontrado("No hay un foro con ese identificador."));
    }

    private SalaEntity buscarSala(UUID id) {
        return salas.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(() -> Errores.noEncontrado("No hay una sala con ese identificador."));
    }

    private EventoEntity buscarEvento(UUID id) {
        return eventos.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(() -> Errores.noEncontrado("No hay un evento con ese identificador."));
    }

    private static MensajeForoResponse aMensaje(MensajeForoEntity mensaje) {
        return new MensajeForoResponse(mensaje.id, mensaje.autorNombre, mensaje.cuerpo, mensaje.esOficial, mensaje.creadoEn);
    }

    private static PymeResponse aPyme(PymeEntity pyme) {
        return new PymeResponse(pyme.id, pyme.nombre, pyme.giro, pyme.descripcion, pyme.telefono, pyme.colonia);
    }

    private PreferenciasResponse aPreferencias(PreferenciaAlertaEntity preferencia) {
        List<String> temas = preferencia.temas == null ? List.of() : List.of(preferencia.temas);
        return new PreferenciasResponse(preferencia.telefono, temas);
    }

    private static EventoResponse aEvento(EventoEntity evento, boolean inscrito) {
        return new EventoResponse(evento.id, evento.titulo, evento.descripcion, evento.lugar, evento.iniciaEn, inscrito);
    }

    private static List<String> normalizarTemas(List<String> temas) {
        if (temas == null || temas.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> unicos = new LinkedHashSet<>();
        for (String tema : temas) {
            if (tema == null || tema.isBlank()) {
                continue;
            }
            String normalizado = tema.trim();
            if (!TEMAS.contains(normalizado)) {
                throw Errores.datoInvalido("temas", "SISMO",
                        "El tema debe ser SISMO, INUNDACION, SERVICIOS o SALUD. Ejemplo: SISMO");
            }
            unicos.add(normalizado);
        }
        return List.copyOf(unicos);
    }

    private static String exigir(String valor, String campo, String ejemplo, int max) {
        if (valor == null || valor.isBlank()) {
            throw Errores.datoInvalido(campo, ejemplo, "El campo " + campo + " es obligatorio. Ejemplo: " + ejemplo);
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw Errores.datoInvalido(campo, ejemplo,
                    "El campo " + campo + " admite hasta " + max + " caracteres. Ejemplo: " + ejemplo);
        }
        return limpio;
    }

    private static String recortar(String valor, int max) {
        if (valor == null) {
            return "";
        }
        return valor.length() <= max ? valor : valor.substring(0, max);
    }
}
