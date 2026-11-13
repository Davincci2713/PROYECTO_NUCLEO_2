"use client";

import { FormEvent, useState } from "react";
import { AcusePago } from "@/components/AcusePago";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { FormularioCobro } from "@/components/FormularioCobro";
import { Pantalla } from "@/components/Pantalla";
import { Pasos } from "@/components/Pasos";
import { FranjaImporte } from "@/components/ResumenPago";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero, fecha, redondear } from "@/lib/formato";
import type { CobroRespuesta, Liquidacion, Predio } from "@/lib/tipos";

const PASOS = ["Consulta", "Adeudo", "Pago", "Recibo"];

export default function PaginaPredial() {
  const [paso, setPaso] = useState(1);
  const [clave, setClave] = useState("");
  const [predio, setPredio] = useState<Predio | null>(null);
  const [seleccion, setSeleccion] = useState<string[]>([]);
  const [liquidacion, setLiquidacion] = useState<Liquidacion | null>(null);
  const [cobro, setCobro] = useState<CobroRespuesta | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  const periodosElegidos = predio?.periodos.filter((p) => seleccion.includes(p.periodo)) ?? [];
  const suma = redondear(periodosElegidos.reduce((total, p) => total + p.importePeriodo, 0));
  const conceptoVisible = liquidacion?.concepto ?? (seleccion.length > 0 ? `Impuesto predial ${seleccion.join(", ")}` : "Pendiente de consulta");
  const importeVisible = liquidacion ? liquidacion.total : seleccion.length > 0 ? suma : null;

  async function consultar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const datos = await api<Predio>(`/api/v1/predios/${encodeURIComponent(clave.trim())}`);
      const misma = predio?.claveCatastral === datos.claveCatastral;
      setPredio(datos);
      if (!misma) {
        setSeleccion([]);
        setLiquidacion(null);
        setCobro(null);
      }
      setPaso(2);
    } catch (err) {
      setPredio(null);
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  function alternar(periodo: string) {
    setSeleccion((actual) => (actual.includes(periodo) ? actual.filter((item) => item !== periodo) : [...actual, periodo]));
    setLiquidacion(null);
    setCobro(null);
  }

  async function liquidar() {
    if (!predio) return;
    setError(null);
    setOcupado(true);
    try {
      const datos = await api<Liquidacion>("/api/v1/pagos/liquidaciones/predial", {
        method: "POST",
        body: { claveCatastral: predio.claveCatastral, periodos: seleccion },
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
    <Pantalla hu="HU-01" titulo="Pago de predial">
      <p className="intro">El descuento del 2 % en línea se confirma en la liquidación. Si la pasarela rechaza el cargo, la liquidación sigue activa.</p>
      <Pasos actual={paso} pasos={PASOS} />
      <FranjaImporte concepto={conceptoVisible} importe={importeVisible} />

      {paso === 1 ? (
        <form className="tarjeta" onSubmit={(evento) => void consultar(evento)}>
          <label htmlFor="clave">Clave catastral</label>
          <input
            id="clave"
            name="claveCatastral"
            required
            placeholder="05-021-0147-003"
            value={clave}
            aria-invalid={campoInvalido(error, "claveCatastral")}
            onChange={(e) => setClave(e.target.value)}
          />
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="submit" disabled={ocupado}>
              {ocupado ? "Consultando…" : "Consultar"}
            </button>
          </div>
        </form>
      ) : null}

      {paso === 2 && predio ? (
        <section className="tarjeta">
          <h2>{predio.claveCatastral}</h2>
          <dl className="datos">
            <div>
              <dt>Propietario</dt>
              <dd>{predio.propietario}</dd>
            </div>
            <div>
              <dt>Dirección</dt>
              <dd>{predio.direccion}</dd>
            </div>
            <div>
              <dt>Superficie</dt>
              <dd>{predio.superficieM2} m²</dd>
            </div>
          </dl>
          <fieldset>
            <legend>Periodos</legend>
            {predio.periodos.map((periodo) => (
              <label className="check" key={periodo.periodo}>
                <input
                  type="checkbox"
                  name="periodos"
                  value={periodo.periodo}
                  disabled={periodo.pagado}
                  checked={!periodo.pagado && seleccion.includes(periodo.periodo)}
                  onChange={() => alternar(periodo.periodo)}
                />
                <span>
                  <strong>{periodo.periodo}</strong>
                  {periodo.pagado ? " · Pagado" : ""}
                  {periodo.vencido ? " · Vencido" : ""}
                  <br />
                  Base {dinero(periodo.importeBase)} · Recargo {dinero(periodo.recargoMora)} · Periodo {dinero(periodo.importePeriodo)}
                  <br />
                  Vence {fecha(periodo.fechaVencimiento)}
                </span>
              </label>
            ))}
          </fieldset>
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
          <div className="fila-acciones">
            <button
              type="button"
              className="secundario"
              onClick={() => {
                setPaso(1);
                setPredio(null);
                setSeleccion([]);
                setLiquidacion(null);
                setCobro(null);
                setError(null);
              }}
            >
              Nueva consulta
            </button>
          </div>
        </section>
      ) : null}
    </Pantalla>
  );
}
