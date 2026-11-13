"use client";

import Link from "next/link";
import { useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { abrirPdf, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero } from "@/lib/formato";
import type { CobroRespuesta } from "@/lib/tipos";

export function BotonPdf({ id }: { id: string }) {
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  async function abrir() {
    setError(null);
    setOcupado(true);
    try {
      await abrirPdf(id);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <>
      <button type="button" className="secundario" onClick={() => void abrir()} disabled={ocupado}>
        {ocupado ? "Abriendo PDF…" : "Abrir PDF"}
      </button>
      <AvisoError error={error} />
    </>
  );
}

export function AcusePago({ cobro }: { cobro: CobroRespuesta }) {
  return (
    <div role="status">
      <div className="aviso a-green">
        <b>Listo</b>
        <span>El pago fue autorizado. El importe y el concepto quedan en el recibo.</span>
      </div>
      <div className="recibo">
        <div className="recibo-top">
          <div>
            <h3>Recibo oficial de pago</h3>
            <p className="mono">{cobro.folio ? `Folio ${cobro.folio}` : "Sin folio"}</p>
          </div>
          <span className="tag t-green">Pagado</span>
        </div>
        <div className="recibo-body">
          <dl className="dl">
            <dt>Concepto</dt>
            <dd>{cobro.concepto}</dd>
            <dt>Importe pagado</dt>
            <dd>{dinero(cobro.total)}</dd>
            {cobro.medio ? (
              <>
                <dt>Medio de pago</dt>
                <dd>{cobro.medio}</dd>
              </>
            ) : null}
          </dl>
        </div>
      </div>
      {cobro.folio ? (
        <div className="fila-acciones">
          <BotonCopiar texto={cobro.folio} />
          {cobro.documentoId ? <BotonPdf id={cobro.documentoId} /> : null}
          {cobro.documentoId ? (
            <Link className="boton secundario" href={`/app/recibos/${cobro.documentoId}`}>
              Ver recibo
            </Link>
          ) : null}
          <Link className="boton secundario" href={`/verificar/${encodeURIComponent(cobro.folio)}`}>
            Verificación pública
          </Link>
        </div>
      ) : null}
    </div>
  );
}
