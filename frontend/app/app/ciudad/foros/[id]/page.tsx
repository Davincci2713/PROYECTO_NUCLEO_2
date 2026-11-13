"use client";

import { useParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha, parametro } from "@/lib/formato";
import { esServidor, leerUsuario } from "@/lib/sesion";
import type { Foro } from "@/lib/tipos";

export default function PaginaForo() {
  const params = useParams<{ id: string }>();
  const id = decodeURIComponent(parametro(params.id));
  const [foro, setForo] = useState<Foro | null>(null);
  const [comentario, setComentario] = useState("");
  const [oficial, setOficial] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [servidor, setServidor] = useState(false);

  function cargar() {
    return api<Foro>(`/api/v1/ciudad/foros/${encodeURIComponent(id)}`).then(setForo);
  }

  useEffect(() => {
    setServidor(esServidor(leerUsuario()?.rol));
    if (!id) return;
    let cancelado = false;
    cargar()
      .catch((err) => {
        if (!cancelado) setError(normalizarError(err));
      });
    return () => {
      cancelado = true;
    };
  }, [id]);

  async function comentar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api(`/api/v1/ciudad/foros/${encodeURIComponent(id)}/mensajes`, {
        method: "POST",
        body: { cuerpo: comentario },
      });
      setComentario("");
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function responder(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api(`/api/v1/ciudad/foros/${encodeURIComponent(id)}/respuesta-oficial`, {
        method: "POST",
        body: { cuerpo: oficial },
      });
      setOficial("");
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-19" titulo={foro?.titulo ?? "Foro"}>
      <AvisoError error={!foro ? error : null} />
      {foro ? (
        <>
          <article className="tarjeta">
            <p>{foro.cuerpo}</p>
            <p>
              {foro.autorNombre} · {fecha(foro.creadoEn)}
            </p>
          </article>
          {(foro.mensajes ?? []).map((mensaje) => (
            <article key={mensaje.id} className={mensaje.esOficial ? "tarjeta oficial" : "tarjeta"}>
              {mensaje.esOficial ? <p className="etiqueta">Respuesta oficial</p> : null}
              <p>{mensaje.cuerpo}</p>
              <p>
                {mensaje.autorNombre} · {fecha(mensaje.creadoEn)}
              </p>
            </article>
          ))}
          <form className="tarjeta" onSubmit={(evento) => void comentar(evento)}>
            <label htmlFor="comentario">Comentario</label>
            <textarea id="comentario" name="cuerpo" required value={comentario} onChange={(e) => setComentario(e.target.value)} />
            <AvisoError error={error} />
            <div className="fila-acciones">
              <button type="submit" disabled={ocupado}>
                Comentar
              </button>
            </div>
          </form>
          {servidor ? (
            <form className="tarjeta" onSubmit={(evento) => void responder(evento)}>
              <h2>Respuesta oficial</h2>
              <label htmlFor="oficial">Mensaje del municipio</label>
              <textarea id="oficial" name="oficial" required value={oficial} onChange={(e) => setOficial(e.target.value)} />
              <div className="fila-acciones">
                <button type="submit" className="acento" disabled={ocupado}>
                  Publicar respuesta oficial
                </button>
              </div>
            </form>
          ) : null}
        </>
      ) : null}
    </Pantalla>
  );
}
