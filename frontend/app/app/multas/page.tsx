"use client";

import { FormEvent, useState } from "react";
import { AcusePago } from "@/components/AcusePago";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { FormularioCobro } from "@/components/FormularioCobro";
import { Pantalla } from "@/components/Pantalla";
import { Pasos } from "@/components/Pasos";
import { FranjaImporte } from "@/components/ResumenPago";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero, fecha } from "@/lib/formato";
import type { CobroRespuesta, Liquidacion, Multa } from "@/lib/tipos";

const PASOS = ["Consulta", "Adeudo", "Pago", "Recibo"];

export default function PaginaMultas() {
  const [paso, setPaso] = useState(1);
  const [placa, setPlaca] = useState("");
  const [folio, setFolio] = useState("");
  const [resultados, setResultados] = useState<Multa[] | null>(null);
  const [elegida, setElegida] = useState<Multa | null>(null);
  const [liquidacion, setLiquidacion] = useState<Liquidacion | null>(null);
  const [cobro, setCobro] = useState<CobroRespuesta | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  const concepto = liquidacion?.concepto ?? (elegida ? `${elegida.descripcion} ${elegida.folio}` : "Pendiente de consulta");
  const importe = liquidacion?.total ?? elegida?.totalAPagar ?? null;

  async function consultar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setResultados(null);
    setElegida(null);
    setLiquidacion(null);
    setOcupado(true);
    const consulta = folio.trim()
      ? `?folio=${encodeURIComponent(folio.trim())}`
      : placa.trim()
        ? `?placa=${encodeURIComponent(placa.trim())}`
        : "";
    try {
      const datos = await api<{ resultados: Multa[] }>(`/api/v1/pagos/multas${consulta}`);
      setResultados(datos.resultados);
      setPaso(2);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function liquidar() {
    if (!elegida) return;
    setError(null);
    setOcupado(true);
    try {
      const datos = await api<Liquidacion>("/api/v1/pagos/liquidaciones/multa", {
        method: "POST",
        body: { folio: elegida.folio },
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
    <Pantalla hu="HU-04" titulo="Pago de multas">
      <p className="intro">Si la infracción está dentro del pronto pago, el descuento es del 50 %.</p>
      <Pasos actual={paso} pasos={PASOS} />
      <FranjaImporte concepto={concepto} importe={importe} />

      {paso === 1 ? (
        <form className="tarjeta" onSubmit={(evento) => void consultar(evento)}>
          <label htmlFor="placa">Placa</label>
          <input
            id="placa"
            name="placa"
            placeholder="ABC-123"
            value={placa}
            aria-invalid={campoInvalido(error, "placa")}
            onChange={(e) => setPlaca(e.target.value)}
          />
          <label htmlFor="folio">Folio</label>
          <input
            id="folio"
            name="folio"
            placeholder="MUL-2026-001"
            value={folio}
            aria-invalid={campoInvalido(error, "folio")}
            onChange={(e) => setFolio(e.target.value)}
          />
          <p>Indica la placa o el folio. Si escribes el folio, la consulta usa el folio.</p>
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="submit" disabled={ocupado}>
              {ocupado ? "Buscando…" : "Buscar"}
            </button>
          </div>
        </form>
      ) : null}

      {paso === 2 ? (
        <section className="tarjeta">
          {resultados && resultados.length === 0 ? <p>La consulta no devolvió multas.</p> : null}
          <fieldset>
            <legend>Resultados</legend>
            {(resultados ?? []).map((multa) => (
              <label className="check" key={multa.folio}>
                <input
                  type="radio"
                  name="multa"
                  value={multa.folio}
                  disabled={multa.pagada}
                  checked={elegida?.folio === multa.folio}
                  onChange={() => {
                    setElegida(multa);
                    setLiquidacion(null);
                  }}
                />
                <span>
                  <strong>{multa.folio}</strong> · {multa.placa}
                  <br />
                  {multa.descripcion}
                  <br />
                  Infracción {fecha(multa.fechaInfraccion)} · Importe {dinero(multa.importe)}
                  {multa.dentroDeProntoPago ? (
                    <>
                      <br />
                      Descuento del 50 % por pronto pago: {dinero(multa.descuentoProntoPago)}. Total a pagar {dinero(multa.totalAPagar)}.
                    </>
                  ) : (
                    <>
                      <br />
                      Sin descuento de pronto pago. Total a pagar {dinero(multa.totalAPagar)}.
                    </>
                  )}
                  {multa.pagada ? <><br />Ya está pagada.</> : null}
                </span>
              </label>
            ))}
          </fieldset>
          <AvisoError error={error} />
          <div className="fila-acciones">
            <button type="button" className="secundario" onClick={() => setPaso(1)}>
              Volver
            </button>
            <button type="button" disabled={!elegida || ocupado} onClick={() => void liquidar()}>
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
