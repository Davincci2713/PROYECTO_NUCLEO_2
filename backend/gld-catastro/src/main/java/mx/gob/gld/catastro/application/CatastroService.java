package mx.gob.gld.catastro.application;

import mx.gob.gld.catastro.domain.AguaCalculo;
import mx.gob.gld.catastro.infrastructure.ContratoAguaEntity;
import mx.gob.gld.catastro.infrastructure.ContratoAguaRepository;
import mx.gob.gld.catastro.infrastructure.PeriodoPredialEntity;
import mx.gob.gld.catastro.infrastructure.PeriodoPredialRepository;
import mx.gob.gld.catastro.infrastructure.PredioEntity;
import mx.gob.gld.catastro.infrastructure.PredioRepository;
import mx.gob.gld.catastro.web.CatastroDtos.ContratoResponse;
import mx.gob.gld.catastro.web.CatastroDtos.PeriodoResponse;
import mx.gob.gld.catastro.web.CatastroDtos.PredioResponse;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.money.Dinero;
import mx.gob.gld.common.security.Acceso;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.common.web.ClaveCatastral;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatastroService {

    private final PredioRepository predios;
    private final PeriodoPredialRepository periodos;
    private final ContratoAguaRepository contratos;
    private final Clock clock;

    public CatastroService(PredioRepository predios,
                           PeriodoPredialRepository periodos,
                           ContratoAguaRepository contratos,
                           Clock clock) {
        this.predios = predios;
        this.periodos = periodos;
        this.contratos = contratos;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PredioResponse predio(String clave) {
        if (!ClaveCatastral.valida(clave)) {
            throw Errores.claveCatastralInvalida();
        }
        PredioEntity predio = predios.findByTenantIdAndClaveCatastral(TenantContext.exigir(), clave)
                .orElseThrow(Errores::predioNoEncontrado);
        LocalDate hoy = LocalDate.now(clock);
        List<PeriodoResponse> lista = new ArrayList<>();
        for (PeriodoPredialEntity periodo : periodos.findByPredioIdOrderByFechaVencimientoAsc(predio.id)) {
            lista.add(new PeriodoResponse(
                    periodo.periodo,
                    Dinero.dos(periodo.importeBase),
                    Dinero.dos(periodo.recargoMora),
                    Dinero.dos(periodo.importeBase.add(periodo.recargoMora)),
                    periodo.fechaVencimiento,
                    periodo.fechaVencimiento.isBefore(hoy),
                    periodo.pagado));
        }
        return new PredioResponse(
                predio.claveCatastral,
                predio.propietario,
                predio.direccion,
                Dinero.dos(predio.superficieM2),
                lista);
    }

    @Transactional(readOnly = true)
    public ContratoResponse contrato(String numero) {
        return aContrato(buscarContrato(numero));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> contratos() {
        Acceso.exigirServidor();
        List<ContratoResponse> lista = new ArrayList<>();
        for (ContratoAguaEntity contrato : contratos.findByTenantIdOrderByNumeroContratoAsc(TenantContext.exigir())) {
            lista.add(aContrato(contrato));
        }
        return lista;
    }

    @Transactional
    public void marcarPredialPagado(String clave, List<String> solicitados) {
        if (!ClaveCatastral.valida(clave)) {
            throw Errores.claveCatastralInvalida();
        }
        if (solicitados == null || solicitados.isEmpty()) {
            throw Errores.periodoNoDisponible();
        }
        PredioEntity predio = predios.findByTenantIdAndClaveCatastral(TenantContext.exigir(), clave)
                .orElseThrow(Errores::predioNoEncontrado);
        List<PeriodoPredialEntity> existentes = periodos.findByPredioIdOrderByFechaVencimientoAsc(predio.id);
        for (String solicitado : solicitados) {
            PeriodoPredialEntity periodo = existentes.stream()
                    .filter(item -> item.periodo.equals(solicitado))
                    .findFirst()
                    .orElseThrow(Errores::periodoNoDisponible);
            periodo.pagado = true;
        }
    }

    @Transactional
    public void marcarAguaPagado(String numero) {
        ContratoAguaEntity contrato = buscarContrato(numero);
        contrato.pagado = true;
    }

    private ContratoAguaEntity buscarContrato(String numero) {
        if (numero == null || numero.isBlank()) {
            throw Errores.contratoNoEncontrado();
        }
        return contratos.findByTenantIdAndNumeroContrato(TenantContext.exigir(), numero.trim())
                .orElseThrow(Errores::contratoNoEncontrado);
    }

    private static ContratoResponse aContrato(ContratoAguaEntity contrato) {
        int consumo = AguaCalculo.consumo(contrato.lecturaAnterior, contrato.lecturaActual);
        return new ContratoResponse(
                contrato.numeroContrato,
                contrato.titular,
                contrato.direccion,
                contrato.periodo,
                contrato.lecturaAnterior,
                contrato.lecturaActual,
                consumo,
                Dinero.dos(contrato.tarifaM3),
                Dinero.dos(contrato.cuotaFija),
                AguaCalculo.importe(contrato.lecturaAnterior, contrato.lecturaActual, contrato.tarifaM3, contrato.cuotaFija),
                contrato.pagado);
    }
}
