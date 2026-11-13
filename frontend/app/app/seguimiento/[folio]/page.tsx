"use client";

import { useParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { LineaTiempoVista } from "@/components/LineaTiempoVista";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { leerArchivos, parametro } from "@/lib/formato";
import type { Tramite } from "@/lib/tipos";

export default function PaginaFolio() {
  const params = useParams<{ folio: string }>();
  const folio = decodeURIComponent(parametro(params.folio));
  const [tramite, setTramite] = useState<Tramite | null>(null);
  const [archivos, setArchivos] = useState<FileList | null>(null);
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  useEffect(() => {
    if (!folio) return;
    let cancelado = false;
    api<Tramite>(`/api/v1/tramites/folio/${encodeURIComponent(folio)}`)
      .then((datos) => {
        if (!cancelado) setTramite(datos);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, [folio]);

  async function subsanar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const documentos = archivos ? await leerArchivos(archivos) : [];
      const actualizado = await api<Tramite>(`/api/v1/tramites/folio/${encodeURIComponent(folio)}/subsanar`, {
        method: "POST",
        body: { documentos },
      });
      if (actualizado && actualizado.folio) setTramite(actualizado);
      else setTramite(await api<Tramite>(`/api/v1/tramites/folio/${encodeURIComponent(folio)}`));
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-06" titulo="Detalle del trámite">
      <AvisoError error={!tramite ? error : null} />
      {tramite ? (
        <article className="tarjeta">
          <h2>{tramite.titulo}</h2>
          <p className="folio">{tramite.folio}</p>
          <BotonCopiar texto={tramite.folio} />
          <p>
            {tramite.tipo} · <span className="estado">{tramite.estado}</span>
          </p>
          <h2>Línea de tiempo</h2>
          <LineaTiempoVista items={tramite.lineaTiempo} />
          {tramite.estado === "POR_SUBSANAR" ? (
            <form onSubmit={(evento) => void subsanar(evento)}>
              <h2>Subsanar</h2>
              <p>El folio se conserva. Adjunta los documentos corregidos.</p>
              <label htmlFor="documentos">Documentos</label>
              <input
                id="documentos"
                name="documentos"
                type="file"
                multiple
                aria-invalid={campoInvalido(error, "documentos")}
                onChange={(e) => setArchivos(e.target.files)}
              />
              <AvisoError error={error} />
              <div className="fila-acciones">
                <button type="submit" disabled={ocupado}>
                  {ocupado ? "Enviando…" : "Enviar subsanación"}
                </button>
              </div>
            </form>
          ) : null}
        </article>
      ) : null}
    </Pantalla>
  );
}
