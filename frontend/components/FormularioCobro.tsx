"use client";

import { FormEvent, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { ResumenLiquidacion } from "@/components/ResumenPago";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero } from "@/lib/formato";
import type { CobroRespuesta, Liquidacion } from "@/lib/tipos";

export function FormularioCobro({
  liquidacion,
  onAutorizado,
  onExpirada,
}: {
  liquidacion: Liquidacion;
  onAutorizado: (cobro: CobroRespuesta) => void;
  onExpirada?: (error: CuerpoError) => void;
}) {
  const [medio, setMedio] = useState<"TARJETA" | "PSE">("TARJETA");
  const [rechazar, setRechazar] = useState(false);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [rechazo, setRechazo] = useState<CobroRespuesta | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const cobro = await api<CobroRespuesta>(`/api/v1/pagos/liquidaciones/${liquidacion.id}/cobrar`, {
        method: "POST",
        body: { medio, simular: rechazar ? "RECHAZADO" : "AUTORIZADO" },
      });
      if (cobro.estadoPago === "RECHAZADO") {
        setRechazo(cobro);
        return;
      }
      setRechazo(null);
      onAutorizado(cobro);
    } catch (err) {
      const cuerpo = normalizarError(err);
      setError(cuerpo);
      if (cuerpo.codigo === "LIQUIDACION_EXPIRADA") onExpirada?.(cuerpo);
    } finally {
      setOcupado(false);
    }
  }

  return (
    <form className="pasarela" onSubmit={(evento) => void enviar(evento)}>
      <div className="pasarela-top">
        <span className="lock">Pasarela bancaria</span>
        <span>
          Concepto <b>{liquidacion.concepto}</b>
        </span>
      </div>
      <div className="card pad">
        <div className="aviso a-green">
          <b>Usted va a pagar</b>
          <span>
            <b>{dinero(liquidacion.total)}</b> por {liquidacion.concepto}. Revise el importe antes de confirmar.
          </span>
        </div>
        <ResumenLiquidacion liquidacion={liquidacion} />
        <div className="met-row" role="group" aria-label="Medio de pago">
          <button type="button" className="met" aria-pressed={medio === "TARJETA"} onClick={() => setMedio("TARJETA")}>
            Tarjeta
          </button>
          <button type="button" className="met" aria-pressed={medio === "PSE"} onClick={() => setMedio("PSE")}>
            PSE
          </button>
        </div>
        <p className="sub">El municipio no guarda número de tarjeta, vencimiento ni CVV. Solo se envía el medio y el resultado simulado.</p>
        <label className="check">
          <input type="checkbox" checked={rechazar} onChange={(evento) => setRechazar(evento.target.checked)} />
          Simular rechazo
        </label>
        {rechazo ? (
          <div className="aviso a-ochre" role="status">
            <b>Pago rechazado</b>
            <span>
              La liquidación sigue {rechazo.estadoLiquidacion}. Concepto: {rechazo.concepto}. Importe: {dinero(rechazo.total)}.
              {rechazo.motivo ? ` ${rechazo.motivo}` : ""}
            </span>
          </div>
        ) : null}
        <AvisoError error={error} />
        <button className="btn-lg btn-block" type="submit" disabled={ocupado}>
          {ocupado ? "Autorizando…" : "Confirmar pago"}
        </button>
      </div>
    </form>
  );
}
