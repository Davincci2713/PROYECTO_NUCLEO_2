"use client";

import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { MarcoPublico } from "@/components/MarcoPublico";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero, fecha, parametro } from "@/lib/formato";
import type { VerificacionDocumento } from "@/lib/tipos";

export default function PaginaVerificarDocumento() {
  const params = useParams<{ folio: string }>();
  const folio = decodeURIComponent(parametro(params.folio));
  const [datos, setDatos] = useState<VerificacionDocumento | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);

  useEffect(() => {
    if (!folio) return;
    let cancelado = false;
    api<VerificacionDocumento>(`/api/v1/documentos/verificar/${encodeURIComponent(folio)}`, { auth: false })
      .then((respuesta) => {
        if (!cancelado) setDatos(respuesta);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, [folio]);

  return (
    <MarcoPublico>
      <main id="contenido" className="contenedor" data-hu="HU-07">
        <h1>Verificación de documento</h1>
        <p className="intro">Consulta pública. No requiere sesión.</p>
        <AvisoError error={error} />
        {!datos && !error ? <p>Consultando folio…</p> : null}
        {datos ? (
          <article className="tarjeta">
            <p className="folio">{datos.folio}</p>
            <BotonCopiar texto={datos.folio} />
            {datos.valido ? (
              <dl className="datos">
                <div>
                  <dt>Resultado</dt>
                  <dd>Documento válido</dd>
                </div>
                {datos.concepto ? (
                  <div>
                    <dt>Concepto</dt>
                    <dd>{datos.concepto}</dd>
                  </div>
                ) : null}
                {datos.monto !== undefined ? (
                  <div>
                    <dt>Monto</dt>
                    <dd>{dinero(datos.monto)}</dd>
                  </div>
                ) : null}
                {datos.titular ? (
                  <div>
                    <dt>Titular</dt>
                    <dd>{datos.titular}</dd>
                  </div>
                ) : null}
                {datos.emitidoEn ? (
                  <div>
                    <dt>Emitido</dt>
                    <dd>{fecha(datos.emitidoEn)}</dd>
                  </div>
                ) : null}
                {datos.sello ? (
                  <div>
                    <dt>Sello</dt>
                    <dd>
                      <code>{datos.sello}</code>
                    </dd>
                  </div>
                ) : null}
              </dl>
            ) : (
              <p role="status">No hay un documento válido con el folio {datos.folio}.</p>
            )}
          </article>
        ) : null}
      </main>
    </MarcoPublico>
  );
}
