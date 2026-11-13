"use client";

import { FormEvent, useEffect, useState } from "react";
import { AvisoError, campoInvalido } from "@/components/AvisoError";
import { Pantalla } from "@/components/Pantalla";
import { api, normalizarError, type CuerpoError } from "@/lib/api";
import { fecha } from "@/lib/formato";
import type { MensajeSala, Sala } from "@/lib/tipos";

export default function PaginaChat() {
  const [salas, setSalas] = useState<Sala[] | null>(null);
  const [salaId, setSalaId] = useState("");
  const [mensajes, setMensajes] = useState<MensajeSala[] | null>(null);
  const [nombre, setNombre] = useState("");
  const [tema, setTema] = useState("");
  const [texto, setTexto] = useState("");
  const [error, setError] = useState<CuerpoError | null>(null);
  const [errorMensaje, setErrorMensaje] = useState<CuerpoError | null>(null);
  const [ocupado, setOcupado] = useState(false);

  function cargarSalas() {
    return api<Sala[]>("/api/v1/ciudad/salas").then(setSalas);
  }

  async function cargarMensajes(id: string) {
    const lista = await api<MensajeSala[]>(`/api/v1/ciudad/salas/${encodeURIComponent(id)}/mensajes`);
    setMensajes(lista);
  }

  useEffect(() => {
    let cancelado = false;
    cargarSalas().catch((err) => {
      if (!cancelado) setError(normalizarError(err));
    });
    return () => {
      cancelado = true;
    };
  }, []);

  async function elegir(id: string) {
    setSalaId(id);
    setMensajes(null);
    setErrorMensaje(null);
    try {
      await cargarMensajes(id);
    } catch (err) {
      setErrorMensaje(normalizarError(err));
    }
  }

  async function crear(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setOcupado(true);
    try {
      const sala = await api<Sala>("/api/v1/ciudad/salas", { method: "POST", body: { nombre, tema } });
      setNombre("");
      setTema("");
      await cargarSalas();
      if (sala?.id) await elegir(sala.id);
    } catch (err) {
      setError(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!salaId) return;
    setErrorMensaje(null);
    setOcupado(true);
    try {
      await api(`/api/v1/ciudad/salas/${encodeURIComponent(salaId)}/mensajes`, {
        method: "POST",
        body: { texto },
      });
      setTexto("");
      await cargarMensajes(salaId);
    } catch (err) {
      setErrorMensaje(normalizarError(err));
    } finally {
      setOcupado(false);
    }
  }

  return (
    <Pantalla hu="HU-22" titulo="Salas de chat">
      <p className="intro">Si el mensaje incluye lenguaje no permitido, no se publica y verás el detalle del rechazo.</p>
      <form className="tarjeta" onSubmit={(evento) => void crear(evento)}>
        <h2>Nueva sala</h2>
        <label htmlFor="nombre">Nombre</label>
        <input id="nombre" name="nombre" required value={nombre} onChange={(e) => setNombre(e.target.value)} />
        <label htmlFor="tema">Tema</label>
        <input id="tema" name="tema" required value={tema} onChange={(e) => setTema(e.target.value)} />
        <AvisoError error={error} />
        <div className="fila-acciones">
          <button type="submit" disabled={ocupado}>
            Crear sala
          </button>
        </div>
      </form>
      <div className="rejilla rejilla-2">
        <section className="tarjeta">
          <h2>Salas</h2>
          {salas && salas.length === 0 ? <p>No hay salas.</p> : null}
          <ul className="nav" style={{ flexDirection: "column" }}>
            {(salas ?? []).map((sala) => (
              <li key={sala.id}>
                <button type="button" className={sala.id === salaId ? "acento" : "secundario"} onClick={() => void elegir(sala.id)}>
                  {sala.nombre} · {sala.tema}
                </button>
              </li>
            ))}
          </ul>
        </section>
        <section className="tarjeta">
          <h2>Mensajes</h2>
          {!salaId ? <p>Elige una sala.</p> : null}
          {(mensajes ?? []).map((mensaje) => (
            <article key={mensaje.id} className="burbuja">
              <strong>{mensaje.autorNombre}</strong>
              <p>{mensaje.texto}</p>
              <p>{fecha(mensaje.creadoEn)}</p>
            </article>
          ))}
          {salaId ? (
            <form onSubmit={(evento) => void enviar(evento)}>
              <label htmlFor="texto">Mensaje</label>
              <textarea
                id="texto"
                name="texto"
                required
                value={texto}
                aria-invalid={campoInvalido(errorMensaje, "texto")}
                onChange={(e) => setTexto(e.target.value)}
              />
              <AvisoError error={errorMensaje} />
              <div className="fila-acciones">
                <button type="submit" disabled={ocupado}>
                  Enviar
                </button>
              </div>
            </form>
          ) : null}
        </section>
      </div>
    </Pantalla>
  );
}
