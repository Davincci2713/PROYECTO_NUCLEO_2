"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha } from "@/lib/formato";
import type { Tramite } from "@/lib/tipos";

export default function PaginaSeguimiento() {
  const [tramites, setTramites] = useState<Tramite[] | null>(null);
  const [folio, setFolio] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);

  useEffect(() => {
    let cancelado = false;
    api<Tramite[]>("/api/v1/tramites/mios")
      .then((lista) => {
        if (!cancelado) setTramites(lista);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  function abrir(evento: FormEvent) {
    evento.preventDefault();
    const limpio = folio.trim();
    if (!limpio) return;
    window.location.assign(`/app/seguimiento/${encodeURIComponent(limpio)}`);
  }

  return (
    <Pantalla hu="HU-06" titulo="Seguimiento de trámites">
      <p className="intro">Aquí están los trámites de tu cuenta. También puedes abrir un folio directamente.</p>
      <form className="tarjeta" onSubmit={abrir}>
        <label htmlFor="folio">Folio</label>
        <input
          id="folio"
          name="folio"
          placeholder="TRM-2026-000001"
          value={folio}
          aria-invalid={campoInvalido(error, "folio")}
          onChange={(e) => setFolio(e.target.value)}
        />
        <div className="fila-acciones">
          <button type="submit">Abrir folio</button>
        </div>
      </form>
      <AvisoError error={error} />
      {tramites && tramites.length === 0 ? <p>No tienes trámites registrados.</p> : null}
      {(tramites ?? []).map((tramite) => (
        <article key={tramite.folio} className="tarjeta">
          <h2>{tramite.titulo}</h2>
          <p className="folio">{tramite.folio}</p>
          <p>
            {tramite.tipo} · <span className="estado">{tramite.estado}</span>
          </p>
          <p>Creado {fecha(tramite.creadoEn)}</p>
          <Link className="boton" href={`/app/seguimiento/${encodeURIComponent(tramite.folio)}`}>
            Ver línea de tiempo
          </Link>
        </article>
      ))}
    </Pantalla>
  );
}
