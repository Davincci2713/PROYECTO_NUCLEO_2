"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { BotonPdf } from "@/components/AcusePago";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { dinero, fecha, parametro } from "@/lib/formato";
import type { Documento } from "@/lib/tipos";

export default function PaginaRecibo() {
  const params = useParams<{ id: string }>();
  const id = decodeURIComponent(parametro(params.id));
  const [documento, setDocumento] = useState<Documento | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);

  useEffect(() => {
    if (!id) return;
    let cancelado = false;
    api<Documento>(`/api/v1/documentos/${encodeURIComponent(id)}`)
      .then((datos) => {
        if (!cancelado) setDocumento(datos);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, [id]);

  return (
    <Pantalla hu="HU-07" titulo="Recibo">
      <AvisoError error={error} />
      {!documento && !error ? <p>Cargando metadatos…</p> : null}
      {documento ? (
        <article className="tarjeta">
          <span className="etiqueta">Folio</span>
          <p className="folio">{documento.folio}</p>
          <dl className="datos">
            <div>
              <dt>Tipo</dt>
              <dd>{documento.tipo}</dd>
            </div>
            <div>
              <dt>Concepto</dt>
              <dd>{documento.concepto}</dd>
            </div>
            <div>
              <dt>Monto</dt>
              <dd>{dinero(documento.monto)}</dd>
            </div>
            <div>
              <dt>Titular</dt>
              <dd>{documento.titular}</dd>
            </div>
            <div>
              <dt>Transacción</dt>
              <dd>{documento.transaccionId}</dd>
            </div>
            <div>
              <dt>Emitido</dt>
              <dd>{fecha(documento.emitidoEn)}</dd>
            </div>
            <div>
              <dt>Sello</dt>
              <dd>
                <code>{documento.sello}</code>
              </dd>
            </div>
            <div>
              <dt>QR</dt>
              <dd>
                <code>{documento.qrPayload}</code>
              </dd>
            </div>
          </dl>
          <div className="fila-acciones">
            <BotonCopiar texto={documento.folio} />
            <BotonPdf id={documento.id} />
            <Link className="boton secundario" href={`/verificar/${encodeURIComponent(documento.folio)}`}>
              Verificación pública
            </Link>
          </div>
        </article>
      ) : null}
    </Pantalla>
  );
}
