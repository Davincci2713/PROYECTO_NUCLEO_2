"use client";

import { FormEvent, useState } from "react";
import { AcusePago } from "@/components/AcusePago";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { FormularioCobro } from "@/components/FormularioCobro";
import { Pantalla } from "@/components/Pantalla";
import { Pasos } from "@/components/Pasos";
import { FranjaImporte } from "@/components/ResumenPago";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero } from "@/lib/formato";
import type { CobroRespuesta, ContratoAgua, Liquidacion } from "@/lib/tipos";

const PASOS = ["Consulta", "Adeudo", "Pago", "Recibo"];

export default function PaginaAgua() {
  const [paso, setPaso] = useState(1);
  const [numero, setNumero] = useState("");
  const [contrato, setContrato] = useState<ContratoAgua | null>(null);
  const [liquidacion, setLiquidacion] = useState<Liquidacion | null>(null);
  const [cobro, setCobro] = useState<CobroRespuesta | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  const concepto = liquidacion?.concepto ?? (contrato ? `Servicio de agua ${contrato.numeroContrato} periodo ${contrato.periodo}` : "Pendiente de consulta");
  const importe = liquidacion ? liquidacion.total : contrato ? contrato.importe : null;

  async function consultar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const datos = await api<ContratoAgua>(`/api/v1/agua/contratos/${encodeURIComponent(numero.trim())}`);
      if (contrato?.numeroContrato !== datos.numeroContrato) {
        setLiquidacion(null);
        setCobro(null);
      }
      setContrato(datos);
      setPaso(2);
    } catch (err) {
      setContrato(null);
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function liquidar() {
    if (!contrato) return;
    setError(null);
    setOcupado(true);
    try {
      const datos = await api<Liquidacion>("/api/v1/pagos/liquidaciones/agua", {
        method: "POST",
        body: { numeroContrato: contrato.numeroContrato },
      });
      setLiquidacion(datos);
      setPaso(3);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-02" titulo="Pago de agua">
      <p className="intro">El importe es el consumo por la tarifa, más la cuota fija. No hay descuento en línea.</p>
      <Pasos actual={paso} pasos={PASOS} />
      <FranjaImporte concepto={concepto} importe={importe} />

      {paso === 1 ? (
        <form className="tarjeta" onSubmit={(evento) => void consultar(evento)}>
          <label htmlFor="numero">Número de contrato</label>
          <input
            id="numero"
            name="numeroContrato"
            required
            placeholder="CTA-1001"
            value={numero}
            aria-invalid={campoInvalido(error, "numeroContrato")}
            onChange={(e) => setNumero(e.target.value)}
          />
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="submit" disabled={ocupado}>
              {ocupado ? "Consultando…" : "Consultar"}
            </button>
          </div>
        </form>
      ) : null}

      {paso === 2 && contrato ? (
        <section className="tarjeta">
          <h2>{contrato.numeroContrato}</h2>
          <dl className="datos">
            <div>
              <dt>Titular</dt>
              <dd>{contrato.titular}</dd>
            </div>
            <div>
              <dt>Dirección</dt>
              <dd>{contrato.direccion}</dd>
            </div>
            <div>
              <dt>Periodo</dt>
              <dd>{contrato.periodo}</dd>
            </div>
            <div>
              <dt>Lectura anterior</dt>
              <dd>{contrato.lecturaAnterior}</dd>
            </div>
            <div>
              <dt>Lectura actual</dt>
              <dd>{contrato.lecturaActual}</dd>
            </div>
            <div>
              <dt>Consumo</dt>
              <dd>{contrato.consumoM3} m³</dd>
            </div>
            <div>
              <dt>Tarifa</dt>
              <dd>{dinero(contrato.tarifaM3)} por m³</dd>
            </div>
            <div>
              <dt>Cuota fija</dt>
              <dd>{dinero(contrato.cuotaFija)}</dd>
            </div>
            <div>
              <dt>Importe</dt>
              <dd>{dinero(contrato.importe)}</dd>
            </div>
            <div>
              <dt>Pagado</dt>
              <dd>{contrato.pagado ? "Sí" : "No"}</dd>
            </div>
          </dl>
          {contrato.pagado ? <p>Este contrato ya está al corriente.</p> : null}
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="button" className="secundario" onClick={() => setPaso(1)}>
              Volver
            </button>
            <button type="button" disabled={ocupado} onClick={() => void liquidar()}>
              {ocupado ? "Calculando…" : "Continuar al pago"}
            </button>
          </div>
        </section>
      ) : null}

      {paso === 3 && liquidacion ? (
        <section className="tarjeta">
          <FormularioCobro
            liquidacion={liquidacion}
            onAutorizado={(resultado) => {
              setCobro(resultado);
              setPaso(4);
            }}
            onExpirada={(cuerpo) => {
              setLiquidacion(null);
              setError(cuerpo);
              setPaso(2);
            }}
          />
          <div className="fila-acciones">
            <button type="button" className="secundario" onClick={() => setPaso(2)}>
              Volver
            </button>
          </div>
        </section>
      ) : null}

      {paso === 4 && cobro ? (
        <section className="tarjeta">
          <AcusePago cobro={cobro} />
        </section>
      ) : null}
    </Pantalla>
  );
}
