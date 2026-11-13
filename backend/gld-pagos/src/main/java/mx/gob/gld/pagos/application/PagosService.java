package mx.gob.gld.pagos.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.money.Dinero;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.common.web.ClaveCatastral;
import mx.gob.gld.pagos.application.CatastroCliente.ContratoRemoto;
import mx.gob.gld.pagos.application.CatastroCliente.PeriodoRemoto;
import mx.gob.gld.pagos.application.CatastroCliente.PredioRemoto;
import mx.gob.gld.pagos.domain.LicenciaCalculo;
import mx.gob.gld.pagos.domain.LiquidacionVigencia;
import mx.gob.gld.pagos.domain.MultaCalculo;
import mx.gob.gld.pagos.domain.PredialCalculo;
import mx.gob.gld.pagos.domain.ResultadoCobro;
import mx.gob.gld.pagos.infrastructure.LiquidacionEntity;
import mx.gob.gld.pagos.infrastructure.LiquidacionRepository;
import mx.gob.gld.pagos.infrastructure.MultaEntity;
import mx.gob.gld.pagos.infrastructure.MultaRepository;
import mx.gob.gld.pagos.infrastructure.TarifaLicenciaEntity;
import mx.gob.gld.pagos.infrastructure.TarifaLicenciaRepository;
import mx.gob.gld.pagos.infrastructure.TransaccionEntity;
import mx.gob.gld.pagos.infrastructure.TransaccionRepository;
import mx.gob.gld.pagos.web.PagosDtos.AguaRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroRequest;
import mx.gob.gld.pagos.web.PagosDtos.CobroResponse;
import mx.gob.gld.pagos.web.PagosDtos.CotizacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.GiroResponse;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaCotizacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.LicenciaInternaRequest;
import mx.gob.gld.pagos.web.PagosDtos.Linea;
import mx.gob.gld.pagos.web.PagosDtos.LiquidacionResponse;
import mx.gob.gld.pagos.web.PagosDtos.MultaItem;
import mx.gob.gld.pagos.web.PagosDtos.MultaLiquidacionRequest;
import mx.gob.gld.pagos.web.PagosDtos.MultasResponse;
import mx.gob.gld.pagos.web.PagosDtos.PredialRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class PagosService {

    private final TarifaLicenciaRepository tarifas;
    private final MultaRepository multas;
    private final LiquidacionRepository liquidaciones;
    private final TransaccionRepository transacciones;
    private final CatastroCliente catastro;
    private final TramitesCliente tramites;
    private final DocumentosCliente documentos;
    private final Secuencias secuencias;
    private final BitacoraService bitacora;
    private final ObjectMapper json;
    private final Clock clock;
    private final EntityManager entityManager;
    private final PlatformTransactionManager transactionManager;

    public PagosService(TarifaLicenciaRepository tarifas,
                        MultaRepository multas,
                        LiquidacionRepository liquidaciones,
                        TransaccionRepository transacciones,
                        CatastroCliente catastro,
                        TramitesCliente tramites,
                        DocumentosCliente documentos,
                        Secuencias secuencias,
                        BitacoraService bitacora,
                        ObjectMapper json,
                        Clock clock,
                        EntityManager entityManager,
                        PlatformTransactionManager transactionManager) {
        this.tarifas = tarifas;
        this.multas = multas;
        this.liquidaciones = liquidaciones;
        this.transacciones = transacciones;
        this.catastro = catastro;
        this.tramites = tramites;
        this.documentos = documentos;
        this.secuencias = secuencias;
        this.bitacora = bitacora;
        this.json = json;
        this.clock = clock;
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
    }

    @Transactional(readOnly = true)
    public List<GiroResponse> giros() {
        List<GiroResponse> lista = new ArrayList<>();
        for (TarifaLicenciaEntity tarifa : tarifas.findByTenantIdOrderByGiroAsc(TenantContext.exigir())) {
            lista.add(new GiroResponse(tarifa.giro, Dinero.dos(tarifa.tarifaBase), Dinero.dos(tarifa.factorM2)));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public CotizacionResponse cotizar(LicenciaCotizacionRequest request) {
        cuerpo(request);
        TarifaLicenciaEntity tarifa = tarifa(request.giro());
        BigDecimal superficie = superficie(request.superficieM2());
        return new CotizacionResponse(
                tarifa.giro,
                Dinero.dos(tarifa.tarifaBase),
                Dinero.dos(tarifa.factorM2),
                superficie,
                LicenciaCalculo.total(tarifa.tarifaBase, tarifa.factorM2, superficie));
    }

    @Transactional(readOnly = true)
    public MultasResponse buscarMultas(String placa, String folio) {
        boolean conFolio = folio != null && !folio.isBlank();
        boolean conPlaca = placa != null && !placa.isBlank();
        if (!conFolio && !conPlaca) {
            throw Errores.placaObligatoria();
        }
        String tenant = TenantContext.exigir();
        List<MultaEntity> filas;
        if (conFolio) {
            filas = List.of(multas.findByTenantIdAndFolio(tenant, folio.trim())
                    .orElseThrow(Errores::multaNoEncontrada));
        } else {
            filas = multas.findByTenantIdAndPlacaOrderByFechaInfraccionDesc(tenant, placa.trim());
        }
        LocalDate hoy = LocalDate.now(clock);
        List<MultaItem> resultados = new ArrayList<>();
        for (MultaEntity multa : filas) {
            resultados.add(aMulta(multa, hoy));
        }
        return new MultasResponse(resultados);
    }

    @Transactional
    public LiquidacionResponse liquidarPredial(PredialRequest request) {
        cuerpo(request);
        if (!ClaveCatastral.valida(request.claveCatastral())) {
            throw Errores.claveCatastralInvalida();
        }
        List<String> periodos = periodos(request.periodos());
        PredioRemoto predio = catastro.predio(request.claveCatastral());
        List<PredialCalculo.Linea> calculo = new ArrayList<>();
        List<Linea> desglose = new ArrayList<>();
        for (String solicitado : periodos) {
            PeriodoRemoto periodo = predio.periodos().stream()
                    .filter(item -> solicitado.equals(item.periodo()))
                    .findFirst()
                    .orElseThrow(Errores::periodoNoDisponible);
            if (periodo.pagado()) {
                throw Errores.periodoNoDisponible();
            }
            BigDecimal base = Dinero.dos(periodo.importeBase());
            BigDecimal recargo = Dinero.dos(periodo.recargoMora());
            calculo.add(new PredialCalculo.Linea(solicitado, base, recargo));
            desglose.add(new Linea(solicitado, base, recargo, Dinero.dos(base.add(recargo))));
        }
        PredialCalculo.Totales totales = PredialCalculo.calcular(calculo);
        String lista = String.join(", ", periodos);
        return guardar(
                "PREDIAL",
                request.claveCatastral() + ":" + String.join(",", periodos),
                "Impuesto predial " + lista,
                totales.subtotal(),
                totales.recargos(),
                totales.descuento(),
                totales.total(),
                GldPrincipal.actual().id(),
                predio.propietario(),
                null,
                desglose);
    }

    @Transactional
    public LiquidacionResponse liquidarAgua(AguaRequest request) {
        cuerpo(request);
        if (request.numeroContrato() == null || request.numeroContrato().isBlank()) {
            throw Errores.contratoNoEncontrado();
        }
        ContratoRemoto contrato = catastro.contrato(request.numeroContrato().trim());
        if (contrato.pagado()) {
            throw Errores.saldoAlCorriente();
        }
        BigDecimal importe = Dinero.dos(contrato.importe());
        return guardar(
                "AGUA",
                contrato.numeroContrato(),
                "Servicio de agua " + contrato.numeroContrato() + " periodo " + contrato.periodo(),
                importe,
                Dinero.cero(),
                Dinero.cero(),
                importe,
                GldPrincipal.actual().id(),
                contrato.titular(),
                null,
                List.of(new Linea(contrato.periodo(), importe, Dinero.cero(), importe)));
    }

    @Transactional
    public LiquidacionResponse liquidarMulta(MultaLiquidacionRequest request) {
        cuerpo(request);
        if (request.folio() == null || request.folio().isBlank()) {
            throw Errores.multaNoEncontrada();
        }
        MultaEntity multa = multas.findByTenantIdAndFolio(TenantContext.exigir(), request.folio().trim())
                .orElseThrow(Errores::multaNoEncontrada);
        if (multa.pagada) {
            throw Errores.liquidacionNoActiva();
        }
        MultaItem item = aMulta(multa, LocalDate.now(clock));
        String concepto = item.dentroDeProntoPago()
                ? "Multa " + multa.folio + " pronto pago 50 %"
                : "Multa " + multa.folio;
        return guardar(
                "MULTA",
                multa.folio,
                concepto,
                Dinero.dos(multa.importe),
                Dinero.cero(),
                item.descuentoProntoPago(),
                item.totalAPagar(),
                GldPrincipal.actual().id(),
                GldPrincipal.actual().nombre(),
                null,
                List.of(new Linea(multa.folio, Dinero.dos(multa.importe), Dinero.cero(), item.totalAPagar())));
    }

    @Transactional
    public LiquidacionResponse crearLicencia(LicenciaInternaRequest request) {
        cuerpo(request);
        if (request.folioTramite() == null || request.folioTramite().isBlank()) {
            throw Errores.datoInvalido("folioTramite", "TRM-2026-000001",
                    "El folio del trámite es obligatorio. Ejemplo: TRM-2026-000001");
        }
        if (request.nombreComercial() == null || request.nombreComercial().isBlank()) {
            throw Errores.datoInvalido("nombreComercial", "Abarrotes Luna",
                    "El nombre comercial es obligatorio. Ejemplo: Abarrotes Luna");
        }
        if (request.total() == null || request.total().signum() <= 0) {
            throw Errores.datoInvalido("total", "1280.00",
                    "El total de la licencia debe ser mayor que cero. Ejemplo: 1280.00");
        }
        BigDecimal total = Dinero.dos(request.total());
        String nombre = request.nombreComercial().trim();
        return guardar(
                "LICENCIA",
                request.folioTramite().trim(),
                "Licencia de funcionamiento " + nombre,
                total,
                Dinero.cero(),
                Dinero.cero(),
                total,
                request.ciudadanoId(),
                request.titular() == null ? nombre : request.titular(),
                request.tramiteId(),
                List.of(new Linea(nombre, total, Dinero.cero(), total)));
    }

    @Transactional
    public LiquidacionResponse consultar(UUID id) {
        return aRespuesta(prepararEstado(buscar(id)));
    }

    @Transactional
    public CobroResponse cobrar(UUID id, CobroRequest request) {
        cuerpo(request);
        String medio = request.medio() == null ? "" : request.medio().trim();
        if (!"TARJETA".equals(medio) && !"PSE".equals(medio)) {
            throw Errores.medioInvalido();
        }
        String simular = request.simular() == null ? "" : request.simular().trim();
        ResultadoCobro.estadoLiquidacion(simular);
        LiquidacionEntity entidad = buscar(id);
        Instant ahora = clock.instant();
        if (LiquidacionVigencia.expirada(entidad.estado, entidad.venceEn, ahora)) {
            UUID liquidacionId = entidad.id;
            entityManager.detach(entidad);
            marcarExpirada(liquidacionId);
            throw Errores.liquidacionExpirada();
        }
        LiquidacionVigencia.exigirCobrable(entidad.estado, entidad.venceEn, ahora);
        boolean autorizado = ResultadoCobro.autorizado(simular);
        TransaccionEntity tx = new TransaccionEntity();
        tx.id = UUID.randomUUID();
        tx.liquidacionId = entidad.id;
        tx.medio = medio;
        tx.estado = ResultadoCobro.estadoTransaccion(simular);
        tx.monto = entidad.total;
        tx.concepto = entidad.concepto;
        tx.motivoRechazo = autorizado ? null : ResultadoCobro.MOTIVO_RECHAZO;
        tx.creadaEn = ahora;
        transacciones.save(tx);
        if (!autorizado) {
            return new CobroResponse(
                    "RECHAZADO", entidad.id, "ACTIVA", null, null, null,
                    ResultadoCobro.MOTIVO_RECHAZO, entidad.concepto, entidad.total, medio);
        }
        String folio = secuencias.folio("PAG", "pagos.folio_seq");
        entidad.estado = "PAGADA";
        entidad.folioPago = folio;
        JsonNode sobre = sobre(entidad);
        String titular = sobre.path("titular").asText("");
        if (titular.isBlank()) {
            titular = GldPrincipal.actual().nombre();
        }
        UUID tramiteId = tramiteId(sobre);
        aplicarEfecto(entidad);
        UUID documentoId = documentos.emitir(entidad.tenantId, new DocumentosCliente.Emitir(
                folio, "RECIBO_PAGO", tx.id, tramiteId, titular, entidad.concepto, entidad.total));
        bitacora.registrar(entidad.tenantId, GldPrincipal.actual().id(), "PAGO_AUTORIZADO", "PAGO", folio,
                entidad.concepto + " por " + entidad.total.toPlainString());
        return new CobroResponse(
                "AUTORIZADO", entidad.id, "PAGADA", folio, tx.id, documentoId,
                null, entidad.concepto, entidad.total, medio);
    }

    private void aplicarEfecto(LiquidacionEntity entidad) {
        switch (entidad.tipo) {
            case "PREDIAL" -> {
                int separador = entidad.referencia.indexOf(':');
                String clave = entidad.referencia.substring(0, separador);
                List<String> periodos = List.of(entidad.referencia.substring(separador + 1).split(","));
                catastro.marcarPredial(entidad.tenantId, clave, periodos);
            }
            case "AGUA" -> catastro.marcarAgua(entidad.tenantId, entidad.referencia);
            case "LICENCIA" -> tramites.pagado(entidad.tenantId, entidad.referencia, entidad.id);
            case "MULTA" -> multas.findByTenantIdAndFolio(entidad.tenantId, entidad.referencia)
                    .ifPresent(multa -> multa.pagada = true);
            default -> {
            }
        }
    }

    private void marcarExpirada(UUID id) {
        TransactionTemplate plantilla = new TransactionTemplate(transactionManager);
        plantilla.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        plantilla.executeWithoutResult(status -> {
            LiquidacionEntity actual = liquidaciones.findById(id).orElseThrow(Errores::liquidacionNoEncontrada);
            actual.estado = "EXPIRADA";
        });
    }

    private LiquidacionResponse guardar(String tipo, String referencia, String concepto,
                                        BigDecimal subtotal, BigDecimal recargos, BigDecimal descuento, BigDecimal total,
                                        UUID ciudadanoId, String titular, UUID tramiteId, List<Linea> desglose) {
        String tenant = TenantContext.exigir();
        LiquidacionEntity previa = liquidaciones
                .findByTenantIdAndTipoAndReferenciaAndEstado(tenant, tipo, referencia, "ACTIVA")
                .orElse(null);
        if (previa != null) {
            if (LiquidacionVigencia.expirada(previa.estado, previa.venceEn, clock.instant())) {
                previa.estado = "EXPIRADA";
                liquidaciones.saveAndFlush(previa);
            } else {
                return aRespuesta(previa);
            }
        }
        Instant ahora = clock.instant();
        LiquidacionEntity entidad = new LiquidacionEntity();
        entidad.id = UUID.randomUUID();
        entidad.tenantId = tenant;
        entidad.tipo = tipo;
        entidad.referencia = referencia;
        entidad.concepto = concepto;
        entidad.subtotal = subtotal;
        entidad.recargos = recargos;
        entidad.descuento = descuento;
        entidad.total = total;
        entidad.estado = "ACTIVA";
        entidad.ciudadanoId = ciudadanoId;
        entidad.desglose = escribirSobre(titular, tramiteId, desglose);
        entidad.creadaEn = ahora;
        entidad.venceEn = LiquidacionVigencia.venceEn(ahora);
        return aRespuesta(liquidaciones.save(entidad));
    }

    private LiquidacionEntity buscar(UUID id) {
        return liquidaciones.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(Errores::liquidacionNoEncontrada);
    }

    private LiquidacionEntity prepararEstado(LiquidacionEntity entidad) {
        String visible = LiquidacionVigencia.estadoVisible(entidad.estado, entidad.venceEn, clock.instant());
        if (!visible.equals(entidad.estado)) {
            entidad.estado = visible;
        }
        return entidad;
    }

    private LiquidacionResponse aRespuesta(LiquidacionEntity entidad) {
        return new LiquidacionResponse(
                entidad.id,
                entidad.tipo,
                entidad.referencia,
                entidad.concepto,
                entidad.estado,
                Dinero.dos(entidad.subtotal),
                Dinero.dos(entidad.recargos),
                Dinero.dos(entidad.descuento),
                porcentaje(entidad),
                Dinero.dos(entidad.total),
                entidad.venceEn,
                lineas(sobre(entidad)));
    }

    private static int porcentaje(LiquidacionEntity entidad) {
        if ("PREDIAL".equals(entidad.tipo)) {
            return 2;
        }
        if ("MULTA".equals(entidad.tipo) && entidad.descuento.signum() > 0) {
            return 50;
        }
        return 0;
    }

    private MultaItem aMulta(MultaEntity multa, LocalDate hoy) {
        boolean dentro = MultaCalculo.dentroDeProntoPago(multa.fechaInfraccion, hoy);
        BigDecimal importe = Dinero.dos(multa.importe);
        return new MultaItem(
                multa.folio,
                multa.placa,
                multa.descripcion,
                importe,
                multa.fechaInfraccion,
                dentro,
                MultaCalculo.descuento(importe, dentro),
                MultaCalculo.total(importe, dentro),
                multa.pagada);
    }

    private TarifaLicenciaEntity tarifa(String giro) {
        if (giro == null || giro.isBlank()) {
            throw Errores.giroDesconocido();
        }
        return tarifas.findByTenantIdAndGiro(TenantContext.exigir(), giro.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(Errores::giroDesconocido);
    }

    private static BigDecimal superficie(BigDecimal superficie) {
        if (superficie == null || superficie.signum() <= 0) {
            throw Errores.superficieInvalida();
        }
        return Dinero.dos(superficie);
    }

    private static List<String> periodos(List<String> solicitados) {
        if (solicitados == null || solicitados.isEmpty()) {
            throw Errores.periodoNoDisponible();
        }
        List<String> periodos = new ArrayList<>();
        for (String solicitado : solicitados) {
            if (solicitado != null && !solicitado.isBlank() && !periodos.contains(solicitado.trim())) {
                periodos.add(solicitado.trim());
            }
        }
        if (periodos.isEmpty()) {
            throw Errores.periodoNoDisponible();
        }
        return periodos;
    }

    private String escribirSobre(String titular, UUID tramiteId, List<Linea> desglose) {
        Map<String, Object> sobre = new LinkedHashMap<>();
        sobre.put("titular", titular);
        sobre.put("tramiteId", tramiteId == null ? null : tramiteId.toString());
        sobre.put("lineas", desglose);
        try {
            return json.writeValueAsString(sobre);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JsonNode sobre(LiquidacionEntity entidad) {
        try {
            return json.readTree(entidad.desglose);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private List<Linea> lineas(JsonNode sobre) {
        JsonNode nodo = sobre.path("lineas");
        if (!nodo.isArray()) {
            return List.of();
        }
        try {
            return json.readerForListOf(Linea.class).readValue(nodo);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static UUID tramiteId(JsonNode sobre) {
        String valor = sobre.path("tramiteId").asText("");
        if (valor.isBlank()) {
            return null;
        }
        return UUID.fromString(valor);
    }

    private static void cuerpo(Object request) {
        if (request == null) {
            throw Errores.datoInvalido("body", "{}", "El cuerpo de la solicitud es obligatorio. Ejemplo: {}");
        }
    }
}
