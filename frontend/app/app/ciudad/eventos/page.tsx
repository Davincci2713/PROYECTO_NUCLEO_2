"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha } from "@/lib/formato";
import { esServidor, leerUsuario } from "@/lib/sesion";
import type { Evento } from "@/lib/tipos";

export default function PaginaEventos() {
  const [eventos, setEventos] = useState<Evento[] | null>(null);
  const [titulo, setTitulo] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [lugar, setLugar] = useState("");
  const [inicia, setInicia] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [servidor, setServidor] = useState(false);

  function cargar() {
    return api<Evento[]>("/api/v1/ciudad/eventos").then(setEventos);
  }

  useEffect(() => {
    setServidor(esServidor(leerUsuario()?.rol));
    let cancelado = false;
    cargar().catch((err) => {
      if (!cancelado) setError(normalizarError(err));
    });
    return () => {
      cancelado = true;
    };
  }, []);

  async function inscribir(id: string) {
    setError(null);
    setOcupado(true);
    try {
      await api<void>(`/api/v1/ciudad/eventos/${id}/inscripcion`, { method: "POST" });
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function cancelar(id: string) {
    setError(null);
    setOcupado(true);
    try {
      await api<void>(`/api/v1/ciudad/eventos/${id}/inscripcion`, { method: "DELETE" });
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function crear(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      await api("/api/v1/ciudad/eventos", {
        method: "POST",
        body: {
          titulo,
          descripcion,
          lugar,
          iniciaEn: new Date(inicia).toISOString(),
        },
      });
      setTitulo("");
      setDescripcion("");
      setLugar("");
      setInicia("");
      await cargar();
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-25" titulo="Agenda municipal">
      <AvisoError error={error} />
      {eventos && eventos.length === 0 ? <p>No hay eventos publicados.</p> : null}
      {(eventos ?? []).map((evento) => (
        <article key={evento.id} className="tarjeta">
          <h2>{evento.titulo}</h2>
          <p>{evento.descripcion}</p>
          <p>
            {evento.lugar} · {fecha(evento.iniciaEn)}
          </p>
          <p>{evento.inscrito ? "Ya estás inscrito." : "No estás inscrito."}</p>
          <div className="fila-acciones">
            {evento.inscrito ? (
              <button type="button" className="secundario" disabled={ocupado} onClick={() => void cancelar(evento.id)}>
                Cancelar inscripción
              </button>
            ) : (
              <button type="button" disabled={ocupado} onClick={() => void inscribir(evento.id)}>
                Inscribirme
              </button>
            )}
          </div>
        </article>
      ))}
      {servidor ? (
        <form className="tarjeta" onSubmit={(evento) => void crear(evento)}>
          <h2>Nuevo evento</h2>
          <label htmlFor="titulo">Título</label>
          <input id="titulo" name="titulo" required value={titulo} onChange={(e) => setTitulo(e.target.value)} />
          <label htmlFor="descripcion">Descripción</label>
          <textarea id="descripcion" name="descripcion" required value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
          <label htmlFor="lugar">Lugar</label>
          <input id="lugar" name="lugar" required value={lugar} onChange={(e) => setLugar(e.target.value)} />
          <label htmlFor="inicia">Inicia</label>
          <input id="inicia" name="iniciaEn" type="datetime-local" required value={inicia} onChange={(e) => setInicia(e.target.value)} />
          <div className="fila-acciones">
            <button type="submit" disabled={ocupado}>
              Publicar evento
            </button>
          </div>
        </form>
      ) : null}
    </Pantalla>
  );
}
