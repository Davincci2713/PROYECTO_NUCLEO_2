"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { BotonCopiar } from "@/components/BotonCopiar";
import { LineaTiempoVista } from "@/components/LineaTiempoVista";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha } from "@/lib/formato";
import type { Tramite } from "@/lib/tipos";

export default function PaginaBandeja() {
  const [tramites, setTramites] = useState<Tramite[] | null>(null);
  const [observando, setObservando] = useState<string | null>(null);
  const [observaciones, setObservaciones] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  function cargar() {
    return api<Tramite[]>("/api/v1/tramites/bandeja").then(setTramites);
  }

  useEffect(() => {
    let cancelado = false;
    cargar().catch((err) => {
      if (!cancelado) setError(normalizarError(err));
    });
    return () => {
      cancelado = true;
    };
  }, []);

  async function dictaminar(folio: string, decision: "APROBAR" | "OBSERVAR", evento?: FormEvent) {
    evento?.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api(`/api/v1/tramites/folio/${encodeURIComponent(folio)}/dictamen`, {
        method: "POST",
        body: decision === "OBSERVAR" ? { decision, observaciones } : { decision },
      });
      setObservando(null);
      setObservaciones("");
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-03" titulo="Bandeja de trámites">
      <p className="intro">Observar exige una nota. Si la omites, el municipio responde con el campo y un ejemplo.</p>
      <AvisoError error={error} />
      {tramites && tramites.length === 0 ? <p>No hay trámites en la bandeja.</p> : null}
      {(tramites ?? []).map((tramite) => (
        <article key={tramite.folio} className="tarjeta">
          <h2>{tramite.titulo}</h2>
          <p className="folio">{tramite.folio}</p>
          <BotonCopiar texto={tramite.folio} />
          <p>
            {tramite.tipo} · <span className="estado">{tramite.estado}</span> · {fecha(tramite.creadoEn)}
          </p>
          <LineaTiempoVista items={tramite.lineaTiempo} />
          <div className="fila-acciones">
            <button type="button" disabled={ocupado} onClick={() => void dictaminar(tramite.folio, "APROBAR")}>
              Aprobar
            </button>
            <button
              type="button"
              className="secundario"
              onClick={() => {
                setObservando(tramite.folio);
                setObservaciones("");
              }}
            >
              Observar
            </button>
          </div>
          {observando === tramite.folio ? (
            <form onSubmit={(evento) => void dictaminar(tramite.folio, "OBSERVAR", evento)}>
              <label htmlFor={`obs-${tramite.folio}`}>Observaciones</label>
              <textarea
                id={`obs-${tramite.folio}`}
                name="observaciones"
                value={observaciones}
                aria-invalid={campoInvalido(error, "observaciones")}
                onChange={(e) => setObservaciones(e.target.value)}
              />
              <div className="fila-acciones">
                <button type="submit" className="acento" disabled={ocupado}>
                  Enviar observación
                </button>
              </div>
            </form>
          ) : null}
        </article>
      ))}
    </Pantalla>
  );
}
