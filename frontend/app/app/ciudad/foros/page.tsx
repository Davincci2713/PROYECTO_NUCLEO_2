"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha } from "@/lib/formato";
import type { Foro } from "@/lib/tipos";

export default function PaginaForos() {
  const [foros, setForos] = useState<Foro[] | null>(null);
  const [titulo, setTitulo] = useState("");
  const [cuerpo, setCuerpo] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  function cargar() {
    return api<Foro[]>("/api/v1/ciudad/foros")
      .then(setForos)
      .catch((err) => setError(normalizarError(err)));
  }

  useEffect(() => {
    let cancelado = false;
    api<Foro[]>("/api/v1/ciudad/foros")
      .then((lista) => {
        if (!cancelado) setForos(lista);
      })
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, []);

  async function crear(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api<Foro>("/api/v1/ciudad/foros", { method: "POST", body: { titulo, cuerpo } });
      setTitulo("");
      setCuerpo("");
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-19" titulo="Foros">
      <form className="tarjeta" onSubmit={(evento) => void crear(evento)}>
        <h2>Nuevo tema</h2>
        <label htmlFor="titulo">Título</label>
        <input id="titulo" name="titulo" required value={titulo} onChange={(e) => setTitulo(e.target.value)} />
        <label htmlFor="cuerpo">Mensaje</label>
        <textarea id="cuerpo" name="cuerpo" required value={cuerpo} onChange={(e) => setCuerpo(e.target.value)} />
        <AvisoError error={error} />
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado}>
            {ocupado ? "Publicando…" : "Publicar"}
          </button>
        </div>
      </form>
      {foros && foros.length === 0 ? <p>No hay temas publicados.</p> : null}
      {(foros ?? []).map((foro) => (
        <article key={foro.id} className="tarjeta">
          <h2>{foro.titulo}</h2>
          <p>{foro.cuerpo}</p>
          <p>
            {foro.autorNombre} · {fecha(foro.creadoEn)}
            {typeof foro.respuestas === "number" ? ` · ${foro.respuestas} respuestas` : ""}
          </p>
          <Link className="boton" href={`/app/ciudad/foros/${foro.id}`}>
            Abrir
          </Link>
        </article>
      ))}
    </Pantalla>
  );
}
